# e-typewriter

A distraction-free Markdown editor for Android with an electric typewriter aesthetic. Designed for deep focus writing on E-ink displays (Onyx Boox Note Air 2 Plus) and foldable phones (Samsung Galaxy Z Fold 5).

## Features (Phase 1 & 2 -- Completed)

- **Typewriter Aesthetic:**
  - Full-screen Markdown (`.md`) editor with no visual clutter
  - Cream background (`#F5F5DC`), black monospace text, zero borders or shadows
  - Visual paper sheet centered with infinite scroll
  - Automatic line wrap at 70 characters
  - Fixed cursor line (typewriter scrolling)
- **Sensory Experience:**
  - Background hum of an electric typewriter on loop
  - Click-clack keystroke sounds and a bell at the line margin (63 chars)
  - Short haptic feedback per keypress
- **Core Functionality:**
  - Create, open and save `.md` files via Android Storage Access Framework
  - Live status bar: line count, word count, character count
  - Edge-to-edge layout with Material 3

## Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| Build | Gradle 8.11 (Kotlin DSL) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

## Project Structure

```
e-typewriter/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── raw/    (Sound assets)
│       │   └── values/ (Strings / Themes)
│       └── java/com/etypewriter/ahc/
│           ├── MainActivity.kt
│           ├── data/
│           │   └── FileManager.kt
│           ├── util/
│           │   └── SoundManager.kt
│           └── ui/
│               ├── theme/  (Color / Type / Theme)
│               └── editor/ (EditorScreen / EditorViewModel)
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── local.properties
```

## Prerequisites

- **Java 17** -- e.g. `brew install openjdk@17`
- **Android SDK** with platform 35 and build-tools 35.0.0:
  ```bash
  brew install --cask android-commandlinetools
  yes | sdkmanager --sdk_root=/opt/homebrew/share/android-commandlinetools \
      "platform-tools" "platforms;android-35" "build-tools;35.0.0"
  ```
- **Sound Assets:** Replace placeholder files in `app/src/main/res/raw/` with real audio files:
  - `typewriter_hum.mp3`
  - `typewriter_key.mp3`
  - `typewriter_return.mp3`
  - `typewriter_bell.mp3`

## Build

```bash
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools

# If your global ~/.gradle/gradle.properties overrides org.gradle.java.home,
# pass it explicitly:
./gradlew assembleDebug \
    -Dorg.gradle.java.home=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home

# Output APK:
# app/build/outputs/apk/debug/app-debug.apk
```

To install on a connected device:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Roadmap

### Phase 3 -- Immersion (Current)
- Immersive mode: hide all system bars (clock, notifications)

### Phase 4 -- Connectivity & Hardware
- **Google Drive sync:** Automatic cloud backup
- **E-ink (Onyx Boox):** Fixed-line mode where text scrolls up and the cursor stays centered; forced screen refresh every 500 characters via Onyx SDK to prevent ghosting
- **Foldable (Z Fold 5):** Fluid layout that adapts between the narrow cover screen and the inner square display
