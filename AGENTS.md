# AGENTS.md

## Проект
`Цветной Квартал` — offline-first Android-головоломка для русскоязычной casual-аудитории. Игрок перекрашивает связанный участок поля от левого верхнего угла и должен собрать весь квартал в один цвет за лимит ходов.

## Стек
- Kotlin
- Android Gradle Plugin
- Jetpack Compose + Material 3
- SharedPreferences для локального прогресса и настроек
- JUnit для unit-тестов игровой логики

## Архитектура
- `app/src/main/java/ru/cisgame/colorquarter/data` — модели, каталог уровней, локальное хранилище.
- `app/src/main/java/ru/cisgame/colorquarter/game` — чистая игровая логика.
- `app/src/main/java/ru/cisgame/colorquarter/ui` — Compose UI и навигация экранов.
- `app/src/main/java/ru/cisgame/colorquarter/ui/theme` — цвета, типографика, тема.
- `app/src/main/res` — строки, темы, иконки, backup/data extraction rules.
- `docs` — продуктовая, QA, release и Google Play документация.
- `scripts` — release validation и packaging automation.

## Команды
Перед командами на этой машине:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

Установка зависимостей выполняется Gradle wrapper автоматически:

```bash
./gradlew clean
./gradlew test
./gradlew assembleDebug
./gradlew lint
./gradlew bundleRelease
scripts/validate_release_candidate.sh
scripts/validate_release_candidate.sh --normalize-only
scripts/package_release_candidate.sh 1.0.0
scripts/verify_release_candidate_package.sh 1.0.0
```

`./gradlew :app:installDebug` разрешён только после проверки отдельного эмулятора проекта и только с явным `ANDROID_SERIAL=<device_id>` для AVD `project_52game_emulator`.

Для чистого validation-прогона без переиспользования Gradle daemon/configuration cache:

```bash
GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache" scripts/validate_release_candidate.sh
```

Production signing:
- Реальные `keystore.properties`, `.jks`, `.keystore`, `.p12`, `.pfx` не коммитить.
- Binary upload keystore files должны храниться вне project tree.
- Пример лежит в `keystore.properties.example`.
- Инструкция лежит в `docs/signing_guide.md`.
- Если `keystore.properties` отсутствует, `bundleRelease` собирает unsigned artifact для локальной проверки.
- `scripts/package_release_candidate.sh <version>` должен получать версию, совпадающую с release `BuildConfig.VERSION_NAME`; скрипт валидирует это перед упаковкой.
- `release_manifest.json` в RC pack должен содержать размер и SHA-256 для критичных handoff файлов: AAB, Play icon, feature graphic, phone screenshots, privacy policy HTML и Play Console submission copy. Packaging обязан валидировать эти записи перед созданием zip.
- `scripts/verify_release_candidate_package.sh <version>` должен проходить после упаковки; он независимо проверяет zip checksum, unzip integrity, internal checksums, manifest critical hashes, Play asset inventory и отсутствие local path leakage в RC handoff.

## Android Emulator Isolation
Жёсткое правило: один проект = один отдельный Android Emulator / AVD.

Для этого проекта закреплён AVD:

```text
project_52game_emulator
```

Package names:
- Release: `ru.cisgame.colorquarter`
- Debug: `ru.cisgame.colorquarter.debug`

Перед любым запуском приложения, `adb install`, `adb shell`, `./gradlew :app:installDebug`, снятием screenshots, UI dump или очисткой данных необходимо проверить:
- текущий workspace: `/Users/andrejivliev/Documents/52game`;
- package name текущего приложения;
- список запущенных устройств через `adb devices`;
- список AVD через `emulator -list-avds`;
- что выбранный device id относится именно к AVD `project_52game_emulator`;
- что выбранный эмулятор не используется другим проектом.

Если `project_52game_emulator` не существует, нужно создать отдельный AVD для этого проекта или остановиться и сообщить, что запуск невозможен без отдельного эмулятора.

Если запущено больше одного устройства/эмулятора, любая ADB-команда должна использовать явный serial:

```bash
adb -s <project_52game_device_id> install app/build/outputs/apk/debug/app-debug.apk
ANDROID_SERIAL=<project_52game_device_id> ./gradlew :app:installDebug
adb -s <project_52game_device_id> shell am start -n ru.cisgame.colorquarter.debug/ru.cisgame.colorquarter.MainActivity
```

