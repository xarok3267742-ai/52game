# Google Play Data Safety

Дата подготовки: 18 мая 2026 г.

## Рекомендуемые ответы

| Вопрос Play Console | Ответ для MVP |
|---|---|
| Does your app collect or share any of the required user data types? | No |
| Is all user data collected by your app encrypted in transit? | Not applicable |
| Do you provide a way for users to request that their data is deleted? | Not applicable for collected data; local progress can be reset in app settings |
| Data shared with third parties | No |
| Data collected | No |
| Security practices | App works offline; no user data transmitted |

## Что хранится только локально

- Прогресс уровней.
- Незавершённая попытка: уровень, текущее поле, число ходов и история отмены хода.
- Лучший результат.
- Флаг прохождения onboarding.
- Настройки приложения.

Эти данные не покидают устройство и не считаются collected/shared для Play Data Safety, потому что приложение не передаёт их разработчику или третьим лицам.

## Permissions

Приложение не запрашивает Android system permissions и не показывает runtime permission prompts. В merged manifest есть только AndroidX internal signature-level permission `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, не связанный со сбором пользовательских данных.

## Privacy Policy URL

Нужно разместить `play-assets/legal/privacy_policy_ru.html` на публичном HTTPS-адресе и вставить URL в Play Console.

Официальная справка: https://support.google.com/googleplay/android-developer/answer/10787469?hl=en
