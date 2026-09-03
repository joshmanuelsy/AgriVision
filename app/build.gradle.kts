plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.pechayimnida"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pechayimnida"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-ui-prototype"
    }

    buildFeatures {
        compose = true
        buildConfig = true
        // ✅ FIX: Disable automatic ML Model Binding (stops the Yolo11n.java error)
        mlModelBinding = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    androidResources {
        noCompress += "tflite"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.google.accompanist.permissions)

    // CameraX dependencies
    val cameraVersion = "1.4.1"
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")
    implementation("androidx.camera:camera-extensions:$cameraVersion")

    // Firebase BoM (Late 2026 Version)
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))
    
    // Firebase AI (Gemini) - Replaces standalone generativeai
    // implementation("com.google.firebase:firebase-ai")

    // Standalone Google AI SDK (Generative AI)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // TensorFlow Lite dependencies
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-gpu-api:2.16.1")

    // ✅ FIX: Adds metadata support (silences the MetadataExtractor error)
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")

    debugImplementation("androidx.compose.ui:ui-tooling")
}