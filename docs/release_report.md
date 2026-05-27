# Release Report

Дата: 2026-05-27  
Проект: `Цветной Квартал`  
Package: `ru.cisgame.colorquarter`  
Version: `1.0.0 (1)`

## Выбранная идея
Offline-first Android color-flood puzzle для русскоязычной casual-аудитории. Идея выбрана потому, что даёт короткий понятный игровой цикл, низкий privacy/policy risk, не требует backend и реалистично доводится до polished release candidate.

## Что реализовано
- Нативное Android-приложение на Kotlin + Jetpack Compose + Material 3.
- 36 локальных уровней с русскими названиями и прогрессией.
- Игровой цикл: onboarding, home, выбор уровня, поле, палитра, ходы/лимит, подсказка с приростом, undo, restart, victory/defeat, settings, about/privacy.
- Локальное сохранение прогресса, настроек и незавершённой попытки через SharedPreferences.
- Защита сохранений: sanitization прогресса и active attempt, critical writes через `commit()`.
- UI polish: ImageGen background, onboarding/home/level/result illustrations, action-panel texture, separate action buttons, vector icons, press-state feedback, reduced motion, high contrast.
- Store assets: ImageGen app icon, ImageGen feature graphic, 6 current Play screenshots, privacy policy, metadata, Data Safety notes, content rating notes, target audience notes.
- Store listing accessibility: localized alt text pack for Play graphics and screenshots.
- Design/layout/code review 2026-05-24: next-goal panel, adaptive level grid and explicit level objective were added after reviewing the game as designer, layout engineer and programmer.
- Security/privacy posture: no backend, no accounts, no analytics, no ads, no IAP, no runtime Android permissions.
- Signing hygiene: private signing files are gitignored; binary upload keystore files are blocked inside the project tree by validation. A production upload key was generated outside the repository for the local signed AAB handoff.
- Dependency governance: approved Gradle plugins, repositories and direct dependencies are documented and enforced by release validation.
- Third-party notices: runtime/build/test dependency families and license posture are documented and enforced as part of release validation.
- Build environment provenance: Gradle/JDK/SDK/toolchain facts are documented and enforced as part of release validation.
- Release automation: validation script and RC packaging script with manifest, checksums and uploadable asset filtering.
- Standalone RC verification script for checking an already built publishing archive before handoff.
- Performance guardrails: automated budgets for release AAB, debug APK, in-app WebP inventory and heavy media/font resources.

## Финальные изменения 2026-05-21 / 2026-05-24
- Replaced top-bar text glyph buttons with native vector drawables:
  - `app/src/main/res/drawable/ic_nav_back.xml`
  - `app/src/main/res/drawable/ic_nav_settings.xml`
- Refreshed all six Play phone screenshots on dedicated AVD `project_52game_emulator`:
  - `play-assets/screenshots/phone/01_onboarding.png`
  - `play-assets/screenshots/phone/02_home.png`
  - `play-assets/screenshots/phone/03_level.png`
  - `play-assets/screenshots/phone/04_victory.png`
  - `play-assets/screenshots/phone/05_settings.png`
  - `play-assets/screenshots/phone/06_about_privacy.png`
- Converted all uploadable phone screenshots to 1080x2400 8-bit RGB PNG without alpha.
- Strengthened `scripts/validate_release_candidate.sh` to reject alpha-channel Play screenshots and non-PNG files in `play-assets/graphics`.
- Strengthened release validation to inspect the release AAB for `local.properties`, `sdk.dir` and user-specific Android SDK paths.
- Strengthened RC packaging to reject an actual `local.properties` file, `sdk.dir`, absolute macOS user-home paths and foreign-user path fragments in uploadable/documentation text surfaces.
- Pinned Gradle wrapper distribution SHA-256 for `gradle-8.13-bin.zip` and added wrapper validation to release checks.
- Added critical file byte/SHA-256 inventory to `release_manifest.json` for the AAB, Play graphics, phone screenshots, privacy policy HTML, Play Console submission copy and asset alt text.
- Converted README links from local absolute workspace paths to repository-relative links.
- Moved obsolete hand-made SVG store creatives out of uploadable path:
  - `archive/rejected-assets/play-graphics/app_icon_legacy_vector.svg`
  - `archive/rejected-assets/play-graphics/feature_graphic_legacy_vector.svg`
