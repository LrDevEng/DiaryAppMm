package eu.merklaafe.diarywrite.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import eu.merklaafe.diaryutil.Constants.WRITE_SCREEN_ARGUMENT_KEY
import eu.merklaafe.diaryutil.Screen
import eu.merklaafe.diaryutil.model.Mood
import eu.merklaafe.diarywrite.WriteScreen
import eu.merklaafe.diarywrite.WriteViewModel

@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(
    onBackPressed: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val viewModel: WriteViewModel = hiltViewModel()
        val uiState = viewModel.uiState
        val galleryState = viewModel.galleryState
        val pagerState = rememberPagerState{ Mood.entries.size }
        val context = LocalContext.current
        val pageNumber by remember { derivedStateOf { pagerState.currentPage } }

        LaunchedEffect(
            key1 = uiState,
            block = {
                Log.d("SelectedDiary", "${uiState.selectedDiaryId}")
            }
        )

        WriteScreen(
            uiState = uiState,
            moodName = { Mood.entries[pagerState.currentPage].name},
            pagerState = pagerState,
            onDataLoaded = onDataLoaded,
            galleryState = galleryState,
            onTitleChanged = { viewModel.setTitle(title = it) },
            onDescriptionChanged = { viewModel.setDescription(description = it) },
            onDeleteConfirmed = {
                viewModel.deleteDiary(
                    diary = it,
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Deleted diary: ${it.title}",
                            Toast.LENGTH_LONG
                        ).show()
                        onBackPressed()
                    },
                    onError = {msg ->
                        throw Exception(msg)
                    }
                )
            },
            onDateTimeUpdated = { viewModel.updateDateTime(zonedDateTime = it) },
            onBackPressed = onBackPressed,
            onSaveClicked = {
                viewModel.upsertDiary(
                    diary = it.apply { mood = Mood.entries[pagerState.currentPage].name },
                    onSuccess = { onBackPressed() },
                    onError = {msg ->
                        throw Exception(msg)
                    }
                )
            },
            onImageSelect = { uri ->
                Log.d("NavGraph@WriteRoute", uri.toString())
                val type = context.contentResolver.getType(uri)?.split("/")?.last() ?: "jpg"
                viewModel.addImage(
                    image = uri,
                    imageType = type
                )
            },
            onImageDeleteClicked = { galleryState.removeImage(it) }
        )
    }
}