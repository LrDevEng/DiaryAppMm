package eu.merklaafe.diaryappmm.presentation.screens.write

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import eu.merklaafe.diaryappmm.data.reposirtory.MongoDb
import eu.merklaafe.diaryappmm.model.Diary
import eu.merklaafe.diaryappmm.model.GalleryImage
import eu.merklaafe.diaryappmm.model.GalleryState
import eu.merklaafe.diaryappmm.model.Mood
import eu.merklaafe.diaryappmm.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import eu.merklaafe.diaryappmm.model.RequestState
import eu.merklaafe.diaryappmm.util.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime

class WriteViewModel(
    private val savedStateHandle: SavedStateHandle
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
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach { galleryImage ->
            val imagePath = storage.child(galleryImage.remoteImagePath)
            imagePath.putFile(galleryImage.image)
        }
    }

}

data class UiState(
    val selectedDiaryId: String? = null,
    val selectedDiary: Diary? = null,
    val title: String = "",
    val description: String = "",
    var mood: Mood = Mood.Neutral,
    val updatedDateTime: RealmInstant? = null
)