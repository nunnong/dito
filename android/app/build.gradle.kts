plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("io.realm.kotlin") version "3.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.dito.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dito.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "SKIP_AI_INTERVENTION", "true")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("Boolean", "SKIP_AI_INTERVENTION", "false")
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    // ========== Android Core ==========
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ========== Lifecycle ==========
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // ========== Compose ==========
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.04.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ========== Navigation ==========
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ========== Coil (Image Loading) ==========
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ========== Hilt (DI) ==========
    implementation("com.google.dagger:hilt-android:2.57.2")
    implementation(libs.androidx.room.ktx)
    kapt("com.google.dagger:hilt-compiler:2.57.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ========== Hilt WorkManager ==========
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // ========== Retrofit (네트워크) ==========
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // ========== OkHttp ==========
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ========== Kotlinx Serialization ==========
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // ========== Coroutines ==========
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // ========== DataStore ==========
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ========== Realm (로컬 DB) ==========
    implementation("io.realm.kotlin:library-base:3.0.0")

    // ========== WorkManager (백그라운드) ==========
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ========== Firebase ==========
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // ========== Health Connect ==========
    implementation("androidx.health.connect:connect-client:1.1.0-alpha10")

    // ========== Coil (이미지 로딩) ============
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ========== Wearable Data Layer (폰-워치 통신) ==========
    implementation("com.google.android.gms:play-services-wearable:18.2.0")

    // ========== Test ==========
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}