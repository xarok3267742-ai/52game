# Play Requirements Audit

Дата проверки: 2026-05-27

## Official Sources
- Target API level: https://developer.android.com/google/play/requirements/target-sdk
- Play Console target API policy: https://support.google.com/googleplay/android-developer/answer/11926878
- Android App Bundle format: https://developer.android.com/guide/app-bundle/app-bundle-format
- App Bundle FAQ / Play App Signing: https://developer.android.com/guide/app-bundle/faq
- Preview assets / screenshots: https://support.google.com/googleplay/android-developer/answer/9866151?hl=en

## Findings
| Requirement | Project state | Status |
|---|---|---|
| New mobile apps must target Android 15 / API 35+ from 2025-08-31 | `targetSdkVersion=35` | Pass |
| New apps should publish to Google Play as Android App Bundle | `app/build/outputs/bundle/release/app-release.aab` exists | Pass |
| Play App Signing is required for new apps using AAB | Signing flow documented; upload key is manual | Manual |
| App identity | `ru.cisgame.colorquarter`, `versionCode=1`, `versionName=1.0.0`; checked from merged release manifest and release `BuildConfig` | Pass |
| Backup/privacy posture | `android:allowBackup="false"` plus explicit backup/data extraction exclude-all rules in merged release manifest | Pass |
| Permissions | No Android system permissions requested | Pass |
| Store graphics | 512x512 32-bit PNG icon, <=1024 KB and fully opaque alpha; 1024x500 RGB feature graphic; 6 phone screenshots at 1080x2400 8-bit RGB PNG without alpha; alt text pack prepared | Pass |
| Data Safety | No data collected/shared; no analytics/ads/IAP/backend | Pass |

## Manifest Notes
Merged release manifest contains AndroidX internal signature-level permission:

```text
ru.cisgame.colorquarter.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
```

This is an app-scoped AndroidX compatibility permission, not a dangerous Android system permission, and it does not create a runtime permission prompt. The project should keep declaring in Play Console that the app does not request user-facing permissions, while internal technical documentation should mention this merged-manifest entry.

Release validation also checks:
- release `BuildConfig.APPLICATION_ID`, `VERSION_CODE`, `VERSION_NAME` and `BUILD_TYPE`;
- app label `Цветной Квартал`;
- launcher icon and round icon references;
- exactly one exported launcher activity;
- no unexpected exported release components.
- Google Play high-res icon size and transparency posture: `play-assets/graphics/app_icon_512.png` must be 512x512, 8-bit RGBA PNG, <=1024 KB and fully opaque.
- Feature graphic format posture: `play-assets/graphics/feature_graphic.png` must be 1024x500, 8-bit RGB PNG without alpha.
- Screenshot format posture: each uploadable phone screenshot in `play-assets/screenshots/phone` must be 1080x2400, 8-bit RGB PNG without alpha.
- Asset alt text posture: `play-assets/metadata/ru-RU/asset_alt_text.md` must contain one <=140-character description for each uploadable Play graphic and screenshot.

## Current Release Artifact
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`
- Local status: build artifact exists, but is unsigned while private `keystore.properties` is absent.
- Upload requirement: create upload keystore, rebuild signed AAB, then upload to Play Console.

## Time-Sensitive Risk
The official target API page checked on 2026-05-27 says that starting 2025-08-31, new apps and app updates must target Android 15 / API 35 or higher. The project currently targets API 35, so it is aligned for the current requirement. Before production upload, re-check the official target API page in Play Console because Google updates this policy annually.

Official preview asset guidance checked on 2026-05-27 says store screenshots must be JPEG or 24-bit PNG without alpha and should show the actual app/game experience. It also recommends alt text for preview assets. The final six phone screenshots were refreshed on `project_52game_emulator`, converted to 8-bit RGB PNG, and paired with localized alt text.
