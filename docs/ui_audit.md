# UI Audit

## Design System

| Токен | Значение |
|---|---|
| Background | `#F7F6F0` |
| Ink | `#17242E` |
| Muted text | `#5E6872` |
| Surface | `#FFFFFF` |
| Line | `#E2E0D7` |
| Success | `#247C5C` |
| Warning | `#E16B3D` |
| Tile Lagoon | `#2BB3A3` |
| Tile Sun | `#F2B84B` |
| Tile Berry | `#E85D75` |
| Tile Mint | `#75C978` |
| Tile Violet | `#6C7AE0` |

## Typography
- Display: 34sp bold.
- Headline: 26sp bold.
- Title: 20sp/16sp semibold.
- Body: 16sp/14sp.
- Letter spacing: 0.

## Spacing
- Screen horizontal padding: 18-20dp.
- Panel padding: 12-16dp.
- Grid gap: 5dp.
- Control gap: 10-14dp.

## Radii, Shadows, Elevation
- Buttons, cards, board cells: 7-8dp.
- No nested cards.
- Elevation only for board/onboarding mosaic, low and functional.

## States
- Button states: enabled/disabled alpha and Material defaults.
- Input states: no text inputs in MVP.
- Loading: not needed, app is local-only.
- Empty: level catalog fallback.
- Error: unknown level fallback.
- Screen background: decorative ImageGen city-block WebP behind every `ScreenFrame`, rendered at low alpha and excluded from accessibility.
- Success: result panel with compact ImageGen victory illustration, stars, best-result feedback, home star progress and completed level result summary.
- Failure: out-of-moves panel with compact ImageGen defeat illustration and captured-percent feedback.
- Onboarding hero: ImageGen WebP illustration in a white 8dp card, with the same city-block palette and no text inside the asset.
- Home hero: compact ImageGen WebP banner above progress, with the same city-block palette, no text inside the asset and fixed 118dp height to avoid layout shift.
- Home next-goal panel: the main CTA is now attached to a contextual `Следующая цель` panel with level title, district, move limit, 3-star target, remaining level count and best-result/complete hint.
- Home level grid: adaptive 3-column phone layout switches to 4 columns on wider screens from 520dp, keeping tap targets readable without leaving excessive empty width.
- Level hero: compact ImageGen WebP banner above the action controls, same city-block palette, no text inside the asset and fixed 76dp height to preserve board/palette access on tall phone screens.
- Level action bar: three separate labeled 8dp buttons for `Отмена`, `Подсказка`, `Заново`; disabled undo is visually muted and exposed as disabled to accessibility services. The bar uses a subtle ImageGen texture behind the controls, while the interactive buttons use native vector icons and readable Russian labels.
- Level objective: progress panel begins with `Цель: собрать 100% за N ходов`, so the player sees the win condition as one sentence before reading counters.
- Destructive attempt actions: active level back/restart asks for confirmation before losing current moves.
- High contrast: tile borders become stronger and board/palette colors get numeric markers.
- Move pressure: level header shows moves, remaining moves and limit as separate compact counters.
- Star target: level progress panel shows the exact `3★` move target before the result screen.
- Star pace: while a level is active, the progress panel shows the best star result still reachable by current move count.
- Hint: level header includes a compact `?` action; the hint appears as a small amber helper panel, shows the recommended color plus expected `+N` gain, does not consume a move, and shows a localized unavailable message when no useful color exists.
- Board captured region: cells connected to the upper-left origin receive a subtle selection border, so the player can distinguish controlled tiles from same-colored but disconnected tiles.
- Palette gain preview: useful colors show a compact `+N` label for immediate captured-cell growth; no-gain colors are disabled and screen-reader labels describe the disabled state.
- Last-move feedback: after a consumed move, the progress panel shows localized text `Последний ход: +N клеток`; undo/restart clears it so stale feedback is not shown.
- Motion polish: hint/palette/result panels use short fade/slide/scale transitions, palette swatches slightly scale by state, captured board cells receive a restrained emphasis animation, action buttons compress on press, and reduced motion switches gameplay-relevant transitions to snap.

## Проверка сценариев
- Первый запуск: onboarding объясняет правила.
- Главный экран: progress, star total, continue, levels, settings visible.
- Основной цикл: board, moves, remaining moves, limit, 3-star target, star pace, last-move gain, palette, hint, undo, reset, confirmation before losing an active attempt.
- Result: победа/поражение, звёзды, первый проход/новый рекорд/сохранённый рекорд, процент собранного поля при поражении, retry, next/home.
- Настройки: haptics, reduced motion, high contrast with numeric color markers, reset progress, privacy note.
- Back: settings возвращает на home; game просит подтверждение, если текущая попытка уже началась.
- Background/restart: прогресс сохраняется.
- Маленькие экраны: контент прокручивается; result panel заменяет palette, чтобы кнопки были видны.
- Большие экраны: board сохраняет квадратный формат.
- Tap targets: основные кнопки 44-58dp.

