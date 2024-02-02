package eu.merklaafe.diarywrite

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import eu.merklaafe.diaryutil.model.Diary
import eu.merklaafe.diaryui.GalleryImage
import eu.merklaafe.diaryui.GalleryState
import eu.merklaafe.diaryutil.model.Mood
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun WriteScreen(
    uiState: UiState,
    moodName: () -> String,
    pagerState: PagerState,
    galleryState: GalleryState,
    onDataLoaded: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDeleteConfirmed: (Diary) -> Unit,
    onDateTimeUpdated: (ZonedDateTime) -> Unit,
    onBackPressed: () -> Unit,
    onSaveClicked: (Diary) -> Unit,
    onImageSelect: (Uri) -> Unit,
    onImageDeleteClicked: (GalleryImage) -> Unit,
) {
    var selectedGalleryImage by remember { mutableStateOf<GalleryImage?>(null) }
    val pagerStateImage = rememberPagerState { galleryState.images.size }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        onDataLoaded()
    }

    LaunchedEffect(key1 = uiState.mood) {
        pagerState.scrollToPage(Mood.valueOf(uiState.mood.name).ordinal)
    }

    Scaffold(
        topBar = {
            WriteTopBar(
                selectedDiary = uiState.selectedDiary,
                moodName = moodName,
                onDeleteConfirmed = onDeleteConfirmed,
                onBackPressed = onBackPressed,
                onDateTimeUpdated = onDateTimeUpdated
            )
        },
        content = { paddingValues ->
            WriteContent(
                uiState = uiState,
                paddingValues = paddingValues,
                galleryState = galleryState,
                pagerState = pagerState,
                title = uiState.title,
                onTitleChanged = onTitleChanged,
                description = uiState.description,
                onDescriptionChanged = onDescriptionChanged,
                onSaveClicked = onSaveClicked,
                onImageSelect = onImageSelect,
                onImageClicked = {
                    selectedGalleryImage = it
                    scope.launch {
                        pagerStateImage.scrollToPage(galleryState.images.indexOf(it))
                    }
                }
            )

            AnimatedVisibility(
                visible = selectedGalleryImage != null,
                enter = EnterTransition.None,
                exit = ExitTransition.None
            ) {
                Dialog(
                    onDismissRequest = {
                        selectedGalleryImage = null
                    }
                ) {
                    if(selectedGalleryImage != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(vertical = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(onClick = { selectedGalleryImage = null }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Icon"
                                    )
                                    Text(text = "Close")
                                }
                                Button(onClick = {
                                    if (selectedGalleryImage != null) {
                                        onImageDeleteClicked(selectedGalleryImage!!)
                                        selectedGalleryImage = null
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Icon"
                                    )
                                    Text(text = "Delete")
                                }
                            }

                            HorizontalPager(
                                state = pagerStateImage,
                            ) { page ->
                                selectedGalleryImage = galleryState.images[page]

                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(selectedGalleryImage!!.image.toString())
                                        .crossfade(true)
                                        .build(),
                                    contentScale = ContentScale.Fit,
                                    contentDescription = "Gallery Image"
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
internal fun ZoomableImage(
    selectedGalleryImage: GalleryImage,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = maxOf(1f, minOf(scale * zoom, 5f))
                    val maxX = (size.width * (scale - 1)) / 2
                    val minX = -maxX
                    offsetX = maxOf(minX, minOf(maxX, offsetX + pan.x))
                    val maxY = (size.height * (scale - 1)) / 2
                    val minY = -maxY
                    offsetY = maxOf(minY, minOf(maxY, offsetY + pan.y))
                }
            }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(.5f, minOf(3f, scale)),
                    scaleY = maxOf(.5f, minOf(3f, scale)),
                    translationX = offsetX,
                    translationY = offsetY
                )
            ,
            model = ImageRequest.Builder(LocalContext.current)
                .data(selectedGalleryImage.image.toString())
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Fit,
            contentDescription = "Gallery Image"
        )
    }
}