- Created `docs/rejected_assets.md`.
- Updated Play checklist, Play runbook, Play requirements audit, UI audit, asset manifest, QA plan, content/accessibility/performance notes, README and AGENTS.
- Removed a reappeared `play-assets/metadata/ru-RU/privacy_policy_draft.md` after validation correctly blocked its placeholder replacement text.
- Added local-machine path leakage checks to both release validation and RC packaging so machine-specific Android SDK configuration cannot silently enter the app bundle or handoff archive.
- Added `NextGoalPanel` on home: the player now sees `Следующая цель`, level title, district, move limit, 3-star target, remaining level count, best-result/complete hint and one clear CTA.
- Added adaptive `HomeLevelGrid`: 3 columns on phone width and 4 columns from 520dp.
- Added explicit level objective copy: `Цель: собрать 100% за N ходов`.
- Added new Russian string resources for next-goal and level objective copy.
- Refreshed Play screenshots `02_home.png` and `03_level.png` on dedicated AVD `project_52game_emulator`; regenerated `contact_sheet.png`.
- Created `docs/design_review_2026-05-24.md` with designer/layout/programmer findings, plan, implemented changes and visual QA evidence.

## Google Play Requirements Check
Official sources checked on 2026-05-27:
- Target API: https://developer.android.com/google/play/requirements/target-sdk
- Android App Bundle format: https://developer.android.com/guide/app-bundle/app-bundle-format
- Preview assets/screenshots: https://support.google.com/googleplay/android-developer/answer/9866151?hl=en

Current project state:
- `targetSdk=35`, aligned with the current API 35+ requirement for new apps/updates.
- Release publishing format is `.aab`.
- Screenshots are 24-bit PNG without alpha.
- App icon is 512x512 32-bit PNG with fully opaque alpha and <=1024 KB.
- Play asset alt text is prepared for the app icon, feature graphic and six phone screenshots.

