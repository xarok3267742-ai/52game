# Content Audit

## Контентные блоки
| Блок | Статус | Проблемы | Исправления |
|---|---|---|---|
| Название приложения | Готово | Нет | `Цветной Квартал` |
| Onboarding | Готово | Нет | 3 коротких правила |
| Названия уровней | Готово | Нет | 36 русскоязычных названий |
| Районы/группы уровней | Готово | Нет | 8 тематических групп |
| Микротексты UI | Готово | Нет | Вынесены в `strings.xml` |
| Result states | Готово | UX-кроп был найден | Result заменяет palette; victory copy explains first clear/new best/kept best; defeat copy shows captured percent |
| Settings copy | Готово | Нет | Понятные описания; high contrast copy отражает числовые метки цветов |
| About/privacy copy | Готово | Нет | Версия, offline/no data, local storage, permissions, deletion |
| Active attempt copy | Готово | Нет | Privacy/local-storage texts mention unfinished attempt persistence |
| Hint copy | Готово | Нет | Level hint uses localized `Подсказка`, gain-aware copy like `Подсказка: Солнце, +3 клетки`, and unavailable-hint copy |
| Level action labels | Готово | Нет | Separate buttons use short Russian labels `Отмена`, `Подсказка`, `Заново` plus fuller accessibility descriptions from `strings.xml`; generated action texture contains no text |
| Navigation icon labels | Готово | Нет | Top-bar icons use native vector drawables with content descriptions `Назад` and `Настройки` |
| Level illustration description | Готово | Нет | Level ImageGen banner has localized description `Иллюстрация: цветной квартал с подсвеченным маршрутом захвата` |
| UI chrome strings | Готово | Нет | Dynamic labels and accessibility descriptions live in `strings.xml`; move/remaining/limit/3-star target counters are localized; level/color names remain authored game content |
| Reset confirmation copy | Готово | Нет | Destructive reset explains deleted data and confirms settings remain |
| Active attempt confirmation copy | Готово | Нет | Back/restart dialogs explain that current moves are not saved before victory |
| Completion-state copy | Готово | Нет | Home screen has explicit `Город собран` final state and replay-final CTA |
| Star-progress copy | Готово | Нет | Home shows `Звёзды`; completed level cards show stars and best result |
| Star-pace copy | Готово | Нет | Level progress panel uses localized text `Темп: ★★★ ещё доступно` |
| Last-move gain copy | Готово | Нет | Level progress panel uses localized plural text `Последний ход: +N клеток` |
| Error/empty states | Готово | Нет | Локальные fallback texts |
| Play Console metadata | Готово | Нет | Title, descriptions, release notes, Data Safety, Target Audience, Content Rating |
| Privacy policy | Готово | Нет | HTML/Markdown для публикации на HTTPS URL; draft-файл с placeholder-контактом удалён из `play-assets` |
| Play asset alt text | Готово | Нет | Для app icon, feature graphic и 6 phone screenshots подготовлены короткие ru-RU описания до 140 символов |

## Удалено/не используется
- Lorem ipsum отсутствует.
- Placeholder-тексты отсутствуют.
- `play-assets` не содержит draft/privacy placeholder files.
- `play-assets/graphics` содержит только uploadable PNG; старые hand-made SVG store creatives перенесены в `archive/rejected-assets/play-graphics` и описаны в `docs/rejected_assets.md`.
- `play_console_submission.md` синхронизирован с canonical `title.txt`, `short_description.txt`, `full_description.txt` и `release_notes.txt`; release validation проверяет это автоматически.
- Технические ошибки пользователю не показываются.
- Незавершённых уровней нет.

## Остаточные риски
- Английская локализация не входит в MVP.
- Play listing copy, app icon, screenshots, feature graphic и privacy policy подготовлены локально; финальный preview нужно проверить в Play Console после загрузки.
- Privacy policy and Data Safety notes updated after adding local active-attempt persistence; no collected/shared data introduced.
