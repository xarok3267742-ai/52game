# Security Audit

Дата проверки: 2026-05-23

## Scope
Проверены release surfaces проекта:
- Android source and resources.
- Gradle signing configuration.
- Play assets and metadata.
- Release AAB contents.
- Public documentation and scripts.

Не проверялись внешние Play Console аккаунт, production upload key и hosted privacy policy URL, потому что это ручные действия вне репозитория.

## Commands
```bash
unzip -l app/build/outputs/bundle/release/app-release.aab

scripts/validate_release_candidate.sh
```

Gradle wrapper checksum source:
```text
https://services.gradle.org/distributions/gradle-8.13-bin.zip.sha256
```

`scripts/validate_release_candidate.sh` now includes an automated security/dependency surface scan. It checks Gradle surfaces for forbidden SDK markers:
- ads: Google Mobile Ads/AdMob, AppLovin, Unity Ads, ironSource;
- analytics/attribution: Firebase Analytics, Google Services plugin, AppMetrica/Yandex Metrica, Amplitude, Mixpanel, AppsFlyer, Adjust, Facebook SDK;
- crash reporting: Crashlytics, Sentry, Bugsnag;
- payments: BillingClient, Stripe, RevenueCat;
- backend/networking: Firebase Auth/Firestore/Database/Storage, Retrofit, OkHttp, Ktor client.

It also scans `app/src/main` for secret-like assignments, private keys, bearer tokens and hardcoded `http://` / `https://` URLs.

## Findings
| Area | Result | Status |
|---|---|---|
| Secrets | No real API keys, tokens, private keys or production keystores found | Pass |
| Keystore handling | Only `keystore.properties.example` is committed; private files are gitignored | Pass |
| Signing hygiene | Validation requires `.gitignore` coverage for private/local signing files and blocks binary upload keystore files inside the project tree | Pass |
| Gradle wrapper | Wrapper distribution URL is HTTPS, `validateDistributionUrl=true`, distribution SHA-256 is pinned, `gradlew` is executable, and wrapper jar contains required Gradle wrapper classes | Pass |
| Build environment provenance | Gradle/JDK/SDK/toolchain facts are documented in `docs/build_environment.md` and checked by validation | Pass |
| Dependency allowlist | Validation enforces approved Gradle plugins, repositories and direct dependencies from `docs/dependency_audit.md` | Pass |
| Third-party notices | Runtime/build/test dependency families and license posture are documented in `docs/third_party_notices.md` and checked by validation | Pass |
| Network/backend | No backend URLs, Firebase, analytics, ads, crash SDK or payment SDK | Pass |
| Automated security scan | `validate_release_candidate.sh` blocks forbidden SDK markers, secret-like values and app-source URLs | Pass |
| Play privacy form consistency | Validation checks privacy policy, Data Safety, App Access, Target Audience and Play submission copy for matching no-collection/no-sharing/no-ads/no-payments/no-account claims | Pass |
| Android permissions | No Android system permissions/runtime prompts in merged release manifest | Pass |
| Internal permission | AndroidX adds app-scoped signature-level `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | Expected |
| Auto Backup | `android:allowBackup="false"` plus explicit backup/data extraction exclude-all rules | Pass |
| Debug-only behavior | Debug app id/version suffix only; user-facing version hides `-debug` | Pass |
| Release identity | Validation checks release package/version/app label/icons/BuildConfig and blocks debug suffix leakage | Pass |
| Local machine path leakage | Release validation inspects the AAB and RC packaging inspects text handoff surfaces for local SDK configuration, user-home paths and foreign-user fragments | Pass |
| RC archive verifier | Standalone verifier checks zip checksum, unzip integrity, internal checksums, manifest critical hashes and local path leakage after packaging | Pass |
| Heavy media/font resources | Release validation blocks audio/video/custom font resources in `app/src/main/res` for v1.0 | Pass |
| Exported components | Release manifest allows only launcher activity plus AndroidX profile installer receiver protected by `android.permission.DUMP` | Pass |
| Release package | AAB exists and is unsigned until production upload key is configured | Manual |
| Release-surface placeholders | No draft privacy policy or placeholder replacement instructions remain in `play-assets` | Pass |

## AAB Content Notes
Release AAB contains one base module with:
- compiled dex;
- optimized resources;
- launcher/splash vector drawables;
- baseline profile metadata;
- R8/proguard mapping metadata;
- AndroidX `libandroidx.graphics.path.so` for supported ABIs.

`DebugProbesKt.bin` is excluded from package resources because it is a Kotlin coroutines debug probe artifact and is not needed for this product's release build.

The release AAB is also checked for local development configuration leakage. The validation rejects packaged `local.properties`, `sdk.dir` assignments, user-specific Android SDK paths and foreign-user fragments.

## RC Package Notes
`scripts/package_release_candidate.sh` rejects:
- an actual `local.properties` file inside the RC handoff archive;
- `sdk.dir` assignments in text files;
- absolute macOS user-home paths in text handoff surfaces;
- foreign-user path fragments.

The RC manifest records byte size and SHA-256 for the AAB, Play app icon, feature graphic, six uploadable phone screenshots, privacy policy HTML, Play Console submission copy and asset alt text. Packaging validates that every critical file record has a relative path, positive size, lowercase SHA-256 and a hash/size matching the file on disk before archive creation.

The RC pack keeps automation scripts for reproducibility, but those scripts are excluded from this text-surface scan because they necessarily contain the detector expressions themselves.

## Residual Risk
- Production upload keystore is not available in the repository by design.
- Play Console pre-launch report has not been run.
- Physical-device QA and TalkBack pass remain manual.
- Hosted privacy policy URL is still manual.
- If future releases intentionally add ads, analytics, crash logs, IAP, networking or backend access, this scan must be updated together with product decision, privacy policy, Play Data Safety and permissions documentation.
