# Third-Party Notices

Дата проверки: 2026-05-23

## Scope
Документ фиксирует open-source dependency surface для release candidate `Цветной Квартал` 1.0.0. Приложение распространяется как offline Android app без backend, рекламы, аналитики, IAP и аккаунтов.

Проверочный источник в проекте:

```bash
./gradlew --no-daemon --no-configuration-cache --console=plain :app:dependencies --configuration releaseRuntimeClasspath
```

Release validation дополнительно сверяет прямые Gradle plugins/repositories/dependencies с `docs/dependency_audit.md`.

## Runtime Third-Party Components
| Component family | How used | License posture |
|---|---|---|
| AndroidX Activity / Core / Lifecycle / SavedState / ProfileInstaller / related AndroidX runtime artifacts | Android application lifecycle, Compose host activity, compatibility helpers | Apache License 2.0 |
| Jetpack Compose UI / Runtime / Foundation / Material 3 / Animation / related Compose runtime artifacts | User interface, board, controls, dialogs, motion | Apache License 2.0 |
| Kotlin standard library | Kotlin runtime support | Apache License 2.0 |
| Kotlinx coroutines / serialization transitive runtime artifacts | Transitive runtime support from AndroidX/Compose graph | Apache License 2.0 |
| JetBrains annotations | Transitive annotations metadata | Apache License 2.0 |
| JSpecify | Transitive nullness annotations metadata | Apache License 2.0 |
| Guava ListenableFuture standalone artifact | Transitive future compatibility artifact through AndroidX | Apache License 2.0 |

## Build-Time / Test-Time Components
| Component family | How used | Release APK/AAB status | License posture |
|---|---|---|---|
| Android Gradle Plugin | Android build pipeline | Build-time only | Apache License 2.0 |
| Gradle wrapper/distribution | Build automation | Build-time only | Apache License 2.0 |
| Kotlin Gradle Plugin / Compose Compiler Plugin | Kotlin and Compose compilation | Build-time only | Apache License 2.0 |
| JUnit 4.13.2 | JVM unit tests for game logic/catalog/storage sanitization | Test-time only, not a release runtime dependency | Eclipse Public License 1.0 |
| Hamcrest transitive test dependency | Assertions used by JUnit dependency graph | Test-time only, not a release runtime dependency | BSD-style license |

## Not Present
- GPL, LGPL or AGPL direct dependencies.
- Ads SDKs.
- Analytics or attribution SDKs.
- Crash reporting SDKs.
- Billing/IAP/payment SDKs.
- Backend/networking SDKs.
- Image loading libraries.
- Game engines.
- Audio/video/custom font packages.

## Distribution Notes
- No third-party brands, characters, copyrighted content or external media assets are bundled.
- ImageGen-generated assets are treated as project-owned generated assets and documented in `docs/asset_manifest.md` and `docs/asset_prompts.md`.
- Google Play upload package includes this notice in `release-candidate/1.0.0/docs/third_party_notices.md`.
- Before a legal/commercial launch under a company account, the publisher should run a final legal review against the exact signed AAB and organization policy.

## Maintenance Rule
Any new Gradle plugin, repository, direct dependency or intentionally added transitive SDK family must update:
- `docs/dependency_audit.md`;
- this document;
- `docs/security_audit.md`, if privacy/security posture changes;
- `scripts/validate_release_candidate.sh` dependency and notices validation.
