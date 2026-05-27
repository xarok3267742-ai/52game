# Accessibility Notes

## Что сделано
- Основные кнопки 44dp+.
- Палитра цветов использует swatches с content descriptions: `Выбрать цвет ...`.
- Полезные цвета в палитре добавляют к content description прирост: `прирост N`; текущий цвет и цвета без прироста получают disabled semantics и понятное описание.
- Кнопка подсказки имеет label `Подсказка`; текст подсказки не зависит только от цвета, прямо называет следующий цвет и ожидаемый прирост клеток.
- Игровое поле имеет content description с размером поля.
- Карточки уровней имеют разные descriptions для доступного, закрытого и пройденного уровня; для пройденных уровней озвучиваются звёзды и лучший результат.
- Важные действия не зависят только от цвета: есть ходы, лимит, проценты, result text, звёзды, числовой star-progress и числовые метки цветов в high contrast режиме.
- Звёздный темп выводится текстом (`Темп: ★★★ ещё доступно`), поэтому потеря 3-звёздочного или 2-звёздочного результата не обозначается только цветом.
- Feedback последнего хода выводится текстом с числом новых клеток и корректными русскими plural forms, поэтому эффект хода не передаётся только цветом или анимацией.
- Захваченный участок поля выделяется рамкой, поэтому состояние управляемой области не зависит только от совпадения цвета плиток.
- Настройки high contrast и reduced motion.
- High contrast добавляет числовые метки цветов на игровое поле и палитру, чтобы игра не полагалась только на различение оттенков.
- Switch controls получили semantics labels.
- Тексты крупные, без отрицательного letter spacing.
- Result panel виден без обязательной прокрутки после победы/поражения.
- Экран `О приложении` использует обычный текст и карточки с достаточными отступами; путь назад доступен как button с label `Назад`.
- Onboarding сделан прокручиваемым, чтобы CTA оставался доступен при крупном системном шрифте.
- App background подключён как декоративная ImageGen WebP с `contentDescription = null`, поэтому не добавляет лишний элемент в accessibility tree.
- Onboarding illustration подключена как ImageGen WebP и имеет русское `contentDescription`, описывающее цветное поле и подсвеченный захваченный участок.
- Home illustration подключена как ImageGen WebP и имеет русское `contentDescription`, описывающее цветной квартал с маршрутом захвата плиток.
- Level illustration подключена как ImageGen WebP и имеет русское `contentDescription`, описывающее цветной квартал с подсвеченным маршрутом захвата.
- Действия уровня разделены на три подписанные кнопки `Отмена`, `Подсказка`, `Заново`; каждая имеет button semantics, content description и disabled state для недоступной отмены. Декоративная action-panel texture имеет `contentDescription = null`, а смысл действий задаётся native vector icons, labels и semantics.
- Top-bar navigation uses native vector icons for `Назад` and `Настройки`, each with button semantics and localized content description; no emoji/glyph rendering dependency remains for these controls.
- Victory illustration подключена как ImageGen WebP и имеет русское `contentDescription`, описывающее собранный квартал со звёздами.
- Defeat illustration подключена как ImageGen WebP и имеет русское `contentDescription`, описывающее почти собранный квартал с оставшимися цветными плитками.
- Основные CTA и level cards используют минимальную высоту вместо жёсткой фиксированной высоты, чтобы текст не обрезался при font scale 1.5x.
- Star-progress на главном экране показывает число набранных звёзд и максимум, поэтому оценка не зависит только от визуального символа `★`.
- Для количества звёзд добавлен Android `plurals`, чтобы TalkBack получал корректный русский текст: `1 звезда`, `2 звезды`, `3 звезды`.
- Для Google Play preview assets подготовлен `play-assets/metadata/ru-RU/asset_alt_text.md`: короткие ru-RU описания app icon, feature graphic и шести phone screenshots.

## Контраст
Основной текст `#17242E` на `#F7F6F0`/white. Цветовые плитки используются как игровая информация, но high contrast добавляет более плотные границы и числовые метки.

## Проверено
- UI tree на эмуляторе API 35.
- Onboarding, home, game, victory, settings, about/privacy.
- Tap targets и русские labels в UI dump.
- Font scale 1.3x: onboarding, home, settings, about/privacy; screenshots сохранены в `qa-artifacts/font-scale-1.3`.
- Font scale 1.5x: найдено и исправлено обрезание onboarding CTA и level card labels; повторная проверка пройдена, screenshots сохранены в `qa-artifacts/font-scale-1.5`.
- High contrast markers: UI dump подтвердил числовые метки на поле и content descriptions палитры формата `Выбрать цвет 1: Лагуна`; artifacts сохранены в `qa-artifacts/high-contrast-markers`.
- Hint button: covered by localized strings and unit-tested hint logic with gain value; 2026-05-21 visual UI dump on the dedicated `project_52game_emulator` confirmed gain-aware hint copy and the separate labeled `Подсказка` button.
- Captured-region highlight: covered by `GameEngine.capturedMask` unit test and debug/lint/test build pass; 2026-05-21 visual smoke confirmed board rendering with the updated compact level layout.
- Palette gain preview and no-gain disabled state: covered by `GameEngine.expansionGain` unit test and debug/lint/test build pass; 2026-05-21 visual smoke confirmed palette remains reachable after level banner/action-bar polish.
- Last-move gain feedback: covered by `GameEngine.capturedCells` unit test and debug/lint/test build pass.
- Star pace feedback: covered by `LevelCatalog.starsStillAvailable` unit test and debug/lint/test build pass.
- 2026-05-20: после замены onboarding mosaic на bitmap illustration `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` прошёл успешно; отдельный ручной TalkBack pass с новой иллюстрацией остаётся перед публикацией.
- 2026-05-20: после добавления decorative app background `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` прошёл успешно; ручной TalkBack pass остаётся перед публикацией.
- 2026-05-20: после добавления home illustration `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` прошёл успешно; отдельный ручной TalkBack pass с обновлённым главным экраном остаётся перед публикацией.
- 2026-05-20: после добавления victory illustration `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` прошёл успешно; отдельный ручной TalkBack pass с обновлённой result panel остаётся перед публикацией.
- 2026-05-20: после добавления defeat illustration `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` прошёл успешно; отдельный ручной TalkBack pass с обновлённой loss result panel остаётся перед публикацией.
- 2026-05-21: после добавления level illustration, отдельных action buttons и motion polish `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` прошёл успешно; UI dump/screenshots с dedicated AVD сохранены в `qa-artifacts/level-visual-refresh`. Ручной TalkBack pass остаётся перед публикацией.
- 2026-05-21: после добавления action-panel texture, vector icons и press-state feedback `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` прошёл успешно; UI dump/screenshots с dedicated AVD сохранены в `qa-artifacts/action-panel-refresh`, labels `Отмена`, `Подсказка`, `Заново` и disabled state подтверждены.
- 2026-05-21: финальные Play screenshots сняты на dedicated AVD `project_52game_emulator`; settings/about screenshots подтверждают новые vector back/settings controls, а level screenshot подтверждает action-panel controls без наложений.

## Остаточные проверки перед публикацией
- Ручная проверка TalkBack на физическом устройстве.
- Проверка дальтонизма на внешнем инструменте, если появится возможность; базовая защита уже есть через high contrast markers.
