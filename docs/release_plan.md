# Release Plan

Дата обновления: 2026-05-27

## Текущий статус
`Цветной Квартал` находится на стадии release candidate hardening:

- native Android MVP реализован на Kotlin + Jetpack Compose;
- основной игровой цикл работает: onboarding, главный экран, выбор уровня, игра, подсказка, отмена хода, рестарт, победа/поражение, настройки, экран приватности;
- 36 offline-уровней заданы в `LevelCatalog` и покрыты unit-тестами игровой логики;
- прогресс, настройки и незавершённая попытка хранятся локально через SharedPreferences;
- backend, аккаунты, аналитика, реклама, IAP и runtime permissions отсутствуют;
- release AAB собирается, но остаётся unsigned без приватного production upload key;
- Play metadata, privacy policy, Data Safety notes, icon, feature graphic и screenshots подготовлены локально;
- release automation уже проверяет build/test/lint/bundle, Gradle wrapper checksum, manifest privacy, release identity, Play metadata, graphics, security surface, placeholder scan, localization surface, AAB content, RC manifest structure/path consistency, critical file byte/SHA inventory и отсутствие локальной machine-specific SDK-конфигурации в release surfaces.

## Product Definition
- Жанр: offline color-flood puzzle.
- Целевая аудитория: русскоязычные Android-пользователи, casual-аудитория, короткие сессии, слабые и средние устройства.
- Core value: быстрая понятная головоломка без аккаунта, интернета, рекламы и сбора данных.
- Монетизация v1.0: отсутствует. Это снижает privacy, UX и Google Play review risk.
- Язык v1.0: русский.

## Архитектурные проблемы
| Проблема | Статус | План |
|---|---|---|
| UI находится в одном крупном Compose-файле | Допустимо для MVP, но усложнит v1.1 | Не переписывать перед v1.0; зафиксировать как refactor risk |
| Release signing зависит от приватного `keystore.properties` | Закрыто локально | Upload key сгенерирован вне репозитория; ключи не коммитить и не загружать в GitHub |
| Active attempt зависит от локального snapshot codec | Закрыто тестами | Сохранять sanitization tests и release validation |
| SharedPreferences critical writes должны переживать закрытие приложения | Закрыто | `commit()` используется для прогресса, onboarding и active attempt; force-stop/relaunch active-attempt smoke пройден на `project_52game_emulator` |

## UI/UX Проблемы
| Проблема | Статус | План |
|---|---|---|
| Store screenshots устарели после ImageGen level/action-panel polish | Исправлено | 6 phone screenshots пересняты на dedicated AVD `project_52game_emulator` и сохранены как 24-bit RGB PNG |
| Верхние кнопки навигации используют текстовые символы | Исправлено | `Назад` и `Настройки` заменены на vector icons |
| Нужно подтвердить updated action panel в store screenshot | Исправлено | `03_level.png` отражает текущий action panel |
| Главный экран недостаточно явно объяснял следующий шаг | Исправлено | Добавлена panel `Следующая цель` с уровнем, районом, лимитом, 3-star target, remaining count и CTA |
| Сетка уровней была фиксирована на 3 колонки | Исправлено | Добавлена adaptive grid: 3 колонки на phone width, 4 колонки от 520dp |
| Цель уровня была распределена между counters | Исправлено | Progress panel уровня теперь показывает `Цель: собрать 100% за N ходов` |
| Малые экраны/крупный шрифт | Частично проверено | Сохранить manual QA risk для physical device и TalkBack |

## Контент / Сюжет
| Проблема | Статус | План |
|---|---|---|
| Сюжета как narrative campaign нет | Осознанное решение | Оставить как абстрактный город; `story_bible.md` описывает мир и тон |
| 36 уровней и русские названия | Готово | Unit-тесты каталога должны оставаться зелёными |
| Placeholder-контент | Не найден в release surface | Поддерживать validation scan |

## Ассеты
| Проблема | Статус | План |
|---|---|---|
| App icon и feature graphic созданы через ImageGen и обработаны под Play размеры | Готово | Не считать SVG source uploadable Play asset |
| In-app WebP assets оптимизированы | Готово | Сохранить asset manifest |
| Screenshot PNG имеют alpha channel после Android screencap | Исправлено | Store screenshots конвертированы в RGB PNG; validation блокирует alpha |
| Play asset alt text отсутствовал как handoff copy | Исправлено | `asset_alt_text.md` создан и валидируется для всех uploadable graphics/screenshots |
| Нужен audit rejected assets по жёсткому правилу пользователя | Исправлено | `docs/rejected_assets.md` создан; старые SVG store creatives перенесены в `archive/rejected-assets` |

## Производительность
| Проблема | Статус | План |
|---|---|---|
| Runtime лёгкий: Compose UI + small WebP + local logic | Готово | Validation блокирует регрессии: AAB <= 8 MiB, debug APK <= 25 MiB, WebP total <= 256 KiB |
| Нет тяжёлых сетевых/ads/analytics SDK | Готово | Security/dependency scan должен оставаться зелёным |
| Неожиданные новые dependencies | Закрыто | Validation сверяет plugins/repositories/direct dependencies с `docs/dependency_audit.md` |
| Third-party notices отсутствовали | Закрыто | `docs/third_party_notices.md` создан и проверяется release validation |
| FPS/анимации на слабых устройствах | Остаточный риск | Reduced motion включён; физический low-end device pass вручную перед публикацией |

