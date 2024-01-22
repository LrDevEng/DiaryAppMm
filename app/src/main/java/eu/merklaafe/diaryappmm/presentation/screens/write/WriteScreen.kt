package eu.merklaafe.diaryappmm.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import eu.merklaafe.diaryappmm.model.Diary

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    selectedDiary: Diary?,
    pagerState: PagerState,
    onDeleteConfirmed: () -> Unit,
    onBackPressed: () -> Unit,
    navigateToHome: () -> Unit
) {
    Scaffold(
        topBar = {
            WriteTopBar(
                selectedDiary = selectedDiary,
                onDeleteConfirmed = onDeleteConfirmed,
                onBackPressed = onBackPressed
            )
        },
        content = {
            WriteContent(
                paddingValues = it,
                pagerState = pagerState,
                title = "",
                onTitleChanged = {},
                description = "",
                onDescriptionChanged = {}
            )
        }
    )
}