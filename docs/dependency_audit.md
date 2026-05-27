# Dependency Audit

Дата проверки: 2026-05-23

## Цель
Зафиксировать минимальный dependency surface для release candidate `Цветной Квартал`. Приложение должно оставаться offline-first, без backend, рекламы, аналитики, crash SDK, платежей и тяжёлых runtime-библиотек.

## Approved Gradle Plugins
| Plugin | Version | Purpose |
|---|---:|---|
| `com.android.application` | `8.13.2` | Android application build |
| `org.jetbrains.kotlin.android` | `2.2.20` | Kotlin Android compilation |
| `org.jetbrains.kotlin.plugin.compose` | `2.2.20` | Compose compiler integration |

## Approved Repositories
- `google()`
- `mavenCentral()`
- `gradlePluginPortal()` only for plugin resolution

`settings.gradle.kts` keeps `RepositoriesMode.FAIL_ON_PROJECT_REPOS`, so modules cannot add ad-hoc repositories.

## Approved Direct Dependencies
| Configuration | Dependency | Purpose |
|---|---|---|
| `implementation` | `androidx.compose:compose-bom:2026.04.01` | Compose version alignment |
| `androidTestImplementation` | `androidx.compose:compose-bom:2026.04.01` | Compose test dependency alignment |
| `implementation` | `androidx.activity:activity-compose:1.13.0` | Compose Activity entry point |
| `implementation` | `androidx.compose.material3:material3` | Material 3 UI components |
| `implementation` | `androidx.compose.ui:ui` | Compose UI runtime |
| `implementation` | `androidx.compose.ui:ui-tooling-preview` | Preview annotations/tooling hooks |
| `debugImplementation` | `androidx.compose.ui:ui-tooling` | Debug-only Compose tooling |
| `debugImplementation` | `androidx.compose.ui:ui-test-manifest` | Debug/test manifest support |
| `testImplementation` | `junit:junit:4.13.2` | Unit tests |

## Explicitly Not Present
- Ads SDKs.
- Analytics or attribution SDKs.
- Crash reporting SDKs.
- Billing/IAP/payment SDKs.
- Firebase/backend/networking SDKs.
- Image loading libraries.
- Game engines.
- Audio/video/font packages.

## Automated Enforcement
`scripts/validate_release_candidate.sh` enforces:
- exact approved plugin inventory and versions;
- `google()`/`mavenCentral()` repository posture with `FAIL_ON_PROJECT_REPOS`;
- exact approved direct dependency coordinates and dependency declarations;
- forbidden SDK marker scan for ads, analytics, crash reporting, payments and backend/networking;
- presence and basic completeness of `docs/third_party_notices.md`;
- app source scan for secret-like values and non-Android-schema URLs.

Any new direct dependency or intentionally introduced transitive SDK family must be treated as a product/release decision and documented here plus in `docs/third_party_notices.md` before release validation should be updated.
