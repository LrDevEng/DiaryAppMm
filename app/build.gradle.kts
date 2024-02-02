plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.realm.kotlin)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
}

android {
    namespace = "eu.merklaafe.diaryappmm"
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        applicationId = ProjectConfig.applicationId
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
        versionCode = ProjectConfig.versionCode
        versionName = ProjectConfig.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = ProjectConfig.kotlinCompilerExtensionVersion
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    implementation(libs.androidx.activity.compose)
//    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.compose.ui)
//    implementation(libs.androidx.compose.ui.graphics)
//    implementation(libs.androidx.compose.ui.tooling.preview)
//    implementation(libs.androidx.compose.material3)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.compose.ui.test.junit)
//    debugImplementation(libs.androidx.compose.ui.tooling)
//    debugImplementation(libs.androidx.compose.ui.test.manifest)
//
//    // Compose Navigation
//    implementation(libs.androidx.navigation.compose)

    // Firebase
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.bundles.firebase)

    // Room components
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

//
//    // Runtime Compose
//    implementation(libs.androidx.lifecycle.runtime.compose)

    // Splash API
    implementation(libs.androidx.core.splashscreen)

    // Mongo DB Realm
    implementation(libs.bundles.mongo.realm)

    // Dagger Hilt
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

//    // Coil
//    implementation(libs.coil.compose)
//
//    // Pager - Accompanist [DEPRECATED]
////    implementation "com.google.accompanist:accompanist-pager:0.27.0"
//
////    // Date-Time Picker
////    implementation("com.maxkeppeler.sheets-compose-dialogs:core:1.0.2")
////
////    // CALENDAR
////    implementation("com.maxkeppeler.sheets-compose-dialogs:calendar:1.0.2")
////
////    // CLOCK
////    implementation("com.maxkeppeler.sheets-compose-dialogs:clock:1.0.2")
//
//    // Message Bar Compose
//    implementation(libs.messageBarCompose)
//
//    // One-Tap Compose
//    implementation(libs.oneTapCompose)
//
//    // Desugar JDK
//    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(project(":core:diaryui"))
    implementation(project(":core:diaryutil"))
    implementation(project(":data:diarymongo"))
    implementation(project(":feature:diaryauth"))
    implementation(project(":feature:diaryhome"))
    implementation(project(":feature:diarywrite"))
}