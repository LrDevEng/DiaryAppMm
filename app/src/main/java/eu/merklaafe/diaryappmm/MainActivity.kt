package eu.merklaafe.diaryappmm

import android.content.ContentResolver
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import eu.merklaafe.diaryappmm.data.database.ImageToUploadDao
import eu.merklaafe.diaryappmm.data.reposirtory.MongoDb
import eu.merklaafe.diaryappmm.navigation.Screen
import eu.merklaafe.diaryappmm.navigation.SetupNavGraph
import eu.merklaafe.diaryappmm.ui.theme.DiaryAppMmTheme
import eu.merklaafe.diaryappmm.util.Constants.APP_ID
import eu.merklaafe.diaryappmm.util.retryUploadingImageToFirebase
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageToUploadDao: ImageToUploadDao
    private var keepSplashOpened = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        }
        FirebaseApp.initializeApp(this)
        WindowCompat.setDecorFitsSystemWindows(window,false)
        setContent {
            DiaryAppMmTheme {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController,
                    onDataLoaded = {
                        keepSplashOpened = false
                    }
                )
            }
        }
        cleanupCheck(
            scope = lifecycleScope,
            imageToUploadDao = imageToUploadDao,
        )
    }
}

private fun cleanupCheck(
    scope: CoroutineScope,
    imageToUploadDao: ImageToUploadDao,
) {
    scope.launch(Dispatchers.IO) {
        val result = imageToUploadDao.getAllImages()
        Log.d("MainActivity", result.size.toString())
        result.forEach { imageToUpload ->
            Log.d("MainActivity",imageToUpload.imageUri)
            retryUploadingImageToFirebase(
                imageToUpload = imageToUpload,
                onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imageToUploadDao.cleanupImage(imageId = imageToUpload.id)
                    }
                }
            )
        }
    }
}

private fun getStartDestination(): String {
    val user = App.Companion.create(APP_ID).currentUser
    return if (user != null && user.loggedIn) Screen.Home.route
    else Screen.Authentication.route
}