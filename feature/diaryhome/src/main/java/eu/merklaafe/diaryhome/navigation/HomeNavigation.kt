package eu.merklaafe.diaryhome.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import eu.merklaafe.diaryhome.HomeScreen
import eu.merklaafe.diaryhome.HomeScreenViewModel
import eu.merklaafe.diaryui.components.DisplayAlertDialog
import eu.merklaafe.diaryutil.Constants.APP_ID
import eu.merklaafe.diaryutil.Screen
import eu.merklaafe.diaryutil.model.RequestState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    navigateToAuthentication: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(route = Screen.Home.route) {
        val viewModel: HomeScreenViewModel = hiltViewModel()
        val diaries by viewModel.diaries
        val context = LocalContext.current
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpened by remember{ mutableStateOf(false)}
        var deleteAllDialogOpened by remember{ mutableStateOf(false)}
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = diaries) {
            if(diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }

        HomeScreen(
            diaries = diaries,
            drawerState = drawerState,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            onSignOutClicked = {
                signOutDialogOpened = true
            },
            onDeleteAllClicked = {
                deleteAllDialogOpened = true
            },
            navigateToWrite = navigateToWrite,
            navigateToWriteWithArgs = navigateToWriteWithArgs,
            dateIsSelected = viewModel.dateIsSelected,
            onDateSelected = {
                viewModel.getDiaries(zonedDateTime = it)
            },
            onDateReset = {
                viewModel.getDiaries()
            }
        )
        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to Sign Out from your Google Account?",
            dialogOpened = signOutDialogOpened,
            onClosedDialog = {
                signOutDialogOpened = false
            },
            onYesClicked = {
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            navigateToAuthentication()
                        }
                    }
                }
            }
        )
        DisplayAlertDialog(
            title = "Delete All Diaries",
            message = "Are you sure you want to permanently delete all your diaries?",
            dialogOpened = deleteAllDialogOpened,
            onClosedDialog = {
                deleteAllDialogOpened = false
            },
            onYesClicked = {
                viewModel.deleteAllDiaries(
                    onSuccess = {
                        android.widget.Toast.makeText(context,"All diaries deleted.",
                            android.widget.Toast.LENGTH_LONG).show()
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onError = {
                        android.widget.Toast.makeText(
                            context,
                            it.message,
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        )
    }
}