## Ключевые файлы
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/ru/cisgame/colorquarter/ui/ColorQuarterApp.kt`
- `app/src/main/java/ru/cisgame/colorquarter/game/GameEngine.kt`
- `app/src/main/java/ru/cisgame/colorquarter/data/LevelCatalog.kt`
- `app/src/main/java/ru/cisgame/colorquarter/data/ProgressStore.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/drawable-nodpi/*.webp`
- `app/src/main/res/drawable/ic_action_*.xml`
- `app/src/main/res/drawable/ic_nav_*.xml`
- `scripts/validate_release_candidate.sh`
- `scripts/package_release_candidate.sh`
- `scripts/verify_release_candidate_package.sh`
- `play-assets/graphics/app_icon_512.png`
- `play-assets/graphics/feature_graphic.png`
- `play-assets/screenshots/phone/*.png`
- `play-assets/metadata/ru-RU/asset_alt_text.md`
- `fastlane/metadata/android/ru-RU/*` (Play listing mirror for future automation)

## Созданные / обновлённые документы
- `AGENTS.md`
- `README.md`
- `docs/product_decision.md`
- `docs/product_spec.md`
- `docs/tech_stack_decision.md`
- `docs/release_plan.md`
- `docs/release_report.md`
- `docs/google_play_checklist.md`
- `docs/play_requirements_audit.md`
- `docs/play_console_runbook.md`
- `docs/security_audit.md`
- `docs/dependency_audit.md`
- `docs/build_environment.md`
- `docs/third_party_notices.md`
- `docs/ui_audit.md`
- `docs/qa_test_plan.md`
- `docs/performance_notes.md`
- `docs/privacy_and_permissions.md`
- `docs/art_direction.md`
- `docs/asset_manifest.md`
- `docs/asset_prompts.md`
- `docs/content_audit.md`
- `docs/accessibility_notes.md`
- `docs/story_bible.md`
- `docs/signing_guide.md`
- `docs/rejected_assets.md`
- `docs/design_review_2026-05-24.md`

## Команды и результаты
Окружение:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

Build/test/lint/release:
```bash
./gradlew --no-daemon --no-configuration-cache clean test assembleDebug lint bundleRelease --console=plain
```
Результат: `BUILD SUCCESSFUL in 40s`, 109 actionable tasks.

Full release validation:
```bash
GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache --console=plain" scripts/validate_release_candidate.sh
```
Результат последнего прогона:
- Signed release checks: `BUILD SUCCESSFUL in 43s`, 110 actionable tasks.
- Signing report: `BUILD SUCCESSFUL in 5s`; release signing config is `releaseUpload`, upload key SHA-256 is `3E:3F:2F:53:1D:9C:BF:76:E7:3C:EE:32:83:71:C6:FD:77:54:D8:88:41:0B:30:75:2C:AB:39:24:70:9A:B2:48`.
- Gradle wrapper validation: pass; wrapper URL, pinned distribution SHA-256, executable `gradlew` and wrapper jar contents are valid.
- Build environment documentation validation: pass; Gradle/JDK/SDK/toolchain facts are present in `docs/build_environment.md`.
- Release automation script validation: pass; validation, packaging and standalone verifier scripts exist, are executable and pass `bash -n`.
- Signing hygiene check: pass; `.gitignore` covers local/private signing files, private `keystore.properties` is present locally, points outside the project tree and no binary upload keystore files are inside the project tree.
- Local machine path leakage check: pass; no `local.properties`, `sdk.dir` or user-specific Android SDK paths in release AAB.
- Security/dependency scan: pass.
- Dependency allowlist validation: pass; approved plugins, repositories and direct dependencies match `docs/dependency_audit.md`.
- Third-party notices validation: pass; runtime/build/test dependency notices are present for the approved dependency posture.
- Play asset validation: pass.
- Play asset alt text validation: pass; every uploadable Play graphic and screenshot has a <=140-character description.
- Performance and size budget check: pass; signed release AAB `2770212 / 8388608` bytes, debug APK `11969062 / 26214400` bytes, in-app WebP total `150084 / 262144` bytes, no audio/video/custom font resources.
- Screenshot format validation: pass; all six phone screenshots are color type 2 RGB PNG.
- Privacy/Play form consistency: pass.
- Merged manifest privacy: pass; no Android system permissions requested.
- Release identity: pass.
- Placeholder scan: pass.
- Localization surface scan: pass.
- AAB content check: pass.

Design review quick Gradle check:
```bash
./gradlew --no-daemon --no-configuration-cache --console=plain :app:assembleDebug :app:lint :app:test
```
Результат: `BUILD SUCCESSFUL in 3m 15s`, 82 actionable tasks. Перед этим такой же ad-hoc command без `ANDROID_HOME` завершился `SDK location not found`; это было исправлено установкой окружения и не было проблемой кода.

Dedicated emulator screenshot QA:
```bash
adb -s emulator-5558 emu avd name
adb -s emulator-5558 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5558 shell pm clear ru.cisgame.colorquarter.debug
adb -s emulator-5558 exec-out screencap -p
adb -s emulator-5558 logcat -d -t 2000
adb -s emulator-5558 emu kill
```
Результат: `emulator-5558` was verified as `project_52game_emulator`; app was installed and data-cleared only for `ru.cisgame.colorquarter.debug`; screenshots/UI evidence saved under `qa-artifacts/store-screenshots-refresh-2026-05-21`; no app-package crash entries found; only the project emulator was stopped.

Dedicated active-attempt restore QA:
```bash
adb -s emulator-5560 emu avd name
adb -s emulator-5560 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5560 shell pm clear ru.cisgame.colorquarter.debug
adb -s emulator-5560 shell am start -n ru.cisgame.colorquarter.debug/ru.cisgame.colorquarter.MainActivity
adb -s emulator-5560 shell am force-stop ru.cisgame.colorquarter.debug
adb -s emulator-5560 shell am start -n ru.cisgame.colorquarter.debug/ru.cisgame.colorquarter.MainActivity
adb -s emulator-5560 emu kill
```
Результат: `emulator-5560` was verified as `project_52game_emulator`; a different project AVD was detected on another serial and not used. After one consumed move, force-stop/relaunch restored level 1 with `Ходы 1`, `Ост. 7`, current color `Солнце`, available undo and no crash-buffer entries. Evidence saved under `qa-artifacts/active-attempt-restore-2026-05-23`.

Dedicated design-review QA:
```bash
adb -s emulator-5556 emu avd name
adb -s emulator-5556 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5556 shell pm clear ru.cisgame.colorquarter.debug
adb -s emulator-5556 shell am start -n ru.cisgame.colorquarter.debug/ru.cisgame.colorquarter.MainActivity
adb -s emulator-5556 exec-out screencap -p
adb -s emulator-5556 logcat -d -t 2000
adb -s emulator-5556 emu kill
```
Результат: `emulator-5556` was verified as `project_52game_emulator`; a foreign `project_53game_emulator` was detected on another serial and not used. UI dump confirmed `Следующая цель`, `Уровень 1: Тихий двор`, `Осталось: 36`, `Продолжить: Тихий двор`, `Цель: собрать 100% за 8 ходов`, `Палитра цветов`, `Отмена`, `Подсказка`, `Заново`; app crash log was empty. Evidence saved under `qa-artifacts/design-review-2026-05-24`.

Dedicated RTF recertification QA:
```bash
adb -s emulator-5556 emu avd name
adb -s emulator-5556 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5556 shell pm clear ru.cisgame.colorquarter.debug
adb -s emulator-5556 shell am start -n ru.cisgame.colorquarter.debug/ru.cisgame.colorquarter.MainActivity
adb -s emulator-5556 exec-out screencap -p
adb -s emulator-5556 exec-out uiautomator dump /dev/tty
adb -s emulator-5556 logcat -b crash -d
```
Результат: `emulator-5556` was verified as `project_52game_emulator`; a foreign `project_betano_emulator` was detected as `emulator-5554` and not used. Flow covered onboarding, home, level 1, hint, known 6-move win, victory result and transition to the next level. UI dumps confirmed `Следующая цель`, `Уровень 1: Тихий двор`, `Цель: собрать 100% за 8 ходов`, `Подсказка: Солнце, +2 клетки`, `Квартал собран`, `★★★`, `Первое прохождение: 6 ходов`, `Следующий`; app crash log was empty. Evidence saved under `qa-artifacts/recertification-2026-05-27`.

RC packaging:
```bash
scripts/package_release_candidate.sh 1.0.0
scripts/verify_release_candidate_package.sh 1.0.0
cd release-candidate
shasum -a 256 -c ColorQuarter-1.0.0-RC-publishing-pack.zip.sha256
```
Результат: package creation, standalone RC verification and archive checksum validation passed. RC manifest structure/path validation passed for release identity, AAB status/path, uploadable assets, required docs including sanitized `docs/AGENTS.md`, QA pointers and manual Play blockers. Critical file byte/SHA-256 validation passed for the AAB, Play graphics, six phone screenshots, privacy policy HTML, Play Console submission copy and asset alt text. RC pack text leakage check passed: no actual `local.properties` file, `sdk.dir` assignment or absolute macOS user-home paths in RC text surfaces. The archive checksum is stored externally in `release-candidate/ColorQuarter-1.0.0-RC-publishing-pack.zip.sha256`.

Non-blocking command note:
- Ad-hoc Python manifest summaries initially queried non-schema keys (`manual_blockers`, then `screenshots`); the manifest schema uses `manual_google_play_blockers` and `phone_screenshots`. Corrected summary commands passed. No project artifact, validation or release output was affected.
- The first 2026-05-24 full validation after design-review docs intentionally failed the Play graphics inventory because duplicate SVG copies had reappeared in `play-assets/graphics`; checksums matched the already archived rejected assets, the uploadable-path copies were removed, `docs/rejected_assets.md` was updated and the rerun passed.
- A validation rerun intentionally failed while hardening because obsolete SVG store sources and an old privacy-policy draft had reappeared in `play-assets`; both were removed from release surfaces and the final validation passed.
- RC packaging was hardened so `release_manifest.json` is validated before checksums and zip creation; malformed paths, missing files, wrong release identity, wrong AAB status/path, unexpected uploadable assets or incomplete manual blockers now fail packaging.
- Gradle wrapper supply-chain hardening was added after fetching the official Gradle 8.13 distribution checksum from `services.gradle.org`; final validation passed with the checksum pinned.
- RC packaging manifest integrity was hardened again so critical file records must have relative paths, positive byte sizes, lowercase SHA-256 values and hash/size matches against files on disk; `checksums.sha256` generation now uses null-delimited paths.
- Play listing accessibility handoff was added: `asset_alt_text.md` is included in metadata, validated by release checks and recorded in the RC manifest.
- Performance budget validation was added to block accidental heavy assets or oversized release artifacts before publishing.
- Standalone RC archive verification was added so the handoff zip can be checked independently after packaging.
- Signing hygiene validation was added to reduce the chance of accidentally keeping upload keystore material in the project tree.
- Dependency allowlist validation was added to block accidental new direct libraries before publishing.

## Release Artifacts
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`
- RC archive: `release-candidate/ColorQuarter-1.0.0-RC-publishing-pack.zip`
- Checksum file: `release-candidate/ColorQuarter-1.0.0-RC-publishing-pack.zip.sha256`
- Local staging directory: `release-candidate/<version>/`, generated by packaging and ignored by git.

## Release Build Status
`bundleRelease` succeeds and creates a signed `app-release.aab` in the current local environment because a private `keystore.properties` points to an upload keystore outside the repository. The upload key and passwords are kept outside git and were copied only to the private Desktop handoff folder.

Upload-ready status: **signed local AAB is ready for Play Console internal testing after the Play Console app, Play App Signing setup and privacy policy URL are prepared**.

## Что не реализовано и почему
- Production signing key: generated and kept outside the repository; it must be backed up securely by the owner.
- Production-signed AAB: generated locally; do not upload the keystore or `keystore.properties` to GitHub.
- Play Console upload/forms: account actions must be done manually.
- Public privacy policy hosting: local HTML is prepared, but hosting URL is manual.
- Ads/IAP/analytics/cloud sync: intentionally excluded from v1.0.
- Physical-device TalkBack and low-end-device profiling: still manual pre-publish checks.

## Оставшиеся риски
- Manual Play Console preview may reveal crop issues in store listing layout; screenshots are current and format-valid, but preview still needs human review.
- `targetSdk=35` is current as of 2026-05-27; re-check target API policy before actual upload if submission happens later.
- No physical low-end device QA was performed.
- No manual TalkBack pass was performed.

## Manual Google Play Actions
1. Create/connect the Play Console app.
2. Create production upload keystore outside the repository.
3. Fill private `keystore.properties` from `keystore.properties.example`.
4. Rebuild signed AAB:
   ```bash
   ./gradlew clean :app:bundleRelease
   ```
5. Verify signing with `jarsigner`.
6. Host `play-assets/legal/privacy_policy_ru.html` on public HTTPS.
7. Upload signed AAB and store assets from `play-assets`.
8. Fill Data Safety, App Access, Content Rating and Target Audience using `play-assets/metadata/ru-RU`.
9. Run internal testing and review Play pre-launch report.
10. Use staged rollout after internal testing passes.

## Итоговая оценка
Project state: **production-ready release candidate for local handoff and Play Console preparation**.

Not yet direct-to-Play-upload only because Play Console account actions, public privacy policy hosting and internal testing/pre-launch report remain manual blockers.
