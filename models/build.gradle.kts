plugins {
    id("com.android.library")
}

android {
    namespace = "com.example.tfgmates.models"
    compileSdk = 35

    defaultConfig {
        minSdk = 33
        testOptions.targetSdk = 35
    }

    buildFeatures {
        buildConfig = false
    }

    sourceSets["main"].assets.srcDirs(layout.buildDirectory.dir("generated/assets"))
}