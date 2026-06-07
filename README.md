# DayXDay

A simple Android calendar app for tracking custom data points on each day and viewing trends over months.

## Features

- **Calendar view** — Browse months and tap any day to add or review entries
- **Data types** — Create custom trackers (number, text, or yes/no) with optional units and colors
- **Stats view** — Monthly summaries (averages, ranges, counts) and line charts over the last 6 months
- **Local storage** — All data stays on device via Room database

## Build the APK

Requirements: JDK 17+, Android SDK (API 34)

```bash
export ANDROID_HOME=$HOME/android-sdk
./gradlew assembleDebug
```

The debug APK will be at:

`app/build/outputs/apk/debug/app-debug.apk`

A pre-built APK is also available at:

`releases/dayxday-v1.0-debug.apk`

## Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Usage

1. Open the **Types** tab and create data types (e.g. Weight, Mood, Sleep hours)
2. On the **Calendar** tab, tap a day and use **+** to log values
3. On the **Stats** tab, review monthly summaries and long-term trends
