# Tech Stack Decision

## Выбранный стек
Kotlin + Jetpack Compose + Material 3 в одном Android Gradle модуле.

## Почему
- Репозиторий был пустой, Android Studio и Android SDK доступны локально.
- Kotlin/Compose быстрее всего даёт нативный Android-first UI без WebView и лишних runtime-слоёв.
- Игра 2D и простая: отдельный игровой движок не нужен.
- Compose удобен для responsive mobile UI, а игровая логика вынесена в чистые Kotlin-функции и тестируется JUnit.
- SharedPreferences достаточно для локального прогресса.

## Альтернативы
- Flutter: быстрый UI, но не был существующим стеком, добавляет отдельный toolchain.
- React Native/Expo: усложняет Android release path и не нужен для маленькой игры.
- Godot/Unity: избыточно для сеточной 2D-головоломки.
- Native Views: стабильны, но Compose быстрее для polished UI.

## Команды запуска
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

ANDROID_SERIAL=<project_52game_device_id> ./gradlew :app:installDebug
adb -s <project_52game_device_id> shell am start -n ru.cisgame.colorquarter.debug/ru.cisgame.colorquarter.MainActivity
```

Android-запуск разрешён только на отдельном AVD текущего проекта `project_52game_emulator`. Если этот AVD не найден, запуск нужно остановить до создания отдельного project-specific эмулятора.

## Команды сборки
```bash
./gradlew clean
./gradlew assembleDebug
./gradlew bundleRelease
```

Signed release build использует optional `keystore.properties` в корне проекта. Если файл заполнен по `keystore.properties.example`, `bundleRelease` подпишет AAB upload key; если файла нет, AAB собирается unsigned для локальной проверки.

`BuildConfig` включён явно, чтобы UI мог показывать актуальную версию из Gradle `versionName`.

## Команды тестов и проверок
```bash
./gradlew test
./gradlew lint
scripts/validate_release_candidate.sh
GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache" scripts/validate_release_candidate.sh
```

## Команды release packaging
```bash
scripts/package_release_candidate.sh 1.0.0
```

## Ограничения окружения
- Системный `/usr/bin/java` не установлен; используется JBR из Android Studio.
- Release `.aab` создаётся, но production signing key в репозитории отсутствует намеренно.
- Release signing flow подготовлен через `keystore.properties`; реальные ключи нужно создать и хранить вручную вне репозитория.
