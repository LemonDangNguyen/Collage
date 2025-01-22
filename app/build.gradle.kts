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

    flavorDimensions("default")
    productFlavors {
        create("develop") {
            buildConfigField("Long", "Minimum_Fetch", "5L")
        }

        create("production") {
            buildConfigField("Long", "Minimum_Fetch", "3600L")
        }
    }
}

dependencies {
    // Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.ktx)
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.config.ktx)
    implementation(libs.app.update.ktx)
    implementation(libs.review.ktx)
    testImplementation(libs.testng)

    // Testing dependencies
    androidTestImplementation(libs.androidx.espresso.core)

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
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Color picker
    implementation("com.github.yukuku:ambilwarna:2.0.1")

    implementation("com.google.android.gms:play-services-ads:23.2.0")
    implementation("com.github.devvtn.vtn_ads_libs:ads:1.9.8")

    implementation ("com.facebook.shimmer:shimmer:0.5.0")

    implementation ("android.arch.lifecycle:extensions:1.1.1")
}
