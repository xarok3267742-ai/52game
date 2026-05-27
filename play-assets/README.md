# Google Play Assets

Этот каталог содержит материалы для ручной загрузки в Google Play Console.

## Metadata
- `metadata/ru-RU/title.txt`
- `metadata/ru-RU/short_description.txt`
- `metadata/ru-RU/full_description.txt`
- `metadata/ru-RU/release_notes.txt`
- `metadata/ru-RU/privacy_policy.md`
- `metadata/ru-RU/play_console_submission.md`
- `metadata/ru-RU/data_safety.md`
- `metadata/ru-RU/content_rating_notes.md`
- `metadata/ru-RU/target_audience_notes.md`
- `metadata/ru-RU/app_access_notes.md`

Fastlane-compatible mirror:
- `../fastlane/metadata/android/ru-RU/title.txt`
- `../fastlane/metadata/android/ru-RU/short_description.txt`
- `../fastlane/metadata/android/ru-RU/full_description.txt`
- `../fastlane/metadata/android/ru-RU/changelogs/default.txt`
- `../fastlane/metadata/android/ru-RU/images/icon/icon.png`
- `../fastlane/metadata/android/ru-RU/images/featureGraphic/feature_graphic.png`
- `../fastlane/metadata/android/ru-RU/images/phoneScreenshots/*.png`

## Legal
- `legal/privacy_policy_ru.md`
- `legal/privacy_policy_ru.html`

## Screenshots
Файлы для загрузки в Google Play:
- `screenshots/phone/01_onboarding.png`
- `screenshots/phone/02_home.png`
- `screenshots/phone/03_level.png`
- `screenshots/phone/04_victory.png`
- `screenshots/phone/05_settings.png`
- `screenshots/phone/06_about_privacy.png`

`screenshots/phone/contact_sheet.png` используется только для внутреннего визуального QA и не копируется в uploadable screenshots внутри RC publishing pack.

## Graphics
Файлы для загрузки в Google Play:
- `graphics/app_icon_512.png`
- `graphics/feature_graphic.png`

В `graphics/` должны оставаться только uploadable PNG. Старые SVG store creatives перенесены в `archive/rejected-assets/play-graphics` и описаны в `docs/rejected_assets.md`.

## Перед публикацией
- Разместить `legal/privacy_policy_ru.html` на публичном HTTPS URL.
- Проверить icon/screenshots на реальном/чистом устройстве без debug suffix.
- Production-signed AAB уже собран локально для RC handoff; upload key хранить вне GitHub.
