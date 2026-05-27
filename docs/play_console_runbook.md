# Play Console Runbook

Дата подготовки: 2026-05-23

## 1. Before Opening Play Console
1. Create production upload keystore outside the repository.
2. Fill private `keystore.properties` from `keystore.properties.example`.
   Keep the binary upload keystore outside the project tree; validation blocks `.jks`, `.keystore`, `.p12` and `.pfx` files inside the project.
3. Rebuild signed bundle:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew clean :app:bundleRelease
```

4. Verify signing:

```bash
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

5. Run final validation:

```bash
scripts/validate_release_candidate.sh
scripts/package_release_candidate.sh 1.0.0
scripts/verify_release_candidate_package.sh 1.0.0
```

`scripts/package_release_candidate.sh` must be called with the same version as the release build. The script fails if the argument does not match release `BuildConfig.VERSION_NAME` and merged manifest `versionName`.
`scripts/verify_release_candidate_package.sh` independently verifies the generated RC zip, archive checksum, internal file checksums and manifest critical hashes.

## 2. Create App
- App name: `Цветной Квартал`
- Default language: Russian (`ru-RU`)
- App or game: Game
- Category: Puzzle
- Free app
- Contains ads: No

## 3. App Signing
1. Enable Play App Signing.
2. Use the locally generated upload key.
3. Upload the signed AAB from:

```text
app/build/outputs/bundle/release/app-release.aab
```

Do not upload `release-candidate/1.0.0/unsigned-aab/app-release-unsigned.aab`.

## 4. Store Listing
Use:

```text
play-assets/metadata/ru-RU/play_console_submission.md
```

For optional Play accessibility/alt text fields use:

```text
play-assets/metadata/ru-RU/asset_alt_text.md
```

Upload:
- `play-assets/graphics/app_icon_512.png`
- `play-assets/graphics/feature_graphic.png`
- `play-assets/screenshots/phone/01_onboarding.png`
- `play-assets/screenshots/phone/02_home.png`
- `play-assets/screenshots/phone/03_level.png`
- `play-assets/screenshots/phone/04_victory.png`
- `play-assets/screenshots/phone/05_settings.png`
- `play-assets/screenshots/phone/06_about_privacy.png`

Screenshots were refreshed on 2026-05-21 on the dedicated project AVD `project_52game_emulator` and converted to 1080x2400 RGB PNG without alpha. Before production upload, review them in Play Console preview; do not recapture on a shared or foreign emulator.

Do not upload `contact_sheet.png`; it is internal QA evidence. In the RC publishing pack it is kept under `qa-artifacts/store-listing-preview`, not under uploadable `store-listing/screenshots`.

Do not upload SVG/source files. Uploadable graphics are only `store-listing/graphics/app_icon_512.png` and `store-listing/graphics/feature_graphic.png`. Obsolete hand-made SVG store creatives are archived under `archive/rejected-assets/play-graphics` and documented in `docs/rejected_assets.md`.

If Play Console exposes alt text fields for preview assets, fill them from `asset_alt_text.md`; each prepared description is localized and kept within 140 characters.

## 5. Privacy And App Content
1. Host `play-assets/legal/privacy_policy_ru.html` on a public HTTPS URL.
2. Add that URL to Play Console.
3. Fill Data Safety from `play-assets/metadata/ru-RU/data_safety.md`.
4. Fill Content Rating from `play-assets/metadata/ru-RU/content_rating_notes.md`.
5. Fill Target Audience from `play-assets/metadata/ru-RU/target_audience_notes.md`.
6. Fill App Access from `play-assets/metadata/ru-RU/app_access_notes.md`.
7. Ads declaration: No ads.
8. Payments/IAP: none.

## 6. Testing Track
1. Upload signed AAB to internal testing first.
2. Add tester emails or group.
3. Install from Play internal test link.
4. Run the smoke from `docs/qa_test_plan.md`.
5. Wait for and review Play pre-launch report.
6. Fix any policy, crash, ANR or rendering issue before production.

Local emulator smoke before Play upload must use only the dedicated AVD `project_52game_emulator`. Do not reuse a shared emulator or a device from another project. If several devices are connected, every `adb` command must include `-s <project_52game_device_id>`.

## 7. Production Rollout
Use staged rollout:
- Start at 5-10%.
- Watch crashes, ANRs, ratings and Play Console warnings.
- Increase only after at least 24-48 hours without critical issues.

## Stop Conditions
Do not submit to production if:
- AAB is unsigned or signed with a throwaway/test key.
- Privacy policy URL is missing or not public HTTPS.
- Play pre-launch report shows app crashes.
- Store listing preview shows cropped screenshots or wrong icon.
- Data Safety answers differ from actual app behavior.
