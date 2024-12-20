package eu.merklaafe.diaryappmm.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import eu.merklaafe.diaryappmm.model.GalleryImage
import eu.merklaafe.diaryappmm.model.Mood
import eu.merklaafe.diaryappmm.presentation.components.DisplayAlertDialog
import eu.merklaafe.diaryappmm.presentation.screens.authentication.AuthenticationScreen
import eu.merklaafe.diaryappmm.presentation.screens.authentication.AuthenticationScreenViewModel
import eu.merklaafe.diaryappmm.presentation.screens.home.HomeScreen
import eu.merklaafe.diaryappmm.presentation.screens.home.HomeScreenViewModel
import eu.merklaafe.diaryappmm.presentation.screens.write.WriteScreen
import eu.merklaafe.diaryappmm.presentation.screens.write.WriteViewModel
import eu.merklaafe.diaryappmm.util.Constants.APP_ID
import eu.merklaafe.diaryappmm.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import eu.merklaafe.diaryappmm.model.RequestState
import eu.merklaafe.diaryappmm.model.rememberGalleryState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController,
    onDataLoaded: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            },
            onDataLoaded = onDataLoaded
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
            navigateToWriteWithArgs = {
                navController.navigate(Screen.Write.passDiaryId(diaryId = it))
            },
            navigateToAuthentication = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            },
            onDataLoaded = onDataLoaded
        )
        writeRoute(
            onBackPressed = {
                navController.popBackStack()
            },
            onDataLoaded = onDataLoaded
        )
    }
}

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationScreenViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val authenticationState by viewModel.authenticationState
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(key1 = Unit) {
            onDataLoaded()
        }

        AuthenticationScreen(
            loadingState = loadingState,
            authenticationState = authenticationState,
            oneTapState = oneTapState,
            messageBarState = messageBarState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoading(true)
            },
            onSuccessfulFirebaseSignIn = { tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated!")
                        viewModel.setLoading(false)
                    },
                    onError = { e ->
                        messageBarState.addError(e)
                        viewModel.setLoading(false)
                    }
                )
            },
            onFailedFirebaseSignIn = { e ->
                messageBarState.addError(e)
                viewModel.setLoading(false)
            },
            onDialogDismissed = {message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateToHome = {
                navigateToHome()
            }
        )
    }
}

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
                scope.launch(Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(Dispatchers.Main) {
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
                        Toast.makeText(context,"All diaries deleted.",Toast.LENGTH_LONG).show()
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onError = {
                        Toast.makeText(
                            context,
                            it.message,
                            Toast.LENGTH_LONG
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
            moodName = {Mood.entries[pagerState.currentPage].name},
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