import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.collageimage"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.collageimage"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.2")
    implementation ("com.github.hypersoftdev:PuzzleLayout:1.0.2")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")
    implementation("com.otaliastudios:zoomlayout:1.9.0")
    implementation ("com.tbuonomo:dotsindicator:5.0")

    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.intuit.ssp:ssp-android:1.1.1")

    



}