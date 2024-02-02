package eu.merklaafe.diarywrite

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.merklaafe.diaryutil.model.Diary
import eu.merklaafe.diaryutil.model.Mood
import eu.merklaafe.diaryutil.model.RequestState
import eu.merklaafe.diarymongo.database.ImageToDeleteDao
import eu.merklaafe.diarymongo.database.ImageToUploadDao
import eu.merklaafe.diarymongo.database.entity.ImageToDelete
import eu.merklaafe.diarymongo.database.entity.ImageToUpload
import eu.merklaafe.diarymongo.reposirtory.MongoDb
import eu.merklaafe.diaryui.GalleryImage
import eu.merklaafe.diaryui.GalleryState
import eu.merklaafe.diaryutil.Constants.WRITE_SCREEN_ARGUMENT_KEY
import eu.merklaafe.diaryutil.fetchImagesFromFirebase
import eu.merklaafe.diaryutil.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
internal class WriteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val imageToUploadDao: ImageToUploadDao,
    private val imageToDeleteDao: ImageToDeleteDao
): ViewModel() {
    val galleryState = GalleryState()

    var uiState by mutableStateOf(UiState())
        private set

    init {
        getDiaryIdArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryIdArgument() {
        uiState = uiState.copy(
            selectedDiaryId = savedStateHandle.get<String>(
                key = WRITE_SCREEN_ARGUMENT_KEY
            )
        )
    }

    private fun fetchSelectedDiary() {
        if(uiState.selectedDiaryId != null) {
            viewModelScope.launch {
                MongoDb.getSelectedDiary(
                    diaryId = ObjectId.invoke(uiState.selectedDiaryId!!)
                ).collect { diary ->
                    if(diary is RequestState.Success) {
                        setSelectedDiary(diary = diary.data)
                        setTitle(title = diary.data.title)
                        setDescription(description = diary.data.description)
                        setMood(mood = Mood.valueOf(diary.data.mood))

                        fetchImagesFromFirebase(
                            remoteImagePaths = diary.data.images,
                            onImageDownload = { downloadedImage ->
                                galleryState.addImage(
                                    GalleryImage(
                                        image = downloadedImage,
                                        remoteImagePath = extractImagePath(
                                            fullImageUrl = downloadedImage.toString()
                                        ),
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    private fun setSelectedDiary(diary: Diary) {
        uiState = uiState.copy(selectedDiary = diary)
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

    fun updateDateTime(zonedDateTime: ZonedDateTime) {
        uiState = uiState.copy(updatedDateTime = zonedDateTime.toInstant().toRealmInstant())
    }

    fun upsertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedDiaryId != null) {
                updateDiary(
                    diary = diary,
                    onSuccess = onSuccess,
                    onError = onError
                )
            } else {
                insertDiary(
                    diary = diary,
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
        }
    }

    private suspend fun insertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
     ) {
        val result = MongoDb.insertDiary(diary = diary.apply {
            if(uiState.updatedDateTime != null) {
                date = uiState.updatedDateTime!!
            }
        })
        if(result is RequestState.Success) {
            uploadImagesToFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if(result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    private suspend fun updateDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = MongoDb.updateDiary(diary = diary.apply {
            _id = ObjectId.invoke(uiState.selectedDiaryId!!)
            date = if(uiState.updatedDateTime != null) {
                uiState.updatedDateTime!!
            } else {
                uiState.selectedDiary!!.date
            }
        })
        if(result is RequestState.Success) {
            uploadImagesToFirebase()
            deleteImagesFromFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if(result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    fun deleteDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = MongoDb.deleteDiary(diary = diary)
            if(result is RequestState.Success) {
                withContext(Dispatchers.Main) {
                    uiState.selectedDiary?.let { deleteImagesFromFirebase(images = it.images) }
                    onSuccess()
                }
            } else if(result is RequestState.Error) {
                withContext(Dispatchers.Main) {
                    onError(result.error.message.toString())
                }
            }
        }
    }

    fun addImage(image: Uri, imageType: String) {
        val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                "${image.lastPathSegment}-${System.currentTimeMillis()}.$imageType"
        Log.d("WriteViewModel", remoteImagePath)
        galleryState.addImage(
            GalleryImage(
                image = image,
                remoteImagePath = remoteImagePath
            )
        )
    }

    private fun uploadImagesToFirebase() {
        var imageToUpload: ImageToUpload? = null
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach { galleryImage ->
            val imagePath = storage.child(galleryImage.remoteImagePath)
            imagePath.putFile(galleryImage.image)
                .addOnProgressListener {
                    val sessionUri = it.uploadSessionUri?: ""
                    imageToUpload = ImageToUpload(
                        remoteImagePath = galleryImage.remoteImagePath,
                        imageUri = galleryImage.image.toString(),
                        sessionUri = sessionUri.toString()
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        imageToUploadDao.addImageToUpload(imageToUpload!!)
                    }
                    Log.d("WriteViewModel","Image added to upload ${galleryImage.image}")
                }
                .addOnSuccessListener {
                    if(imageToUpload != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToUploadDao.cleanupImage(imageToUpload!!.id)
                        }
                        Log.d("WriteViewModel","Image removed from upload ${galleryImage.image}")
                    }
                }
        }
    }

    private fun extractImagePath(fullImageUrl: String): String {
        val chunks = fullImageUrl.split("%2F")
        val imageName = chunks[2].split("?").first()
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"
    }

    private fun deleteImagesFromFirebase(images: List<String>? = null) {
        val storage = FirebaseStorage.getInstance().reference
        if(images != null) {
            images.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao.addImageToDelete(
                                ImageToDelete(remoteImagePath = remotePath)
                            )
                        }
                    }
            }
        } else {
            galleryState.imagesToBeDeleted.map { it.remoteImagePath }.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao.addImageToDelete(
                                ImageToDelete(remoteImagePath = remotePath)
                            )
                        }
                    }
            }
        }
    }

}

internal data class UiState(
    val selectedDiaryId: String? = null,
    val selectedDiary: Diary? = null,
    val title: String = "",
    val description: String = "",
    var mood: Mood = Mood.Neutral,
    val updatedDateTime: RealmInstant? = null
)