## Исправления после QA
- Панель результата сначала появлялась ниже палитры и на эмуляторе частично уходила за нижний край. Исправлено: после завершения уровня result panel заменяет palette.
- Переключателям настроек добавлены semantics labels.
- Добавлен star-progress на главном экране: игрок видит не только `пройдено / всего`, но и общий результат `★ / максимум`; карточки пройденных уровней показывают звёзды и лучший ход.
- Star-progress проверен через UI dump: `Звёзды` и `★ 0 / 108` видны на home после onboarding.
- Level card semantics уточнены: доступный уровень озвучивает район, закрытый сообщает статус, пройденный озвучивает звёзды и лучший результат.
- Result panel получил рекордный feedback: первое прохождение, новый лучший результат или сохранённый лучший результат.
- Defeat panel получил captured-percent feedback: игрок видит, сколько поля было собрано до окончания ходов.
- Active attempt guard добавлен для back и restart: случайное нажатие больше не теряет текущие ходы без явного подтверждения.
- Active attempt guard проверен на эмуляторе через UI dump: после одного хода показаны оба диалога, `Отмена` сохраняет попытку.
- High contrast больше не ограничивается границами: на поле и палитре появляются числовые метки цветов; UI dump подтвердил `Выбрать цвет 1: Лагуна` и видимые метки на поле.
- Добавлен отдельный счётчик оставшихся ходов: UI dump подтвердил `Ходы 0`, `Ост. 8`, `Лимит 8` на старте уровня и `Ходы 1`, `Ост. 7`, `Лимит 8` после первого хода.
- Добавлена явная цель на 3 звезды: UI dump подтвердил `Цель 3★: 6 ходов` на первом уровне, при этом палитра осталась видимой.
- Добавлена подсказка следующего цвета: `?` показывает локализованную helper panel вида `Подсказка: Солнце, +3 клетки`; если полезного цвета нет, показывается локализованное объяснение вместо молчаливого нажатия. Логика выбора цвета и прироста покрыта unit-тестами и не предлагает цвет, который не расширяет захваченный участок.
- Захваченный участок теперь подсвечивается на поле: игрок видит, какие клетки уже входят в управляемую область, даже если на поле есть отдельные плитки того же цвета.
- Палитра получила preview прироста: цветовые кнопки показывают `+N`, если выбор сразу присоединит новые клетки. Цвета без прироста больше не кликабельны, поэтому случайное нажатие не тратит ход. В high contrast режиме сохраняются числовые маркеры цвета и добавляется прирост.
- Добавлен feedback последнего успешного хода: прогресс-панель показывает `Последний ход: +N клеток`, чтобы пользователь видел эффект уже сделанного выбора, а не только прогноз на палитре.
- Добавлен звёздный темп активной попытки: прогресс-панель показывает, достижимы ли ещё `3★`, `2★` или `1★` по текущему числу ходов.
- Onboarding mosaic заменена на ImageGen illustration 840x500 WebP: первый экран теперь имеет polished visual direction, совпадающий с Play icon/feature graphic.
- Базовый `ScreenFrame` получил subtle ImageGen background pattern 720x1280 WebP: все экраны стали визуально связаны, без нового layout pressure и без accessibility noise.
- Home screen получил ImageGen illustration 720x405 WebP: главный экран теперь имеет visual anchor над прогрессом, сохраняя быстрый доступ к `Продолжить`, star-progress и level grid.
- Victory result panel получил компактную ImageGen illustration 720x405 WebP: успешное завершение уровня теперь визуально вознаграждает игрока, не ломая прежний layout и CTA.
- Defeat result panel получил парную ImageGen illustration 720x405 WebP: поражение выглядит как аккуратный retry state, а не как пустой системный message.
- Level screen получил компактную ImageGen illustration 720x405 WebP, обработанную до 40 KB: уровень теперь визуально связан с onboarding/home/result, но баннер сжат до 76dp, чтобы не вытеснять board и palette.
- Верхний ряд уровня разгружен: undo/hint/restart вынесены из icon-only header в отдельные подписанные кнопки с крупными tap targets.
- Action bar получил ImageGen texture 720x240 WebP весом 5 KB и нативные vector icons для undo/hint/restart. Кнопки теперь выглядят как отдельные controls, имеют press-scale feedback и сохраняют readable labels.
- Верхние кнопки `Назад` и `Настройки` переведены с текстовых glyph symbols на нативные vector icons, чтобы UI не зависел от конкретного Android-шрифта.
- Подсказка стала gain-aware и анимированной: панель появляется мягко и показывает `Подсказка: Солнце, +2 клетки` на проверенном уровне.
- Проверка на dedicated AVD `project_52game_emulator`: визуальный smoke сохранил `qa-artifacts/level-visual-refresh/06_hint_compact.png`; UI dump подтвердил ImageGen-баннер, кнопки `Отмена`, `Подсказка`, `Заново`, gain-aware hint, board и palette без наложений.
- Повторная проверка на dedicated AVD `project_52game_emulator`: `qa-artifacts/action-panel-refresh/03_level_start.png` подтвердил action-panel texture, icon buttons, disabled `Отмена`, активные `Подсказка`/`Заново`, board и palette без наложений.
- Design/layout/code review 2026-05-24: home screen получил contextual next-goal panel вместо отдельной standalone CTA-кнопки, level grid стал адаптивным 3/4 columns, а progress panel уровня получила явную строку цели.
- Dedicated AVD visual smoke 2026-05-24: `qa-artifacts/design-review-2026-05-24` подтвердил `Следующая цель`, `Уровень 1: Тихий двор`, `Осталось: 36`, `Продолжить: Тихий двор`, `Цель: собрать 100% за 8 ходов`, board/palette/action buttons без наложений и пустой crash log.
- Play screenshots `02_home.png` и `03_level.png` обновлены после design review на dedicated AVD `project_52game_emulator`; `contact_sheet.png` regenerated for QA preview.

## Остаточные риски
- Не выполнена ручная проверка на физических устройствах с очень большим font scale.
- Play screenshots refreshed on 2026-05-21 on dedicated AVD `project_52game_emulator`; home and level screenshots refreshed again on 2026-05-24 after next-goal/objective/grid polish.
- Store screenshot assets are 1080x2400 RGB PNG without alpha; `contact_sheet.png` is QA-only and excluded from uploadable RC screenshots.
