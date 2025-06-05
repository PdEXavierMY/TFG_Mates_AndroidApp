import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties()
if (secretsFile.exists()) {
    secrets.load(secretsFile.inputStream())
}

android {
    namespace = "com.example.tfgmates"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tfgmates"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "TENANT_ID", "\"${secrets["TENANT_ID"]}\"")
        buildConfigField("String", "CLIENT_ID", "\"${secrets["CLIENT_ID"]}\"")
        buildConfigField("String", "CLIENT_SECRET", "\"${secrets["CLIENT_SECRET"]}\"")
        buildConfigField("String", "API_CLIENT_ID", "\"${secrets["API_CLIENT_ID"]}\"")
        buildConfigField("String", "API_URL", "\"${secrets["API_URL"]}\"")
        buildConfigField("String", "FLOW_GPT_URL", "\"${secrets["FLOW_GPT_URL"]}\"")
        buildConfigField("String", "FLOW_REPORT_URL", "\"${secrets["FLOW_REPORT_URL"]}\"")
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

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    //Http
    implementation(libs.okhttp)
    //Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    //Vosk
    implementation(project(":models"))
    implementation(libs.alphacephei.vosk.android)
    implementation("net.java.dev.jna:jna:5.17.0@aar")
    //Imagenes
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    //Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}