# Цветной Квартал

Offline-first Android-головоломка для русскоязычной casual-аудитории. Игрок выбирает цвета, перекрашивает связанный участок от левого верхнего угла и старается собрать весь квартал в один цвет за лимит ходов. Прогресс и незавершённая попытка сохраняются локально, главный экран показывает next-goal panel, пройденные уровни и общий результат по звёздам, уровень получил ImageGen-баннер, явную строку цели, отдельные кнопки `Отмена`, `Подсказка`, `Заново`, подсказка предлагает полезный цвет вместе с приростом `+N`, поле выделяет захваченный участок, палитра показывает прирост и отключает цвета без прироста, после хода игра показывает присоединённые клетки, а панель темпа подсказывает, какой звёздный результат ещё достижим.

## Почему эта идея
Выбрана простая puzzle-игра: она понятна без длинного обучения, хорошо подходит для коротких Android-сессий, не требует backend, аккаунтов, платежей, персональных данных или опасных permissions. Рыночный анализ зафиксирован в [docs/product_decision.md](docs/product_decision.md).

## Стек
- Kotlin + Jetpack Compose
- Material 3
- SharedPreferences
- Gradle Android project
- JUnit unit-тесты

## Подготовка окружения
На текущей машине используется JBR из Android Studio:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

## Команды
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

Gradle wrapper pinned to `gradle-8.13-bin.zip` with `distributionSha256Sum`; `scripts/validate_release_candidate.sh` verifies the wrapper URL, checksum and wrapper jar contents before release-surface checks.

Android-запуск выполняется только на отдельном AVD этого проекта: `project_52game_emulator`. Перед `installDebug` нужно определить serial именно этого AVD и запускать так:

```bash
ANDROID_SERIAL=<project_52game_device_id> ./gradlew :app:installDebug
adb -s <project_52game_device_id> shell am start -n ru.cisgame.colorquarter.debug/ru.cisgame.colorquarter.MainActivity
```

Запрещено использовать общий или чужой эмулятор, включая уже открытый device id без проверки принадлежности к `project_52game_emulator`.

Если локальная Gradle/ADB-сессия нестабильна, release validation можно запускать изолированно:

```bash
GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache" scripts/validate_release_candidate.sh
```

`scripts/validate_release_candidate.sh --normalize-only` только выносит локальные numbered `app/build/* N` каталоги в карантин за пределами проекта и не запускает Gradle.

`scripts/package_release_candidate.sh 1.0.0` проверяет, что аргумент версии совпадает с release `BuildConfig.VERSION_NAME` и merged manifest versionName. Это защищает RC pack от случайной упаковки AAB другой версии.
RC pack также получает `release_manifest.json` с applicationId, versionName/versionCode, статусом подписи AAB, списком uploadable Play assets, QA/source assets и ручными блокерами перед публикацией.
Packaging validates `release_manifest.json` before creating the zip: release identity, AAB status/path, uploadable graphics/screenshots, required docs, QA evidence pointers and manual Play blockers must be internally consistent.
The manifest also records byte size and SHA-256 for critical handoff files: the AAB, Play icon, feature graphic, six phone screenshots, privacy policy HTML and Play Console submission copy. Packaging fails if those records are missing, malformed or do not match the files on disk.
Release validation и RC packaging дополнительно блокируют попадание локальной Android SDK-конфигурации, actual `local.properties`, `sdk.dir` assignments и machine-specific user paths в AAB или handoff archive.
Release validation также фиксирует performance budget v1.0: release AAB <= 8 MiB, debug APK <= 25 MiB, in-app WebP total <= 256 KiB, один WebP <= 96 KiB, без audio/video/custom font resources в `app/src/main/res`.
Release validation проверяет signing hygiene: `.gitignore` должен закрывать `keystore.properties`, binary keystore formats и `local.properties`; production upload keystore files должны храниться вне project tree.
Release validation закрепляет approved dependency allowlist v1.0: новые Gradle plugins/repositories/dependencies требуют обновить [docs/dependency_audit.md](docs/dependency_audit.md).
`scripts/verify_release_candidate_package.sh 1.0.0` независимо проверяет уже собранный zip: archive checksum, unzip integrity, internal `checksums.sha256`, manifest critical hashes, Play asset inventory и отсутствие local path leakage в RC handoff.

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

Release AAB: `app/build/outputs/bundle/release/app-release.aab`.

Важно: production upload key и приватный `keystore.properties` не коммитятся. В текущем локальном handoff signed AAB собран через private signing material вне project tree; для повторной сборки используйте [docs/signing_guide.md](docs/signing_guide.md) и пример [keystore.properties.example](keystore.properties.example).

