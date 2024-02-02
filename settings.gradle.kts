pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "DiaryAppMm"
include(":app")
include(":core:diaryui")
include(":core:diaryutil")
include(":data:diarymongo")
include(":feature:diaryauth")
include(":feature:diaryhome")
include(":feature:diarywrite")
