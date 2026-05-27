# Play Console Submission Pack

Дата подготовки: 27 мая 2026 г.

## App Details

- App name: `Цветной Квартал`
- Default language: Russian (`ru-RU`)
- App or game: Game
- Category: Puzzle
- Contains ads: No
- Paid app: No

## Store Listing

Title:

```text
Цветной Квартал
```

Short description:

```text
Спокойная цветовая головоломка без интернета: соберите квартал за лимит ходов.
```

Full description:

```text
Цветной Квартал — простая offline-головоломка для коротких сессий.

Выбирайте цвета, расширяйте связанный участок от угла и собирайте весь район в один цвет. Игра не требует интернета, аккаунта, покупок или сбора данных.

В первом релизе:
- 36 уровней с постепенным ростом сложности;
- понятный onboarding;
- локальное сохранение прогресса и незавершённой попытки;
- звёзды, рекорды и лучший результат на уровне;
- подсказка, undo и быстрый перезапуск;
- настройки контраста, анимации и тактильного отклика.

Подходит для короткого отдыха: открыть, пройти уровень, закрыть.
```

Release notes:

```text
Первый релиз: 36 уровней, подсказки, звёзды, рекорды и локальный прогресс.
```

## Graphics

- App icon: `play-assets/graphics/app_icon_512.png`
- Feature graphic: `play-assets/graphics/feature_graphic.png`
- Phone screenshots:
  - `play-assets/screenshots/phone/01_onboarding.png`
  - `play-assets/screenshots/phone/02_home.png`
  - `play-assets/screenshots/phone/03_level.png`
  - `play-assets/screenshots/phone/04_victory.png`
  - `play-assets/screenshots/phone/05_settings.png`
  - `play-assets/screenshots/phone/06_about_privacy.png`

## Privacy Policy

Host this file on a public HTTPS URL:

```text
play-assets/legal/privacy_policy_ru.html
```

Then paste that URL into Play Console.

## App Content

- Data Safety: use `play-assets/metadata/ru-RU/data_safety.md`.
- Target Audience: use `play-assets/metadata/ru-RU/target_audience_notes.md`.
- Content Rating: use `play-assets/metadata/ru-RU/content_rating_notes.md`.
- App Access: use `play-assets/metadata/ru-RU/app_access_notes.md`.
- Asset alt text: use `play-assets/metadata/ru-RU/asset_alt_text.md`.
- Permissions: no Android system permissions/runtime prompts. Internal merged-manifest AndroidX permission `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` is not user-facing.

## Release Artifact

Signed local AAB:

```text
app/build/outputs/bundle/release/app-release.aab
```

Current handoff also includes:

```text
<desktop-handoff>/Цветной Квартал - Google Play RC 1.0.0/aab/app-release-signed.aab
```

The upload key is private and must stay outside GitHub. See `docs/signing_guide.md`.
