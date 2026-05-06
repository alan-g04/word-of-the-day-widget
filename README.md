# Word of the Day Widget

An Android widget demo that fetches the daily word from the Wordnik API and displays it using Android Glance and Jetpack Compose.

## Overview

This repository contains a lightweight Android application focused on a home screen widget:

- Retrieves the **Word of the Day** from the Wordnik REST API
- Displays word, part of speech, definition, and etymology
- Supports a **manual refresh** action from the widget
- Plays pronunciation audio when available
- Uses **Glance App Widgets** with **Jetpack Compose for Widgets**
- Uses **WorkManager** for background updates and safe network handling

## Features

- Daily word synchronisation via a background worker
- Instant refresh using a widget action button
- Audio pronunciation playback through a broadcast receiver
- Offline state handling with a friendly message when the network fails
- Built with Kotlin, Compose, Retrofit, Moshi, WorkManager, and Glance

## Project Structure

- `app/` — Android application module
- `app/src/main/java/com/example/wordwidget/` — widget implementation and Wordnik integration
- `app/build.gradle.kts` — module Gradle configuration
- `settings.gradle.kts` — project settings
- `gradle/` — version management and Gradle wrapper

## Requirements

- Android Studio or compatible IDE
- Java 17
- Android SDK 34
- Gradle 8.8 or compatible wrapper
- A valid **Wordnik API key**

## Setup

1. Clone the repository.
2. Open the project in Android Studio.
3. Create or update `local.properties` in the project root with your Wordnik API key:

```properties
WORDNIK_API_KEY=your_wordnik_api_key_here
```

4. Sync Gradle and build the project.

## How It Works

- `DailyWordWorker` fetches the daily word from Wordnik and writes widget state using Glance preferences.
- `WordWidget` renders the widget UI from the stored preference state.
- `WordWidgetReceiver` listens for a manual refresh broadcast and performs an immediate network fetch.
- `AudioReceiver` receives audio playback requests and uses `MediaPlayer` to stream pronunciation audio.

## Build & Run

### Using Android Studio

1. Open the project.
2. Let Gradle sync.
3. Build and install the app on a device or emulator with API level 26+.
4. Add the widget to the home screen.

### Using Gradle wrapper

```bash
gradlew assembleDebug
gradlew installDebug
```

> The widget requires a device or emulator that supports App Widgets and Android API level 26 or higher.

## Dependencies

- `androidx.compose:compose-bom`
- `androidx.glance:glance-appwidget`
- `androidx.work:work-runtime-ktx`
- `com.squareup.retrofit2:retrofit`
- `com.squareup.retrofit2:converter-moshi`
- `com.squareup.moshi:moshi-kotlin`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`

## Notes

- The API key is injected into `BuildConfig` through `local.properties` in `app/build.gradle.kts`.
- The widget is designed to update state for all active widget instances.
- If audio is not available for a word, the widget still shows the word and definition.
