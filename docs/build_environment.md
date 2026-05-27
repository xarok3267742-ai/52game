# Build Environment

Дата проверки: 2026-05-23

## Purpose
Этот документ фиксирует toolchain, с которым собран и проверен release candidate `Цветной Квартал` 1.0.0. Он нужен для воспроизводимой локальной сборки, handoff и диагностики Play Console / pre-launch report issues.

## Validated Host Environment
| Area | Value |
|---|---|
| Host OS used for RC validation | macOS 15.7.4, arm64 |
| JDK used by Gradle launcher | JetBrains Runtime / OpenJDK 21.0.5 |
| Java source compatibility | `JavaVersion.VERSION_17` |
| Java target compatibility | `JavaVersion.VERSION_17` |
| Kotlin JVM target | `JvmTarget.JVM_17` |

The app is compiled for JVM 17 bytecode compatibility even though the local Gradle launcher runs on JBR 21.

## Android / Gradle Toolchain
| Tool | Version / setting |
|---|---|
| Gradle wrapper | Gradle 8.13 |
| Gradle wrapper distribution | `https://services.gradle.org/distributions/gradle-8.13-bin.zip` |
| Gradle wrapper SHA-256 | `20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78` |
| Android Gradle Plugin | 8.13.2 |
| Kotlin Android plugin | 2.2.20 |
| Kotlin Compose compiler plugin | 2.2.20 |
| Compose BOM | 2026.04.01 |
| Android command-line tools | 20.0 |
| Android emulator used in latest QA evidence | 35.4.9.0 |
| ADB used in latest QA evidence | 35.0.2 / 1.0.41 |

## Android SDK Posture
| Setting | Value |
|---|---|
| `compileSdk` | 36 |
| `targetSdk` | 35 |
| `minSdk` | 23 |
| Installed SDK platforms observed | `android-34`, `android-35`, `android-36` |
| Installed build tools observed | `33.0.1`, `34.0.0`, `35.0.0`, `35.0.1`, `36.0.0` |

Google Play target API requirements were checked separately in `docs/play_requirements_audit.md`.

## Environment Setup
Use Android Studio's bundled JBR and the local Android SDK:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

The project intentionally does not commit `local.properties`. Gradle can generate/use it locally, but release validation and RC packaging reject `local.properties`, `sdk.dir` assignments and absolute machine-specific user paths in release handoff surfaces.

## Reproducible Validation Commands
```bash
./gradlew --version --no-daemon
./gradlew --no-daemon --no-configuration-cache --console=plain :app:dependencies --configuration releaseRuntimeClasspath
GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache --console=plain" scripts/validate_release_candidate.sh
scripts/package_release_candidate.sh 1.0.0
scripts/verify_release_candidate_package.sh 1.0.0
```

## Emulator Rule
Runtime QA must use only the dedicated project AVD:

```text
project_52game_emulator
```

If this AVD is unavailable, create it or skip emulator launch. Do not install or run this project on a shared or foreign emulator.

## Maintenance Rule
Update this document when any of these change:
- Gradle wrapper or Android Gradle Plugin;
- Kotlin or Compose plugin/BOM versions;
- Java/JDK compatibility;
- `compileSdk`, `targetSdk`, `minSdk`;
- Android SDK command-line tools, emulator or ADB version used for QA evidence;
- release validation commands or emulator policy.
