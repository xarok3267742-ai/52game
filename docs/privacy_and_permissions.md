# Privacy And Permissions

## Данные собираются
Никакие персональные данные не собираются и не передаются.

## Локально сохраняется
- Лучшие результаты уровней.
- Незавершённая попытка: id уровня, текущее поле, число ходов и история undo.
- Флаг прохождения onboarding.
- Настройки haptics/reduced motion/high contrast.

Эти данные хранятся в `SharedPreferences` на устройстве.

Критичные записи прогресса, незавершённой попытки, onboarding state и сброса прогресса выполняются через синхронный `commit()`, чтобы уменьшить риск потери результата при немедленном force-stop/убийстве процесса после действия. Настройки интерфейса остаются асинхронными через `apply()`, потому что они не являются критичным игровым результатом.

При чтении и записи прогресс и незавершённая попытка санитизируются: учитываются только известные уровни каталога, положительные значения ходов в пределах лимита уровня, разблокированность уровня текущим прогрессом, полная история undo для числа ходов, исходный снимок текущей версии уровня, корректный размер поля, коды цветов из палитры уровня, незавершённое состояние поля, факт, что поле уже отличается от стартового, и то, что каждый соседний snapshot можно получить реальным использованным ходом из палитры уровня. Если локальная строка прогресса содержит дубликаты уровня, сохраняется лучший положительный результат. Malformed snapshots с пустыми строками не нормализуются, а отклоняются. Это защищает UI от устаревших или повреждённых локальных записей.

Android Auto Backup отключён через `android:allowBackup="false"`, чтобы локальный прогресс не уходил в облачную резервную копию.

Дополнительно добавлены явные exclude-all правила:
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`

Они исключают `file`, `database`, `sharedpref` и `external` domains из cloud backup и device transfer. Это defensive privacy настройка на случай будущих изменений backup-политики проекта.

## Данные не собираются
- Имя, email, телефон.
- Геолокация.
- Контакты.
- Фото/видео/аудио.
- Device identifiers.
- Advertising ID.
- Платёжные данные.
- User-generated content.

## Permissions
Приложение не запрашивает Android system permissions и не показывает runtime permission prompts.

Merged release manifest содержит AndroidX internal signature-level permission:

```text
ru.cisgame.colorquarter.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
```

Это app-scoped compatibility permission от AndroidX, не dangerous permission и не пользовательский доступ к данным устройства.

## In-App Privacy Surface
В настройках добавлен экран `О приложении и приватность`. Он объясняет:
- приложение работает offline, без аккаунта, рекламы и покупок;
- локально хранятся прогресс, незавершённая попытка, лучший результат и настройки;
- персональные данные, рекламный идентификатор и пользовательский контент не собираются;
- системные permissions не запрашиваются;
- локальные данные можно удалить через сброс прогресса или очистку данных приложения в Android.

## Analytics / Crash Logs / Ads / Payments
В приложении нет аналитики, рекламных SDK, crash-reporting SDK, встроенных покупок, платежей и backend-интеграции.

- Analytics: нет.
- Crash logs SDK: нет.
- Ads: нет.
- Payments/IAP: нет.
- Backend: нет.

Release validation автоматически блокирует добавление распространённых ads, analytics, crash reporting, payment, backend/networking SDK markers и hardcoded `http://` / `https://` URLs в `app/src/main` без отдельного продуктового и privacy-решения.

## Google Play Data Safety Notes
На основе Play Console Help: даже если приложение не собирает данные, Data Safety form и privacy policy link всё равно нужны для production listing. Источник: https://support.google.com/googleplay/android-developer/answer/10787469

Рекомендуемая декларация:
- Data collected: No.
- Data shared: No.
- Data encrypted in transit: Not applicable, app does not transmit user data.
- Account deletion: Not applicable, no accounts.

Release validation проверяет, что эти заявления не расходятся между `play-assets/legal/privacy_policy_ru.*`, `play-assets/metadata/ru-RU/data_safety.md`, `play-assets/metadata/ru-RU/play_console_submission.md`, `play-assets/metadata/ru-RU/app_access_notes.md` и `play-assets/metadata/ru-RU/target_audience_notes.md`.

Финальные заметки для формы: `play-assets/metadata/ru-RU/data_safety.md`.

## Privacy Policy Notes
Privacy policy должна коротко указать:
- Приложение работает offline.
- Персональные данные не собираются и не передаются.
- Прогресс и незавершённая попытка хранятся локально на устройстве.
- Удаление данных возможно через сброс прогресса в настройках или очистку данных приложения в Android settings.

Готовые файлы:
- `play-assets/legal/privacy_policy_ru.md`
- `play-assets/legal/privacy_policy_ru.html`

HTML-файл нужно разместить на публичном HTTPS URL и указать ссылку в Play Console.