Запрещено:
- запускать приложение на первом найденном эмуляторе;
- использовать общий или уже открытый эмулятор без доказанной привязки к `project_52game_emulator`;
- устанавливать APK этого проекта на чужой AVD;
- выполнять `pm clear`, менять настройки, перезапускать или закрывать эмуляторы других проектов;
- выполнять `adb kill-server`, если это может повлиять на активные проекты;
- использовать `emulator-5554` или любой другой serial без проверки, что это именно `project_52game_emulator`.

Исторические QA-артефакты, где указан `emulator-5554` или shared AVD, не являются разрешением переиспользовать этот эмулятор. Для всех будущих запусков действует только политика `project_52game_emulator`.

## Правила кода
- Логику игры держать в `game`, без зависимости от Android UI.
- Уровни добавлять через `LevelCatalog`; каждый уровень должен проходить `GameEngineTest` и `LevelCatalogTest`.
- Не добавлять backend, аккаунты, аналитику, рекламу, IAP или permissions без отдельного продуктового решения.
- Не добавлять новые Gradle plugins/repositories/dependencies без обновления `docs/dependency_audit.md` и release validation allowlist.
- Не хранить ключи, keystore, токены и приватные URL в репозитории.
- Пользовательские строки должны быть на русском; фиксированные UI-строки — в `res/values/strings.xml`.
- Динамические UI labels и accessibility descriptions форматировать через string resources, а не собирать русские фразы в Compose-коде.

## UI/UX
- Android-first, короткие сессии, крупные tap targets.
- Радиусы карточек и кнопок — 8dp.
- Пользователь всегда должен видеть текущий уровень, ходы, лимит и следующий очевидный шаг.
- Результат уровня должен быть виден без обязательной прокрутки.
- Все важные действия должны иметь content description или понятный текст.

## Ассеты
- Только собственные vector drawable / generated-by-prompt concepts.
- Не использовать бренды, известных персонажей, чужие UI и copyrighted content.
- Иконка, splash и декоративные элементы должны поддерживать единый мир цветных городских плиток.
- Uploadable Play graphics в `play-assets/graphics` должны быть только PNG; слабые или устаревшие store creatives переносить в `archive/rejected-assets` и документировать в `docs/rejected_assets.md`.
- Phone screenshots для Play должны быть 1080x2400 8-bit RGB PNG без alpha channel; `contact_sheet.png` только для QA preview.
- Play preview asset alt text хранить в `play-assets/metadata/ru-RU/asset_alt_text.md`; для каждого uploadable graphic/screenshot должно быть описание до 140 символов.
- Performance budgets v1.0: release AAB <= 8 MiB, debug APK <= 25 MiB, in-app WebP total <= 256 KiB, one in-app WebP <= 96 KiB, no audio/video/custom font resources in `app/src/main/res`.

## Документация
При изменении продукта обновлять:
- `README.md`
- `docs/product_spec.md`
- `docs/qa_test_plan.md`
- `docs/release_report.md`
- `docs/design_review_2026-05-24.md`, если меняются выводы текущего design/layout/code review.
- `docs/google_play_checklist.md`, если меняются Play Console параметры.
- `docs/build_environment.md`, если меняются Gradle/JDK/SDK/toolchain параметры или validation commands.
- `docs/play_requirements_audit.md`, если меняются Play target API, AAB, manifest или permissions параметры.
- `docs/security_audit.md`, если меняются permissions, dependencies, signing, networking, SDKs или release artifacts.
- `docs/dependency_audit.md`, если меняются Gradle plugins, repositories или dependencies.
- `docs/third_party_notices.md`, если меняются runtime/build/test dependency families или license posture.
- `docs/play_console_runbook.md`, если меняется ручной процесс публикации.
- `docs/signing_guide.md`, если меняется release signing flow.
- `docs/rejected_assets.md`, если ассет убран из release path или признан неготовым к публикации.

## Definition of Done
1. Продукт можно запустить.
2. MVP-идея закончена и задокументирована.
3. UI выглядит как commercial mobile product.
4. Нет placeholder-контента.
5. Основные сценарии работают.
6. Ассеты консистентны.
7. Код аккуратный и поддерживаемый.
8. Debug/release проверки выполнены насколько позволяет среда.
9. Google Play checklist создан.
10. Release report создан.
