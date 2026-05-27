# Rejected Assets

Дата аудита: 2026-05-24

## Итог
В uploadable release path оставлены только Play-ready PNG assets:

- `play-assets/graphics/app_icon_512.png`
- `play-assets/graphics/feature_graphic.png`
- `play-assets/screenshots/phone/01_onboarding.png`
- `play-assets/screenshots/phone/02_home.png`
- `play-assets/screenshots/phone/03_level.png`
- `play-assets/screenshots/phone/04_victory.png`
- `play-assets/screenshots/phone/05_settings.png`
- `play-assets/screenshots/phone/06_about_privacy.png`

Старые hand-made SVG store creatives перенесены в archive и не используются в приложении, Google Play assets или RC uploadable path.
2026-05-24 validation снова обнаружил duplicate SVG copies в `play-assets/graphics`; checksums matched archived rejected files, поэтому duplicates удалены из uploadable path, а archived copies сохранены как evidence.

## Отклонённые ассеты
| Ассет | Старый путь | Новый путь | Причина | Статус |
|---|---|---|---|---|
| Legacy vector app icon source | `play-assets/graphics/app_icon.svg` | `archive/rejected-assets/play-graphics/app_icon_legacy_vector.svg` | Старый ручной SVG больше не является финальным источником; Play icon заменён ImageGen PNG и проверяется в 512x512 | Rejected, not release path |
| Legacy vector feature graphic source | `play-assets/graphics/feature_graphic.svg` | `archive/rejected-assets/play-graphics/feature_graphic_legacy_vector.svg` | Hand-made SVG содержит embedded text и fake board composition; финальный feature graphic заменён ImageGen PNG без alpha | Rejected, not release path |

## Правила после аудита
- Не добавлять SVG/banner/source creatives в `play-assets/graphics`, если они не являются uploadable Play PNG.
- Не использовать embedded text, fake UI, случайные бейджи и декоративный шум в store creatives.
- Если нужен новый store creative, сначала обновить `docs/art_direction.md` и `docs/asset_prompts.md`, затем создать ImageGen/Figma/design-tool output и проверить в маленьком размере.
- `contact_sheet.png` остаётся только QA preview и не попадает в uploadable screenshots внутри RC pack.
