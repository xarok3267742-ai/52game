# Performance Notes

## Cold start
Приложение без сети; cold start ограничен Activity, Compose composition и декодированием небольшого decorative background WebP. Onboarding, home, level/action и result screens дополнительно декодируют компактные WebP.

## Размер
- Debug APK: 11,969,062 bytes on latest validation.
- Release AAB после R8/resource shrink: 2,761,321 bytes on latest validation.

## Automated Budgets
`scripts/validate_release_candidate.sh` blocks performance regressions with these v1.0 budgets:
- release AAB <= 8 MiB;
- debug APK <= 25 MiB;
- in-app WebP total <= 256 KiB;
- one in-app WebP <= 96 KiB;
- no audio/video/custom font resources in `app/src/main/res`.

## Main thread
- Игровая логика работает на маленьких списках 5x5-7x7.
- Captured-region mask пересчитывается только для текущего маленького поля при Compose recomposition; размер поля максимум 7x7, поэтому нагрузка пренебрежима.
- Palette gain preview считает немедленный прирост для 4-5 цветов на маленьком поле максимум 7x7; это дешёвая локальная операция без IO и сети.
- Нет тяжёлого IO, сети, парсинга JSON или bitmap decode.
- SharedPreferences используются только для маленького progress/settings/active-attempt payload.
- Critical progress/onboarding/active-attempt writes use synchronous SharedPreferences `commit()` for better durability after immediate process kill; settings still use async `apply()` because they are non-critical and tiny.
- Startup clears a corrupted active-attempt payload only when active-attempt keys actually exist, so clean launches do not perform unnecessary SharedPreferences writes.
- Progress payload is sanitized on load/save and only keeps known catalog level ids with positive move counts.

## FPS/плавность
- Анимации смены цвета — короткий `tween(180)`.
- Подсказка, палитра и result panel используют короткие fade/slide/scale transitions 120-220ms; palette swatches, captured cells и action buttons имеют небольшое scale-состояние без физики и бесконечных анимаций.
- Есть настройка reduced motion, переключающая цветовые/scale анимации на snap там, где это влияет на игровой процесс.

## Ассеты
- В приложении семь bitmap-ассетов: `app_background.webp`, 720x1280 RGB WebP, 10 KB; `onboarding_illustration.webp`, 840x500 RGB WebP, 17 KB; `home_illustration.webp`, 720x405 RGB WebP, 32 KB; `level_illustration.webp`, 720x405 RGB WebP, 40 KB; `action_panel_texture.webp`, 720x240 RGB WebP, 5 KB; `victory_illustration.webp`, 720x405 RGB WebP, 22 KB; `defeat_illustration.webp`, 720x405 RGB WebP, 21 KB.
- Иконки, splash, top-bar navigation icons и action icons — vector drawable.

## Зависимости
- Только AndroidX Activity Compose, Compose UI, Material3 и JUnit.
- Нет analytics/ads/crash SDK.

## Остаточные риски
- Не выполнялся профилинг на физическом low-end устройстве.
- Не измерялся cold start через Macrobenchmark, так как для MVP это избыточно.
- Synchronous critical SharedPreferences commits are small and user-action-driven, but physical low-end QA should still include quick taps through level completion and reset.
