# Google Play Checklist

## App Identity
- App name: `Цветной Квартал`
- Package/applicationId: `ru.cisgame.colorquarter`
- Version code: `1`
- Version name: `1.0.0`
- minSdk: `23`
- targetSdk: `35`
- compileSdk: `36`

Google Play target API: новые приложения и обновления должны target Android 15 / API 35+ с 31 августа 2025. Источник: https://developer.android.com/google/play/requirements/target-sdk

Publishing format: Android App Bundle `.aab`. Источник: https://developer.android.com/guide/app-bundle

Play requirements audit: `docs/play_requirements_audit.md`.

App preview asset requirements checked 2026-05-27: screenshots must be JPEG or 24-bit PNG without alpha; store screenshots should demonstrate the actual in-app/game experience. Источник: https://support.google.com/googleplay/android-developer/answer/9866151?hl=en

## Store Copy
Short description:
> Спокойная цветовая головоломка без интернета: соберите квартал за лимит ходов.

Full description:
> Цветной Квартал — простая offline-головоломка для коротких сессий. Выбирайте цвета, расширяйте связанный участок от угла и собирайте весь район в один цвет. В игре 36 уровней, подсказки, звёзды, рекорды, локальный прогресс, мягкие анимации и настройки доступности. Интернет, аккаунт, покупки и сбор данных не требуются.

Release notes:
> Первый релиз: 36 уровней, подсказки, звёзды, рекорды и локальный прогресс.

Length check:
- Title: 15 / 30 chars.
- Short description: 78 / 80 chars.
- Full description: 573 / 4000 chars.
- Release notes: 74 / 500 chars.

## Graphics Checklist
- App icon: готов `play-assets/graphics/app_icon_512.png` 512x512, 32-bit PNG, 276 KB, fully opaque alpha, refreshed through built-in ImageGen on 2026-05-20; release validation enforces size <=1024 KB and no transparent pixels.
- App icon source: ImageGen source/processed files are stored in `qa-artifacts/imagegen`; obsolete hand-made SVG source was moved to `archive/rejected-assets/play-graphics` and is not a release asset.
- Adaptive icon: готов в `mipmap-anydpi-v26`, включая monochrome drawable для themed icons.
- Feature graphic: готов `play-assets/graphics/feature_graphic.png` 1024x500, 8-bit RGB PNG without alpha, refreshed through built-in ImageGen on 2026-05-20; release validation enforces this format.
- Screenshots: готовы 6 phone screenshots в `play-assets/screenshots/phone`, включая onboarding, home, level, victory, settings и in-app privacy/about. Все 6 пересняты 2026-05-21 на dedicated AVD `project_52game_emulator`; home and level screenshots refreshed again on 2026-05-24 after next-goal panel, adaptive grid and explicit level objective polish. Uploadable screenshots are 1080x2400 8-bit RGB PNG без alpha. Contact sheet is regenerated for QA only and excluded from uploadable RC store-listing screenshots.
- Fastlane-compatible metadata/assets mirror: готов `fastlane/metadata/android/ru-RU` для будущей автоматизации Play listing.
- Tablet screenshots: не обязательно для первого релиза, но полезно.

## Privacy
- Privacy policy source: готов `play-assets/legal/privacy_policy_ru.html`.
- Privacy policy URL: нужно разместить HTML вручную на публичном HTTPS URL.
- Data Safety: No data collected/shared.
- Data Safety notes: готово `play-assets/metadata/ru-RU/data_safety.md`.
- Permissions explanation: no Android system permissions/runtime prompts; merged release manifest includes only AndroidX internal signature-level `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`.
- Backup: Android Auto Backup отключён, игровой прогресс остаётся на устройстве.
- Ads declaration: no ads.
- App access: all features available without account.
- App Access notes: готово `play-assets/metadata/ru-RU/app_access_notes.md`.
- In-app privacy/about screen: готово в настройках.
- Release validation checks consistency across privacy policy, Data Safety notes, App Access notes, Target Audience notes and Play submission copy for no data collection, no sharing, no ads, no purchases, no account and local-only storage claims.

## Content Rating
- Target audience: casual users, recommended 13+ in Play Console.
- Target Audience notes: готово `play-assets/metadata/ru-RU/target_audience_notes.md`.
- Content Rating notes: готово `play-assets/metadata/ru-RU/content_rating_notes.md`.
- Violence: none.
- Gambling/stakes: none.
- User-generated content: none.
- Online interaction: none.
- Content warnings: none expected.