## Структура
- [MainActivity.kt](app/src/main/java/ru/cisgame/colorquarter/MainActivity.kt) — Android entry point.
- [ColorQuarterApp.kt](app/src/main/java/ru/cisgame/colorquarter/ui/ColorQuarterApp.kt) — Compose UI, экраны, состояние.
- [GameEngine.kt](app/src/main/java/ru/cisgame/colorquarter/game/GameEngine.kt) — игровая логика.
- [LevelCatalog.kt](app/src/main/java/ru/cisgame/colorquarter/data/LevelCatalog.kt) — 36 уровней.
- [ProgressStore.kt](app/src/main/java/ru/cisgame/colorquarter/data/ProgressStore.kt) — локальный прогресс, активная попытка и настройки.
- [BoardSnapshotCodec.kt](app/src/main/java/ru/cisgame/colorquarter/data/BoardSnapshotCodec.kt) — единый формат snapshots поля для undo и active attempt persistence.
- [app_background.webp](app/src/main/res/drawable-nodpi/app_background.webp) — оптимизированный декоративный ImageGen-фон для всех экранов.
- [onboarding_illustration.webp](app/src/main/res/drawable-nodpi/onboarding_illustration.webp) — оптимизированная ImageGen-иллюстрация для первого запуска.
- [home_illustration.webp](app/src/main/res/drawable-nodpi/home_illustration.webp) — оптимизированная ImageGen-иллюстрация для главного экрана.
- [level_illustration.webp](app/src/main/res/drawable-nodpi/level_illustration.webp) — оптимизированная ImageGen-иллюстрация для экрана уровня.
- [action_panel_texture.webp](app/src/main/res/drawable-nodpi/action_panel_texture.webp) — лёгкая ImageGen-подложка для отдельных кнопок уровня.
- [ic_nav_back.xml](app/src/main/res/drawable/ic_nav_back.xml) и [ic_nav_settings.xml](app/src/main/res/drawable/ic_nav_settings.xml) — векторные top-bar icons вместо текстовых glyph-кнопок.
- [victory_illustration.webp](app/src/main/res/drawable-nodpi/victory_illustration.webp) — оптимизированная ImageGen-иллюстрация для победного результата.
- [defeat_illustration.webp](app/src/main/res/drawable-nodpi/defeat_illustration.webp) — оптимизированная ImageGen-иллюстрация для результата без ходов.
- [validate_release_candidate.sh](scripts/validate_release_candidate.sh) — полный release validation.
- [package_release_candidate.sh](scripts/package_release_candidate.sh) — сборка RC publishing pack.
- [verify_release_candidate_package.sh](scripts/verify_release_candidate_package.sh) — независимая проверка собранного RC archive.

## Google Play
Подготовлены:
- [docs/google_play_checklist.md](docs/google_play_checklist.md)
- [docs/build_environment.md](docs/build_environment.md)
- [docs/play_requirements_audit.md](docs/play_requirements_audit.md)
- [docs/play_console_runbook.md](docs/play_console_runbook.md)
- [docs/security_audit.md](docs/security_audit.md)
- [docs/dependency_audit.md](docs/dependency_audit.md)
- [docs/third_party_notices.md](docs/third_party_notices.md)
- [docs/privacy_and_permissions.md](docs/privacy_and_permissions.md)
- [docs/release_report.md](docs/release_report.md)
- [docs/design_review_2026-05-24.md](docs/design_review_2026-05-24.md)
- [docs/signing_guide.md](docs/signing_guide.md)
- `play-assets/metadata/ru-RU` — готовый Play listing handoff.
- `play-assets/metadata/ru-RU/play_console_submission.md` — единый copy/paste pack для Play Console.
- `fastlane/metadata/android/ru-RU` — Play listing metadata/assets mirror in Fastlane-compatible layout for future automation.
- `play-assets/metadata/ru-RU/asset_alt_text.md` — localized alt text для Play graphics и screenshots.
- `play-assets/legal/privacy_policy_ru.html` — готовая privacy policy для публикации на HTTPS URL.
- `play-assets/screenshots/phone` — 6 phone screenshots saved as 1080x2400 RGB PNG without alpha; home and level were refreshed again on 2026-05-24 after next-goal/objective polish.
- `play-assets/screenshots/phone/contact_sheet.png` — только internal QA preview; RC packaging исключает его из uploadable store screenshots.
- `play-assets/graphics/app_icon_512.png` — high-res app icon 512x512 для Play Console, refreshed through ImageGen and processed to opaque Play-ready RGBA PNG.
- `play-assets/graphics/feature_graphic.png` — feature graphic 1024x500, refreshed through ImageGen and processed to Play-ready RGB PNG.
- Старые hand-made SVG store creatives перенесены в `archive/rejected-assets/play-graphics` и описаны в `docs/rejected_assets.md`; uploadable store graphics теперь только PNG.
- `qa-artifacts/imagegen` — source/processed evidence for ImageGen-generated store icon, feature graphic, app background, onboarding illustration, home dashboard illustration, level illustration, action panel texture, victory result illustration and defeat result illustration refresh.
- `qa-artifacts/store-screenshots-refresh-2026-05-21` — final Play screenshot capture evidence from dedicated AVD `project_52game_emulator`.
- `qa-artifacts/design-review-2026-05-24` — dedicated AVD evidence for next-goal panel, explicit level objective and refreshed home/level screenshots.
- `qa-artifacts/action-panel-refresh` — screenshots/UI dumps from the dedicated project AVD after action-panel texture, vector icons and button press-state polish.
- `qa-artifacts/level-visual-refresh` — screenshots/UI dumps from the dedicated project AVD after level-screen banner, separate action buttons and animation polish.
- `qa-artifacts/font-scale-1.3` и `qa-artifacts/font-scale-1.5` — screenshots/UI dumps для accessibility QA.
- `release-candidate/ColorQuarter-1.0.0-RC-publishing-pack.zip` — архив для передачи/публикационной подготовки.
- `release-candidate/ColorQuarter-1.0.0-RC-publishing-pack.zip.sha256` — checksum архива.
- `release-candidate/<version>/` — локальная staging-папка, создаётся packaging-скриптом и игнорируется git.

Ручные действия перед публикацией: создать Play Console app, настроить Play App Signing/upload key, разместить privacy policy на публичном HTTPS URL, проверить и загрузить app icon/screenshots/feature graphic, заполнить Data Safety и content rating.