## Сборка / Release Engineering
| Проблема | Статус | План |
|---|---|---|
| Debug build | Должен проходить | Запустить `assembleDebug` |
| Unit tests | Должны проходить | Запустить `test` |
| Android lint | Должен проходить | Запустить `lint` |
| Release AAB | Должен собираться | Запустить `bundleRelease` |
| Gradle wrapper supply chain | Закрыто | Wrapper URL HTTPS, checksum pinned, `validateDistributionUrl=true`, wrapper jar проверяется validation script |
| Build environment provenance | Закрыто | `docs/build_environment.md` фиксирует Gradle/JDK/SDK/toolchain и проверяется validation script |
| Production signing | Готово локально | Upload key создан вне репозитория, signed AAB собран; перед публикацией владелец должен сохранить ключ в безопасном хранилище |
| Signing hygiene | Закрыто | Validation проверяет `.gitignore`, optional `keystore.properties` и отсутствие binary keystore files внутри project tree |
| Local SDK path leakage | Закрыто | Validation проверяет AAB, packaging проверяет RC handoff archive |
| RC manifest consistency | Закрыто | Packaging проверяет release identity, AAB status/path, Play assets, docs, QA pointers, manual blockers и critical file hashes |
| RC publishing pack | Готово | `scripts/package_release_candidate.sh 1.0.0` проходит; archive checksum and critical file manifest validation pass |
| RC archive verification | Готово | `scripts/verify_release_candidate_package.sh 1.0.0` независимо проверяет готовый zip после упаковки |

## Google Play Риски
| Риск | Оценка | План |
|---|---|---|
| Target API policy меняется ежегодно | Средний | На 2026-05-27 проект `targetSdk=35` совпадает с official requirement Android 15 / API 35+ для новых apps/updates; перед upload повторно проверить Play Console |
| Потеря upload key | Высокий manual blocker | Сохранить Desktop private signing handoff в безопасном хранилище; не загружать ключ в GitHub |
| Privacy policy требует public HTTPS URL | Высокий manual blocker | Разместить `play-assets/legal/privacy_policy_ru.html` на публичном HTTPS |
| Store screenshots должны отражать текущую версию | Закрыто | Все шесть пересняты 2026-05-21 на `project_52game_emulator`; home/level пересняты 2026-05-24 после design review polish; Play Console preview всё равно проверить вручную |
| Active attempt должен восстанавливаться после выгрузки процесса | Закрыто | Проверено 2026-05-23 на `project_52game_emulator` после force-stop/relaunch |
| TalkBack/physical device не пройдены | Средний | Выполнить вручную перед production rollout |

## Пошаговый план до RC
1. Обновить release plan и зафиксировать текущие RC-риски.
2. Провести финальный audit code/assets/docs/scripts.
3. Исправить Play screenshot format: 24-bit PNG без alpha. Готово.
4. Переснять 6 store screenshots на `project_52game_emulator`, не используя чужие устройства. Готово.
5. Улучшить верхние icon buttons через vector drawables. Готово.
6. Усилить `scripts/validate_release_candidate.sh`, чтобы screenshots с alpha channel блокировались. Готово.
7. Создать `docs/rejected_assets.md` и зафиксировать visual asset audit. Готово.
8. Обновить docs: Play requirements, UI audit, asset manifest, QA plan, release report, checklist, README/AGENTS при необходимости. Готово.
9. Запустить Gradle checks: `test`, `assembleDebug`, `lint`, `bundleRelease`. Готово, последний full validation Gradle pass: `BUILD SUCCESSFUL in 37s`.
10. Запустить full release validation с isolated flags. Готово, validation completed.
11. Собрать RC publishing pack `1.0.0` и проверить checksum. Готово, archive checksum OK.
12. Усилить validation/package scripts против попадания локальной SDK-конфигурации и machine-specific path fragments в release artifacts. Готово.
13. Закрыть active-attempt force-stop/relaunch smoke на dedicated AVD проекта. Готово.
14. Усилить RC manifest validation перед созданием publishing zip. Готово.
15. Закрепить Gradle wrapper distribution SHA-256 и добавить wrapper validation в release checks. Готово.
16. Добавить в RC manifest размер и SHA-256 для критичных upload/handoff файлов и валидировать их перед упаковкой. Готово.
17. Подготовить Play asset alt text и добавить validation/handoff. Готово.
18. Добавить standalone verifier для готового RC archive. Готово.
19. Добавить signing hygiene validation. Готово.
20. Добавить dependency allowlist audit и validation. Готово.
21. Добавить third-party notices для runtime/build/test dependency families и включить в validation/RC handoff. Готово.
22. Добавить build environment provenance и включить в validation/RC handoff. Готово.
23. Провести design/layout/code review и внедрить выбранные улучшения UX. Готово: next-goal panel, adaptive level grid, explicit level objective.
24. Обновить home/level Play screenshots после design review polish. Готово, 2026-05-24 на `project_52game_emulator`.
25. Провести свежую recertification по RTF goal 2026-05-27: official Play docs, full validation, dedicated AVD smoke, docs update, RC packaging. Готово.
26. В финальном ответе указать фактическую готовность, путь к AAB/RC pack и ручные Play Console действия. Готово в handoff response.

## Rollback
Если Play review или pre-launch report найдут blocker, остановить rollout, исправить проблему, поднять `versionCode=2`, пересобрать signed AAB и загрузить новый release.

## Future v1.1
- Разделить крупный Compose-файл на feature components.
- Добавить новые level packs.
- Добавить опциональный sound pack без analytics/ads.
- Подготовить английскую локализацию.
- Рассмотреть tablet-specific screenshots после mobile launch.
