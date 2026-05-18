plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "haptikos.gestortareashogar_haptikos"
    compileSdk = 36

    defaultConfig {
        applicationId = "haptikos.gestortareashogar_haptikos"
        minSdk = 27
        targetSdk = 36
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.ui.graphics)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.gson)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.remote.creation.core)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)
    implementation(libs.identity.jvm)
    implementation(libs.runtime)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains:annotations:23.0.0")
    }
    exclude(group = "com.intellij", module = "annotations")
}