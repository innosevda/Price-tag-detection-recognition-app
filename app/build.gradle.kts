plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.sdp_i"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sdp_i"
        minSdk = 24
        //noinspection OldTargetApi
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
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.camera.lifecycle)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Manually added dependencies

    // Google Drive API
    implementation(libs.google.api.client.v1250)
    implementation(libs.google.api.client.android)
    implementation(libs.play.services.auth)
    implementation(libs.google.api.services.drive.vv3rev1971250)

    // CameraX (if using instead of Camera2)
    implementation(libs.camera.core)
    implementation(libs.camera.view)
    implementation(libs.camera.lifecycle)

    implementation(libs.google.http.client.android)
    implementation(libs.google.http.client.jackson2)

    // Fast API
    implementation(libs.okhttp)

}