# Project Setup Guide

## 1. Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | 17 (LTS) | Temurin recommended. Android Studio's embedded JBR 17 also works |
| Android SDK | platform `android-35`, `build-tools;35.0.0`, `platform-tools` | via Android Studio SDK Manager or `sdkmanager` |
| Android Studio | Ladybug (2024.2)+ | anything bundling AGP 8.7 support |
| Git | any recent | |

No global Gradle installation is needed or wanted — the wrapper pins Gradle **8.11.1**.

## 2. Clone & configure

```bash
git clone <repo-url>
cd NEXA/nexa-android
```

Create `local.properties` (never committed) pointing at your SDK:

```properties
sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk   # Windows
# sdk.dir=/Users/<you>/Library/Android/sdk               # macOS
```

Android Studio generates this automatically on first open.

## 3. Command-line setup without Android Studio

```bash
# Install SDK components (after unpacking commandline-tools into $SDK/cmdline-tools/latest):
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

Set `JAVA_HOME` to your JDK 17 and build:

```bash
./gradlew :app:assembleDebug
```

First run downloads the Gradle distribution and dependencies (~1 GB total); subsequent builds are incremental and cached.

## 4. Everyday commands

| Task | Command |
|---|---|
| Debug APKs (gms + nogms) | `./gradlew :app:assembleDebug` |
| Release APKs (R8, debug-signed) | `./gradlew :app:assembleRelease` |
| Unit tests + Konsist law | `./gradlew test testDebugUnitTest :app:testGmsDebugUnitTest` |
| Static analysis + Kotlin style (ktlint via detekt-formatting) | `./gradlew detekt` |
| Non-Kotlin formatting | `./gradlew spotlessCheck` (fix: `spotlessApply`) |
| Android lint | `./gradlew lintDebug :app:lintGmsDebug` |
| Everything CI runs | `./gradlew spotlessCheck detekt test testDebugUnitTest :app:testGmsDebugUnitTest lintDebug :app:lintGmsDebug :app:assembleDebug :app:assembleRelease :benchmark:assembleDebug` |

## 5. IDE settings

- Import as a Gradle project from `nexa-android/`.
- Kotlin code style comes from `.editorconfig` (ktlint `android_studio` style, 120 columns) — no manual IDE style configuration needed.
- Enable "Use project JDK" = 17.

## 6. Troubleshooting

- **`SDK location not found`** — create `local.properties` (step 2). Use forward slashes on Windows (`C:/Users/...`).
- **Configuration-cache problems after editing build logic** — run once with `--no-configuration-cache` or `./gradlew --stop` then retry; the cache re-primes.
- **Formatting findings from detekt** — `./gradlew detekt --auto-correct` fixes everything mechanical; remaining findings are real.
