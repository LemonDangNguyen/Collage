plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.google.services)
 //   alias(libs.plugins.kotlin.kapt)
    id("kotlin-kapt")
}


android {
    namespace = "com.example.collageimage"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.collageimage"
        minSdk = 27
        targetSdk = 34
        versionCode = 100
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    flavorDimensions += "default"
    productFlavors {
        create("develop") {
            buildConfigField("Long", "Minimum_Fetch", "5L")
        }

        create("production") {
            buildConfigField("Long", "Minimum_Fetch", "3600L")
        }
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    // Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.ktx)

    // Image loading and editing
    implementation("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")
    implementation(libs.puzzlelayout)

    // Material design
    implementation("com.google.android.material:material:1.12.0")

    // Zoom layout and indicators
    implementation("com.otaliastudios:zoomlayout:1.9.0")
    implementation("com.tbuonomo:dotsindicator:5.0")

    // Size utilities
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    // CameraX dependencies
    implementation("androidx.camera:camera-core:1.2.2")
    implementation("androidx.camera:camera-camera2:1.2.2")
    implementation("androidx.camera:camera-lifecycle:1.2.2")
    implementation("androidx.camera:camera-video:1.2.2")
    implementation("androidx.camera:camera-view:1.2.2")
    implementation("androidx.camera:camera-extensions:1.2.2")

    // CameraView
    implementation("com.otaliastudios:cameraview:2.7.1")

    // ImageView utilities
    implementation ("com.airbnb.android:lottie:6.4.0")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Color picker
    implementation("com.github.yukuku:ambilwarna:2.0.1")

    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation("android.arch.lifecycle:extensions:1.1.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt ("com.google.dagger:hilt-compiler:2.51.1")

    //service
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.2.3"))

    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Add the dependencies for the Remote Config
    implementation("com.google.firebase:firebase-config-ktx")

    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:app-update:2.1.0")

    implementation("com.google.android.gms:play-services-ads:23.2.0")

    implementation(libs.ads)

    implementation("com.facebook.android:facebook-android-sdk:18.0.0")
    implementation("com.google.ads.mediation:facebook:6.18.0.0")
    implementation("com.facebook.infer.annotation:infer-annotation:0.18.0")
    implementation("com.google.guava:guava:27.0.1-android")

    implementation ("com.google.ads.mediation:vungle:7.0.0.1")
    implementation ("com.google.ads.mediation:ironsource:7.5.2.0")
    implementation ("com.google.ads.mediation:applovin:11.11.3.0")
    implementation ("com.google.ads.mediation:mintegral:16.5.41.0")
    implementation ("com.google.ads.mediation:pangle:5.5.0.7.0")
    implementation  ("com.unity3d.ads:unity-ads:4.9.1")
    implementation  ("com.google.ads.mediation:unity:4.9.1.0")
}