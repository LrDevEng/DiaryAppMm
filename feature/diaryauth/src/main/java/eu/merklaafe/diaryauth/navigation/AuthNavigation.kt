package eu.merklaafe.diaryauth.navigation

import androidx.compose.runtime.LaunchedEffect
import eu.merklaafe.diaryauth.AuthenticationScreenViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import eu.merklaafe.diaryauth.AuthenticationScreen
import eu.merklaafe.diaryutil.Screen

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

        LaunchedEffect(key1 = kotlin.Unit) {
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