## Testing Instructions
1. Install app.
2. Pass onboarding.
3. Start first level.
4. Select colors in sequence until victory.
5. Return home and verify level 2 unlocks.
6. Open settings and toggle reduced motion/high contrast.
7. Force-stop/relaunch and verify progress remains.
8. Start a level, make one move, force-stop/relaunch and verify the active attempt reopens with the same move count and current color.

## Release Build Status
- `./gradlew bundleRelease` creates `app/build/outputs/bundle/release/app-release.aab`.
- Optional release signing config is prepared through private `keystore.properties`.
- Current local `.aab` is signed with `releaseUpload`; the upload keystore and private `keystore.properties` are outside the repository and copied only to the private Desktop handoff folder.
- Signing guide: `docs/signing_guide.md`.
- Signing hygiene validation checks `.gitignore`, optional `keystore.properties` completeness and keeps binary upload keystore files outside the project tree.
- Final validation command: `scripts/validate_release_candidate.sh`.
- Clean local validation command: `GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache" scripts/validate_release_candidate.sh`.
- Final validation verifies Gradle wrapper supply-chain settings: HTTPS distribution URL, pinned SHA-256, `validateDistributionUrl=true`, executable `gradlew` and required wrapper jar classes.
- Final validation verifies dependency allowlist from `docs/dependency_audit.md`: approved Gradle plugins, repositories and direct dependencies only.
- RC packaging command: `scripts/package_release_candidate.sh 1.0.0`.
- RC archive verification command: `scripts/verify_release_candidate_package.sh 1.0.0`.
- RC packaging validates that the requested pack version matches release `BuildConfig.VERSION_NAME` and merged manifest `versionName`.
- RC packaging writes and validates `release_manifest.json` with release identity, AAB signing status/path, uploadable Play assets, QA/source assets, required docs and manual Play blockers.
- `release_manifest.json` includes byte size and SHA-256 for critical handoff files: AAB, app icon, feature graphic, six phone screenshots, privacy policy HTML, Play Console submission copy and asset alt text. Packaging validates those records against the files on disk.
- RC archive verification independently checks the zip checksum, unzip integrity, internal checksums, manifest critical hashes, Play asset inventory and local path leakage in handoff surfaces.
- Release validation now decodes the Play app icon PNG and fails if any alpha pixel is transparent.
- Release validation now rejects Play screenshots unless they are 1080x2400 8-bit RGB PNG without alpha.
- Release validation checks `play-assets/metadata/ru-RU/asset_alt_text.md` has one <=140-character description for each uploadable Play graphic and screenshot.
- Release validation and RC packaging now reject local Android SDK configuration leakage, actual `local.properties` files, `sdk.dir` assignments and machine-specific user paths in release handoff surfaces.

## Manual Play Console Actions
- Create app.
- Enable/configure Play App Signing and upload key.
- Create upload keystore outside repository, fill private `keystore.properties`, rebuild `./gradlew clean :app:bundleRelease`.
- Upload `.aab`.
- Add app icon, screenshots and feature graphic from `play-assets`.
- Host `play-assets/legal/privacy_policy_ru.html` on public HTTPS and add privacy policy URL.
- Complete Data Safety, Ads, Content Rating, Target Audience forms using `play-assets/metadata/ru-RU/play_console_submission.md`.
- Run internal testing before production rollout.

## RC Publishing Pack
- Archive: `release-candidate/ColorQuarter-1.0.0-RC-publishing-pack.zip`.
- Archive checksum: `release-candidate/ColorQuarter-1.0.0-RC-publishing-pack.zip.sha256`.
- Unpacked staging directory `release-candidate/<version>/` is generated locally by packaging and ignored by git; the zip contains the manifest, internal checksums, sanitized docs and standalone verifier.
- Store graphics in the RC pack contain exactly 2 uploadable PNG files; obsolete SVG store creatives are archived under `archive/rejected-assets/play-graphics` and are not included in uploadable Play assets.
- ImageGen sources for refreshed app icon, feature graphic, app background, onboarding illustration, home dashboard illustration, level illustration, action panel texture, victory result illustration and defeat result illustration are kept only in QA evidence under `qa-artifacts/imagegen`, not in uploadable store graphics.
- Store screenshots in the RC pack contain exactly 6 uploadable phone PNG files; contact sheet is moved to QA evidence.
- Store listing metadata includes localized alt text for all uploadable graphics and phone screenshots.
- Note: after local signing, the RC pack includes `signed-aab/app-release.aab`; keep upload key material private and never commit it.
