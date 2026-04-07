plugins {
    id("com.android.application") version "8.8.0"
    id("org.jetbrains.kotlin.android") version "2.0.0" 
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" 
}

import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
val wordnikApiKey: String = localProperties.getProperty("WORDNIK_API_KEY") ?: "\"\""

android {
    namespace = "com.example.wordwidget"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wordwidget"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        // Inject key
        buildConfigField("String", "WORDNIK_API_KEY", wordnikApiKey)
    }

    buildFeatures {
        buildConfig = true
        compose = true 
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")

    implementation("androidx.glance:glance-appwidget:1.1.1")
    
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}