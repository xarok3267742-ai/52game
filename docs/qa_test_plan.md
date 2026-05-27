# QA Test Plan

## Automated
- `./gradlew test`: verifies all catalog levels are solvable, catalog shape is stable, level boards match palettes, scoring works, star-pace availability tracks current move count, hint color selection chooses the largest immediate expansion, exposes the expected gain value and returns no hint when no color expands the captured area, palette expansion-gain preview counts newly captured cells, captured-cell count supports last-move gain feedback, 3-star target calculation matches stored solution length, board snapshot codec round-trips rows/history with stable separators, active-attempt sanitization rejects corrupted/finished/solved/locked-level attempts, unchanged current boards, illegal move transitions, stale initial snapshots, malformed row snapshots and incomplete undo history, captured-percent calculation works, captured-region mask marks only the connected origin area, remaining-move calculation never goes below zero, total star aggregation works, current-color move is free, flood-fill expansion works, legal move transition detection works, invalid tile codes fail fast, unlock logic works, progress sanitization ignores unknown/invalid/out-of-limit saved results, progress codec keeps the best duplicate local result, best-progress updates keep the lowest move count, and best-result improvement detection rejects equal/worse/non-positive results.
- `./gradlew lint`: Android lint.
- `./gradlew assembleDebug`: debug APK.
- `./gradlew bundleRelease`: release AAB.
- `scripts/validate_release_candidate.sh --normalize-only`: moves stale numbered `app/build/* N` directories outside the project root without running Gradle.
- `scripts/verify_release_candidate_package.sh 1.0.0`: independently verifies the generated RC zip checksum, unzip integrity, internal `checksums.sha256`, manifest critical hashes, Play asset inventory and handoff local-path leakage.
- Release validation verifies the Gradle wrapper supply-chain surface: executable `gradlew`, expected HTTPS `gradle-8.13-bin.zip` URL, pinned `distributionSha256Sum`, `validateDistributionUrl=true` and required classes inside `gradle-wrapper.jar`.
- Release validation verifies build environment provenance: `docs/build_environment.md` must document the approved Gradle/JDK/SDK/toolchain posture and dedicated emulator policy.
- Release validation verifies release automation scripts: `validate_release_candidate.sh`, `package_release_candidate.sh` and `verify_release_candidate_package.sh` must exist, be executable and pass `bash -n`.
- Release validation verifies signing hygiene: `.gitignore` must cover `keystore.properties`, binary keystore formats and `local.properties`; binary upload keystore files must not be inside the project tree; optional local `keystore.properties` must be complete and point outside the project tree.
- Release validation verifies dependency governance: approved Gradle plugins, repositories and direct dependencies must match `docs/dependency_audit.md`.
- Release validation verifies third-party notice readiness: `docs/third_party_notices.md` must cover the approved runtime/build/test dependency families and license posture.
- Critical local writes for progress, active attempt, onboarding and reset use synchronous `commit()`; settings remain async `apply()`.
- Release validation verifies release identity from merged manifest, release `BuildConfig` and string resources: package, version, app label, launcher icons, launcher activity, no debug suffix and no unexpected exported components.
- Design review 2026-05-24 verifies home next-goal copy, adaptive level grid, explicit in-level objective and refreshed Play screenshots on the dedicated project AVD.
- RC packaging validates that the requested package version matches release `BuildConfig.VERSION_NAME`, merged manifest `versionName`, `versionCode` and `applicationId`, and rejects debug markers.
- RC packaging keeps only uploadable PNG graphics in `store-listing/graphics`; obsolete SVG store creatives are archived under `archive/rejected-assets/play-graphics` and documented in `docs/rejected_assets.md`.
- RC packaging writes and validates `release_manifest.json`; the manifest lists release identity, AAB signing status/path, uploadable Play graphics/screenshots, QA/source assets, required docs including sanitized `docs/AGENTS.md`, and manual Play blockers. Packaging now fails if any manifest path is missing, absolute, escaping the RC directory, or inconsistent with expected release identity and asset inventory.
- RC packaging records and validates byte size plus lowercase SHA-256 for critical handoff files: AAB, app icon, feature graphic, six phone screenshots, privacy policy HTML, Play Console submission copy and asset alt text.
- Release validation checks `play-assets/metadata/ru-RU/asset_alt_text.md`: one description for each uploadable Play graphic and screenshot, no placeholder markers, each <=140 characters.
- Release validation checks that `play_console_submission.md` matches canonical title, short description, full description and release notes files, and references every required Play asset path.
- Release validation now checks Play graphic formats more strictly: high-res app icon is 512x512 8-bit RGBA PNG, <=1024 KB and fully opaque; feature graphic is 1024x500 8-bit RGB PNG without alpha; all six phone screenshots are 1080x2400 8-bit RGB PNG without alpha.
- In-app app-wide background graphic is an optimized 720x1280 RGB WebP generated through built-in ImageGen; resource compile/lint/test verifies it is packaged and referenced correctly.
- In-app onboarding graphic is an optimized 840x500 RGB WebP generated through built-in ImageGen; resource compile/lint/test verifies it is packaged and referenced correctly.
- In-app home dashboard graphic is an optimized 720x405 RGB WebP generated through built-in ImageGen; resource compile/lint/test verifies it is packaged and referenced correctly.
- In-app level screen graphic is an optimized 720x405 RGB WebP generated through built-in ImageGen; resource compile/lint/test verifies it is packaged and referenced correctly.
- In-app action panel texture is an optimized 720x240 RGB WebP generated through built-in ImageGen; resource compile/lint/test verifies it is packaged and referenced correctly.
- In-app victory result graphic is an optimized 720x405 RGB WebP generated through built-in ImageGen; resource compile/lint/test verifies it is packaged and referenced correctly.
- In-app defeat result graphic is an optimized 720x405 RGB WebP generated through built-in ImageGen; resource compile/lint/test verifies it is packaged and referenced correctly.
- Release validation includes security/dependency surface scan: Gradle files must not contain forbidden ads, analytics, crash, payments or backend/networking SDK markers; `app/src/main` must not contain secret-like assignments, private keys, bearer tokens or hardcoded network URLs.
- Release validation inspects the release AAB for local development configuration leakage: no packaged `local.properties`, `sdk.dir` assignments or user-specific Android SDK path fragments.
- Release validation enforces performance budgets: release AAB <= 8 MiB, debug APK <= 25 MiB, in-app WebP total <= 256 KiB, one in-app WebP <= 96 KiB and no audio/video/custom font resources in `app/src/main/res`.
- RC packaging inspects the handoff archive text surfaces and rejects actual local configuration files, `sdk.dir` assignments, absolute macOS user-home paths and foreign-user fragments.
- Release validation checks privacy/Play form consistency across privacy policy, Data Safety, App Access, Target Audience, Play submission copy and `docs/privacy_and_permissions.md`.
- Accessibility strings: level card descriptions for available/locked/completed states compile through Android resources, including Russian plurals for star counts.

Current JVM test coverage:
- `BoardSnapshotCodecTest`: 2 tests.
- `GameEngineTest`: 14 tests.
- `LevelCatalogTest`: 19 tests.
- `LevelProgressTest`: 8 tests.
- Debug + release unit test variants: 86 executed test cases total, 0 failures.

Latest full release validation:
- 2026-05-27: production upload key generated outside the repository and signed `bundleRelease` passed. `GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache --console=plain" scripts/validate_release_candidate.sh` then passed with Gradle release checks `BUILD SUCCESSFUL in 43s`, signing report `BUILD SUCCESSFUL in 5s`, release config `releaseUpload`, signing hygiene, Play assets/metadata, privacy, manifest, identity, placeholder, localization, AAB content and performance budgets all passing. Signed AAB `2770212 / 8388608` bytes; upload key SHA-256 `3E:3F:2F:53:1D:9C:BF:76:E7:3C:EE:32:83:71:C6:FD:77:54:D8:88:41:0B:30:75:2C:AB:39:24:70:9A:B2:48`.
- 2026-05-27: `GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache --console=plain" scripts/validate_release_candidate.sh` passed for RTF recertification; Gradle release checks completed in 54s with 109 actionable tasks, signing report passed in 4s, release automation scripts, signing hygiene, Gradle wrapper, build environment, local-path leakage, dependency allowlist, third-party notices, Play assets/metadata, privacy, manifest, identity, placeholder, localization, AAB content and performance budgets all passed. Release AAB `2761321 / 8388608` bytes, debug APK `11969062 / 26214400` bytes, in-app WebP total `150084 / 262144` bytes.
- 2026-05-24: `./gradlew --no-daemon --no-configuration-cache --console=plain :app:assembleDebug :app:lint :app:test` passed in 3m 15s after explicitly setting `JAVA_HOME` and `ANDROID_HOME`; an earlier ad-hoc run without `ANDROID_HOME` failed with SDK location missing and did not indicate a project code issue.
- 2026-05-24: first full validation after design-review docs caught reappeared rejected SVG duplicates in `play-assets/graphics`; their SHA-256 matched archived rejected assets, so the uploadable-path copies were removed and `docs/rejected_assets.md` was updated.
- 2026-05-24: `GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache --console=plain" scripts/validate_release_candidate.sh` passed after removing the SVG duplicates; final rerun after documentation updates completed Gradle release checks in 45s with 109 actionable tasks, signing report passed in 5s, release automation scripts, signing hygiene, Gradle wrapper, build environment, local-path leakage, dependency allowlist, third-party notices, Play assets/metadata, privacy, manifest, identity, placeholder, localization, AAB content and performance budgets all passed. Release AAB `2761321 / 8388608` bytes, debug APK `11969062 / 26214400` bytes, in-app WebP total `150084 / 262144` bytes.
- 2026-05-23: `GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache --console=plain" scripts/validate_release_candidate.sh` passed after build environment validation was added; Gradle release checks completed in 37s with 109 actionable tasks, signing report passed in 3s, release automation scripts, signing hygiene, Gradle wrapper, build environment, local-path leakage, dependency allowlist, third-party notices, Play assets/metadata, privacy, manifest, identity, placeholder, localization, AAB content and performance budgets all passed.
- 2026-05-23: `./gradlew --no-daemon --no-configuration-cache --console=plain :app:dependencies --configuration releaseRuntimeClasspath` passed and was used to prepare `docs/third_party_notices.md`.
- 2026-05-23: `./gradlew --version --no-daemon`, Android SDK tool version checks and `docs/build_environment.md` were used to document the RC build environment.
- 2026-05-19: `./gradlew --no-daemon --no-configuration-cache clean test assembleDebug lint bundleRelease --console=plain` passed in 39s with 109 actionable tasks.
- 2026-05-19: `./gradlew --no-daemon --no-configuration-cache :app:signingReport --console=plain` passed in 4s; release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-19: `./gradlew --no-daemon --no-configuration-cache --console=plain :app:assembleDebug :app:lint :app:test` passed after critical SharedPreferences commit hardening in 19s.
- 2026-05-19: `GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache --console=plain" scripts/validate_release_candidate.sh` passed after critical SharedPreferences commit hardening; Gradle release checks completed in 38s with 109 actionable tasks, signing report in 3s, Play asset/metadata consistency, manifest privacy, release identity, placeholder, localization and AAB content checks passed.
- 2026-05-19: `scripts/package_release_candidate.sh 9.9.9` failed as expected before RC mutation with `does not match release VERSION_NAME 1.0.0`; `scripts/package_release_candidate.sh 1.0.0` passed and printed `Packaging release ru.cisgame.colorquarter 1.0.0 (1)`.
- 2026-05-19: RC packaging now emits `release_manifest.json`; JSON parse validation confirmed version `1.0.0`, unsigned AAB status, 2 uploadable graphics and 6 phone screenshots.
- 2026-05-19: Play icon/feature graphic validation hardened; `GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache --console=plain" scripts/validate_release_candidate.sh` passed with Gradle release checks in 39s, signing report in 3s, app icon 19,404 bytes with fully opaque alpha, and RGB feature graphic.
- 2026-05-19: Security/dependency surface scan added to release validation; validation passed with no forbidden SDK markers, secret-like values or app-source URLs.
- 2026-05-19: Privacy/Play form consistency validation added; validation passed for no collection, no sharing, no ads, no purchases, no account, no permissions and local-only storage claims.
- 2026-05-20: Google Play feature graphic refreshed through built-in ImageGen. Final uploadable asset is `play-assets/graphics/feature_graphic.png`, 1024x500 RGB PNG, 435,110 bytes; generated source and processed copy are stored in `qa-artifacts/imagegen`.
- 2026-05-20: Feature graphic refresh is covered by release asset validation: `GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache --console=plain" scripts/validate_release_candidate.sh` passed with Gradle release checks in 42s, signing report in 5s, exact 1024x500 dimensions and RGB/no-alpha format.
- 2026-05-20: Google Play high-res icon refreshed through built-in ImageGen. Final uploadable asset is `play-assets/graphics/app_icon_512.png`, 512x512 RGBA PNG, 276,266 bytes, fully opaque alpha; generated source and processed copy are stored in `qa-artifacts/imagegen`. Release validation passed with Gradle checks in 48s and signing report in 4s.
- 2026-05-20: In-app onboarding illustration refreshed through built-in ImageGen. Final app asset is `app/src/main/res/drawable-nodpi/onboarding_illustration.webp`, 840x500 RGB WebP, 17,538 bytes; generated source and processed copy are stored in `qa-artifacts/imagegen`. `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 19s.
- 2026-05-20: Play onboarding screenshot refreshed from emulator rapid screencap after ImageGen onboarding art integration. This older RGBA capture was superseded on 2026-05-21 by the final RGB screenshot set.
- 2026-05-20: In-app victory result illustration added through built-in ImageGen. Final app asset is `app/src/main/res/drawable-nodpi/victory_illustration.webp`, 720x405 RGB WebP, 22,294 bytes; generated source and processed copy are stored in `qa-artifacts/imagegen`. `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 17s.
- 2026-05-20: In-app defeat result illustration added through built-in ImageGen. Final app asset is `app/src/main/res/drawable-nodpi/defeat_illustration.webp`, 720x405 RGB WebP, 21,208 bytes; generated source and processed copy are stored in `qa-artifacts/imagegen`. `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 6s.
- 2026-05-20: In-app home dashboard illustration added through built-in ImageGen. Final app asset is `app/src/main/res/drawable-nodpi/home_illustration.webp`, 720x405 RGB WebP, 32,752 bytes; generated source and processed copy are stored in `qa-artifacts/imagegen`. `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 10s.
- 2026-05-20: Full release validation after home dashboard ImageGen integration passed: Gradle release checks `BUILD SUCCESSFUL in 50s` with 109 actionable tasks, signing report `BUILD SUCCESSFUL in 4s`, Play graphic/screenshot dimensions, metadata consistency, security/dependency scan, privacy/Play form consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content checks passed. Release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-20: App-wide decorative background added through built-in ImageGen. Final app asset is `app/src/main/res/drawable-nodpi/app_background.webp`, 720x1280 RGB WebP, 10,342 bytes; generated source and processed copy are stored in `qa-artifacts/imagegen`. `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 14s.
- 2026-05-20: Full release validation after app-wide background integration passed: Gradle release checks `BUILD SUCCESSFUL in 39s` with 109 actionable tasks, signing report `BUILD SUCCESSFUL in 3s`, Play graphic/screenshot dimensions, metadata consistency, security/dependency scan, privacy/Play form consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content checks passed. Release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-21: Captured-region highlight added to the board. `GameEngine.capturedMask` is unit-tested; `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 56s. No emulator smoke was run because future Android launches must use only `project_52game_emulator`.
- 2026-05-21: Full release validation after captured-region highlight passed: Gradle release checks `BUILD SUCCESSFUL in 2m 50s` with 109 actionable tasks, signing report `BUILD SUCCESSFUL in 23s`, Play graphic/screenshot dimensions, metadata consistency, security/dependency scan, privacy/Play form consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content checks passed. Release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-21: Palette expansion-gain preview added. Useful palette buttons now show `+N`, no-gain buttons are disabled, and accessibility descriptions include gain/disabled text. `GameEngine.expansionGain` is unit-tested; `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 9s. No emulator smoke was run because future Android launches must use only `project_52game_emulator`.
- 2026-05-21: Full release validation after palette expansion-gain preview passed: Gradle release checks `BUILD SUCCESSFUL in 45s` with 109 actionable tasks, signing report `BUILD SUCCESSFUL in 4s`, Play graphic/screenshot dimensions, metadata consistency, security/dependency scan, privacy/Play form consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content checks passed. Release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-21: Full release validation after disabling no-gain palette colors passed: Gradle release checks `BUILD SUCCESSFUL in 55s` with 109 actionable tasks, signing report `BUILD SUCCESSFUL in 8s`, Play graphic/screenshot dimensions, metadata consistency, security/dependency scan, privacy/Play form consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content checks passed. Release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-21: Last-move gain feedback added to the level progress panel. `GameEngine.capturedCells` is unit-tested; `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 5s. No emulator smoke was run because future Android launches must use only `project_52game_emulator`.
- 2026-05-21: Full release validation after last-move gain feedback passed: Gradle release checks `BUILD SUCCESSFUL in 42s` with 109 actionable tasks, signing report `BUILD SUCCESSFUL in 4s`, Play graphic/screenshot dimensions, metadata consistency, security/dependency scan, privacy/Play form consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content checks passed. Release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-21: Star-pace feedback added to the level progress panel. `LevelCatalog.starsStillAvailable` is unit-tested; `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 11s. No emulator smoke was run because future Android launches must use only `project_52game_emulator`.
- 2026-05-21: Full release validation after star-pace feedback passed: Gradle release checks `BUILD SUCCESSFUL in 40s` with 109 actionable tasks, signing report `BUILD SUCCESSFUL in 4s`, Play graphic/screenshot dimensions, metadata consistency, security/dependency scan, privacy/Play form consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content checks passed. Release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-21: Hint panel now shows the recommended color plus expected gain. `GameEngine.suggestMove` is unit-tested through existing hint tests; `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 5s. No emulator smoke was run because future Android launches must use only `project_52game_emulator`.
- 2026-05-21: Full release validation after gain-aware hint passed: Gradle release checks `BUILD SUCCESSFUL in 36s` with 109 actionable tasks, signing report `BUILD SUCCESSFUL in 3s`, Play graphic/screenshot dimensions, metadata consistency, security/dependency scan, privacy/Play form consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content checks passed. Release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-21: In-app level screen illustration added through built-in ImageGen. Final app asset is `app/src/main/res/drawable-nodpi/level_illustration.webp`, 720x405 RGB WebP, 40,920 bytes; generated source and processed copy are stored in `qa-artifacts/imagegen`. Level controls were moved into separate labeled buttons and hint/palette/result/cell motion polish was added. `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 6s.
- 2026-05-21: Dedicated emulator policy satisfied for visual smoke: created/used AVD `project_52game_emulator`, resolved serial `emulator-5556`, verified ownership with `adb -s emulator-5556 emu avd name`, installed only `ru.cisgame.colorquarter.debug` on that serial, captured screenshots/UI dumps to `qa-artifacts/level-visual-refresh`, confirmed no app crash entries, then stopped only `emulator-5556`.
- 2026-05-21: Full release validation after level ImageGen/action-bar/motion polish passed with isolated Gradle flags: Gradle release checks `BUILD SUCCESSFUL in 1m 21s` with 109 actionable tasks, signing report `BUILD SUCCESSFUL in 4s`, Play graphic/screenshot dimensions, metadata consistency, security/dependency scan, privacy/Play form consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content checks passed. Release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-21: RC packaging after level ImageGen/action-bar/motion polish passed with `scripts/package_release_candidate.sh 1.0.0`; version guard, docs, QA artifacts, store listing assets, unsigned AAB, manifest and archive integrity checks passed.
- 2026-05-21: Action panel texture added through built-in ImageGen. Final app asset is `app/src/main/res/drawable-nodpi/action_panel_texture.webp`, 720x240 RGB WebP, 5,030 bytes; generated source and processed copy are stored in `qa-artifacts/imagegen`. Separate action buttons now use native vector icons and press-scale feedback. `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 8s.
- 2026-05-21: Dedicated emulator visual smoke after action panel refresh used only `project_52game_emulator`, resolved serial `emulator-5554`, verified ownership with `adb -s emulator-5554 emu avd name`, installed only `ru.cisgame.colorquarter.debug`, cleared only that package, captured screenshots/UI dumps to `qa-artifacts/action-panel-refresh`, confirmed no app-package `FATAL EXCEPTION`, and left other running project emulators untouched.
- 2026-05-21: Full release validation after action panel texture/vector icons/press-state polish passed with isolated Gradle flags: Gradle release checks `BUILD SUCCESSFUL in 49s` with 109 actionable tasks, signing report `BUILD SUCCESSFUL in 3s`, Play graphic/screenshot dimensions, metadata consistency, security/dependency scan, privacy/Play form consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content checks passed. Release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-21: RC packaging after action panel texture/vector icons/press-state polish passed with `scripts/package_release_candidate.sh 1.0.0`; version guard, docs, QA artifacts, store listing assets, unsigned AAB, manifest and archive integrity checks passed.
- 2026-05-21: Final Play screenshot refresh used only `project_52game_emulator`, resolved serial `emulator-5558`, verified ownership with `adb -s emulator-5558 emu avd name`, installed only `ru.cisgame.colorquarter.debug`, cleared only that package, captured onboarding/home/level/victory/settings/about screenshots, converted all six uploadable PNGs to 1080x2400 RGB without alpha, regenerated `contact_sheet.png`, confirmed no app-package crash entries, and stopped only `emulator-5558`.
- 2026-05-21: Legacy hand-made SVG store creatives were moved from `play-assets/graphics` to `archive/rejected-assets/play-graphics`; uploadable Play graphics path now contains only `app_icon_512.png` and `feature_graphic.png`.
- 2026-05-23: Local machine path leakage checks were added to release validation and RC packaging. A hardening validation intentionally caught reappeared obsolete SVG store sources and an old privacy-policy draft with replacement instructions; those files were removed from release surfaces and validation passed.
- 2026-05-23: Final full release validation after path-leakage script cleanup passed: Gradle release checks `BUILD SUCCESSFUL in 41s`, signing report `BUILD SUCCESSFUL in 4s`, AAB local-machine path leakage check, security/dependency scan, Play asset validation, privacy/Play consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content check all passed. Release signing config remains `null` until private `keystore.properties` is provided.
- 2026-05-23: RC packaging passed after final validation; archive integrity and external checksum validation passed, and text leakage check found no local machine configuration in RC handoff surfaces.
- 2026-05-23: RC packaging now performs a dedicated `release_manifest.json` structure/path validation before checksums and zip creation. The validation passed for package `ru.cisgame.colorquarter` `1.0.0 (1)`, unsigned AAB path, 2 uploadable graphics, 6 phone screenshots, required docs and manual Play blockers.
- 2026-05-23: Gradle wrapper hardening added. Official Gradle 8.13 distribution checksum was pinned in `gradle-wrapper.properties`; `GRADLE_EXTRA_ARGS="--no-daemon --no-configuration-cache --console=plain" scripts/validate_release_candidate.sh` passed with Gradle release checks `BUILD SUCCESSFUL in 39s`, signing report `BUILD SUCCESSFUL in 3s`, release automation script validation, signing hygiene check, wrapper validation, local path leakage check, security/dependency scan, Play assets, privacy/Play consistency, manifest privacy, release identity, placeholder scan, localization surface and AAB content checks all passing.
- 2026-05-23: RC packaging manifest integrity was tightened further: critical file records now include validated positive byte sizes and SHA-256 hashes for the AAB, Play graphics, phone screenshots, privacy policy HTML and Play Console submission copy; `checksums.sha256` generation now uses null-delimited paths.
- 2026-05-23: RC packaging after critical-file manifest hardening passed. External archive checksum validation with `shasum -a 256 -c` passed; the current archive checksum is stored in `release-candidate/ColorQuarter-1.0.0-RC-publishing-pack.zip.sha256`.
- 2026-05-23: Play asset alt-text handoff added. `asset_alt_text.md` now covers the app icon, feature graphic and six uploadable phone screenshots; release validation enforces inventory and length.
- 2026-05-23: Performance/size budget validation added for release AAB, debug APK, in-app WebP inventory and heavy media/font resources. Latest validation passed with release AAB `2722642 / 8388608` bytes, debug APK `11965961 / 26214400` bytes and in-app WebP total `150084 / 262144` bytes.
- 2026-05-23: Standalone RC archive verifier added. Validation now checks release automation script syntax/executable bits; packaging includes the verifier in the RC scripts directory.
- 2026-05-23: Signing hygiene validation added for `.gitignore`, optional `keystore.properties` and binary upload keystore placement.
- 2026-05-23: Dependency allowlist validation added for Gradle plugins, repositories and direct dependencies; `docs/dependency_audit.md` created.

## Emulator Smoke
Environment:
- Required future emulator: dedicated AVD `project_52game_emulator`.
- Device serial must be resolved before each run and passed explicitly with `-s <project_52game_device_id>` or `ANDROID_SERIAL=<project_52game_device_id>`.
- Historic QA artifacts may mention `emulator-5554` / Medium Phone API 35. They are retained as past evidence only and must not be treated as permission to reuse a shared or foreign emulator.
- Package: `ru.cisgame.colorquarter.debug`.

Checked:
- Install debug APK.
- First launch onboarding.
- Tap `Играть`.
- Home screen shows progress and locked levels.
- Home screen shows the contextual `Следующая цель` panel with next level, move limit, 3-star target, remaining level count and the primary continue CTA.
- Home screen now includes the ImageGen dashboard illustration above progress. Store home screenshot was refreshed on the dedicated project AVD `project_52game_emulator` on 2026-05-21.
- Start level 1.
- Level header/progress area shows the explicit objective, moves, remaining moves, limit and 3-star target.
- Level screen now includes the compact ImageGen banner, action-panel texture, separate icon+label buttons `Отмена`, `Подсказка`, `Заново`, gain-aware hint panel and motion polish.
- Hint button opens a localized next-color hint with expected gain.
- Complete level 1 with known solution.
- Victory panel visible with `Повторить` and `Следующий`.
- Victory panel now includes the ImageGen completed-quarter illustration. Store victory screenshot was refreshed on the dedicated project AVD `project_52game_emulator` on 2026-05-21 with the result panel visible.
- Defeat panel now includes the ImageGen almost-complete-quarter illustration. Defeat flow remains covered by local UI structure and resources; a separate store screenshot is not included in the six uploadable phone screenshots.
- Force-stop/relaunch keeps progress: `1 / 36`, continue moves to level 2.
- Force-stop/relaunch during an active level restores the current attempt. Confirmed on dedicated AVD `project_52game_emulator` on 2026-05-23 after one consumed move.
- Settings screen opens and shows all toggles.
- `О приложении и приватность` opens from settings, shows version/privacy/permissions/delete-data text.
- Back from privacy/about returns to settings.
- About/privacy version text is sourced from `BuildConfig.VERSION_NAME` and displays production-style `Версия 1.0.0`.
- Crash log buffer empty during smoke.
- Google Play phone screenshots captured at 1080x2400 into `play-assets/screenshots/phone`; level screenshot reflects current ImageGen level banner, action-panel texture, vector action buttons, remaining-move, 3-star target, gain-aware palette and hint UI.
- Home and level Google Play screenshots were refreshed again on 2026-05-24 after next-goal panel, adaptive level grid and explicit level objective polish.
- Final Google Play screenshots are RGB PNG without alpha and were visually inspected through `play-assets/screenshots/phone/contact_sheet.png`.
- Settings screenshot and about/privacy screenshot were refreshed after nav-icon polish and in-app privacy screen.
- Screenshot contact sheet visually inspected.
- Font scale 1.3x checked on onboarding, home, settings and about/privacy.
- Font scale 1.5x checked after fixing onboarding scroll and level card text clipping.
- Star-progress polish checked on emulator: onboarding -> home shows `Прогресс`, `0 / 36`, `Звёзды`, `★ 0 / 108` and `Продолжить: Тихий двор`.
- High contrast markers checked on emulator: setting enabled from UI, level 1 shows numeric board/palette markers and marked palette content descriptions.

## Manual Matrix
| Scenario | Expected |
---|---|
| Первый запуск | Onboarding visible |
| Onboarding complete | Home visible |
| Next goal panel | Home shows `Следующая цель`, next level title, district, move limit, 3-star target, remaining count and a single clear continue CTA |
| Main continue | Opens next unfinished level |
| Color choice | Captured area expands, moves increase |
| Captured area highlight | Connected upper-left controlled cells have a visible border distinct from same-colored disconnected cells |
| Palette gain preview | Useful colors show `+N`; current color and no-gain colors do not show misleading gain |
| No-gain palette color | Button is disabled and cannot consume a move |
| Last-move gain feedback | After a consumed move, progress panel shows `Последний ход: +N клеток`; undo/restart clears it |
| Star pace feedback | Active level shows `Темп: ★★★ ещё доступно`, then drops to `★★`/`★` as move thresholds are exceeded |
| Remaining moves | Start shows `Ходы 0`, `Ост. 8`, `Лимит 8`; after one move shows `Ходы 1`, `Ост. 7`, `Лимит 8` |
| Explicit level objective | Level 1 shows `Цель: собрать 100% за 8 ходов` above counters |
| 3-star target | Level 1 shows `Цель 3★: 6 ходов` before result |
| Level visual banner | Level screen shows the compact ImageGen city-block banner without hiding board, counters or palette |
| Separate action buttons | `Отмена`, `Подсказка`, `Заново` are visible as separate icon+label buttons with clear disabled state for undo before any move |
| Action panel texture | Generated texture is visible behind action controls but does not obscure labels, icons or tap targets |
| Hint | Hint button shows `Подсказка: <цвет>, +N клеток` without consuming a move, or a localized unavailable message when no useful color exists |
| Same color tap | Move not consumed |
| Undo | Previous board and move count restored |
| Reset level before moves | Board and moves reset immediately |
| Reset level after moves | Confirmation dialog appears; cancel keeps attempt; confirm restarts level |
| Back from active level | Confirmation dialog appears; cancel keeps attempt; confirm returns home |
| Win | Result panel, stars, best-result feedback, next button |
| Lose | Failure panel, captured-percent feedback, retry/home |
| All levels complete | Home shows `36 / 36`, total stars, `Город собран`, replay-final CTA and completed level cards |
| Settings toggles | Persist after relaunch |
| High contrast | Board and palette show numeric color markers; palette descriptions include marker and color name |
| About/privacy screen | Opens from settings and returns back |
| Reset progress | Confirmation dialog appears; cancel keeps current screen; confirm clears progress and shows success notice |
| No internet | App works unchanged |
| Background/relaunch | Progress and active attempt persist |
| Immediate force-stop after move/win/reset | Critical progress/active-attempt writes should already be committed |
| Small screen | Content scrolls, no blocked core actions |
| Large screen | Board remains square |

## Known Limitations
- Physical-device QA not completed.
- TalkBack manual pass not completed.
- Play Console pre-launch report not run.
- Future screenshot refreshes must continue to use only dedicated AVD `project_52game_emulator`; current six uploadable Play screenshots reflect the latest ImageGen app-art updates, and home/level screenshots additionally reflect the 2026-05-24 next-goal/objective polish.

## Latest Emulator Check
- 2026-05-15: after adding the in-app about/privacy screen, checked settings -> about/privacy -> back on Medium Phone API 35.
- UI tree confirmed visible Russian texts: `О приложении`, `Версия 1.0.0`, local storage, no collection, permissions and deletion sections.
- Crash buffer did not show app package crashes during this check.
- Store screenshot contact sheet regenerated with 6 screens and visually inspected.
- 2026-05-15: font scale 1.3x screenshots captured to `qa-artifacts/font-scale-1.3`; onboarding, home, settings and about/privacy remained usable.
- 2026-05-15: font scale 1.5x initially exposed clipped onboarding CTA and fixed-height level card labels.
- 2026-05-15: fixed onboarding scrolling and replaced fixed heights with minimum heights for primary CTA/level cards; rechecked font scale 1.5x successfully.
- Font scale 1.5x artifacts: `qa-artifacts/font-scale-1.5/01_onboarding.png`, `01b_onboarding_scrolled.png`, `02_home.png`, `03_settings.png`, `04_about_privacy.png`, `contact_sheet.png`.
- 2026-05-15: checked settings reset confirmation on Medium Phone API 35. `Сбросить прогресс` opens a confirmation dialog, `Отмена` dismisses without success notice, confirm shows `Прогресс сброшен`.
- Reset confirmation artifacts: `qa-artifacts/reset-confirmation/01_reset_dialog.png`, `02_reset_confirmed.png` and matching UI dumps/summaries.
- 2026-05-15: checked all-levels-complete home state by seeding debug SharedPreferences with completed results for all 36 levels. UI tree confirmed `36 / 36`, `Город собран`, complete-state body and `Повторить финал: Весь квартал`.
- Completion-state artifacts: `qa-artifacts/completion-state/01_home_complete.png`, UI dump and summary.
- 2026-05-15: checked star-progress home state on emulator after direct `adb install` of debug APK. UI dump confirmed `Прогресс`, `0 / 36`, `Звёзды`, `★ 0 / 108`, `Продолжить: Тихий двор`; crash buffer was empty.
- Star-progress artifacts: `qa-artifacts/star-progress/01_launch.png`, `01_launch.xml`, `02_home.png`, `02_home.xml`, `02_home_summary.txt`, `crash.log`.
- 2026-05-15: after adding level-card accessibility descriptions and star-count plurals, `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 7s.
- 2026-05-15: after adding result best-feedback and stricter `LevelProgress.improvesBest`, `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 9s.
- 2026-05-15: checked first-win result feedback on emulator from clean app data. Level 1 completed with the stored 6-move solution; UI dump confirmed `Квартал собран`, `★★★`, `Первое прохождение: 6 ходов`, `Повторить` and `Следующий`; crash buffer was empty.
- Result-feedback artifacts: `qa-artifacts/result-feedback/01_onboarding.png`, `02_home.png`, `03_level.png`, `04_result.png`, matching UI dumps, `04_result_summary.txt`, `crash.log`.
- 2026-05-18: Google Play phone screenshots refreshed from current result-feedback captures for onboarding, home and victory; level screenshot later refreshed from the current level UI with remaining moves and 3-star target; `contact_sheet.png` regenerated with 6 screens.
- 2026-05-18: checked defeat feedback on emulator from clean app data. Level 1 was intentionally lost with 8 moves; UI dump confirmed `Ходы закончились`, `Собрано: 12% из 100%`, `Повторить` and `На главную`; crash buffer was empty.
- Defeat-feedback artifacts: `qa-artifacts/defeat-feedback/01_onboarding.xml`, `02_home.xml`, `03_level.xml`, `04_defeat.xml`, `04_defeat_summary.txt`, `crash.log`. Headless screencap PNGs for this run were removed because they were black; UI dump and summary remain the source of evidence.
- 2026-05-18: checked active-attempt confirmation on Medium Phone API 35. After one move, Android back showed `Выйти из уровня?`; `Отмена` kept the attempt; restart showed `Начать уровень заново?`; crash buffer was empty.
- Active-attempt artifacts: `qa-artifacts/active-attempt-confirmation/01_onboarding.xml`, `02_home.xml`, `03_level_start.xml`, `04_after_move.xml`, `05_leave_dialog.xml`, `05_leave_dialog_summary.txt`, `06_after_leave_cancel.xml`, `07_restart_dialog.xml`, `07_restart_dialog_summary.txt`, `crash.log`.
- 2026-05-18: checked high contrast markers on Medium Phone API 35. Enabled `Повышенный контраст` via settings, opened level 1, UI dump confirmed board labels `1-4`, palette labels `✓ 1`, `2`, `3`, `4` and descriptions `Выбрать цвет 1: Лагуна`, `Выбрать цвет 2: Солнце`, `Выбрать цвет 3: Ягода`, `Выбрать цвет 4: Мята`; crash buffer was empty.
- High-contrast marker artifacts: `qa-artifacts/high-contrast-markers/01_onboarding.xml`, `02_home.xml`, `03_settings.xml`, `04_settings_high_contrast.xml`, `05_home_high_contrast.xml`, `06_level_high_contrast.xml`, `06_level_high_contrast_summary.txt`, `crash.log`.
- 2026-05-18: checked remaining-moves counters on Medium Phone API 35. UI dump confirmed `Ходы 0`, `Ост. 8`, `Лимит 8` at level start and `Ходы 1`, `Ост. 7`, `Лимит 8` after one move; both dumps belonged to package `ru.cisgame.colorquarter.debug`; crash buffer was empty.
- Remaining-moves artifacts: `qa-artifacts/remaining-moves/01_onboarding.xml`, `02_home.xml`, `03_level_start.xml`, `03_level_start_summary.txt`, `04_after_one_move.xml`, `04_after_one_move_summary.txt`, `crash.log`.
- 2026-05-18: checked 3-star target on Medium Phone API 35. Level 1 UI dump confirmed package `ru.cisgame.colorquarter.debug`, `Ходы 0`, `Ост. 8`, `Лимит 8`, `Цель 3★: 6 ходов`, `Палитра цветов` and visible palette buttons; crash buffer was empty.
- 3-star target artifacts: `qa-artifacts/three-star-target/01_onboarding.xml`, `02_home.xml`, `03_level_target.xml`, `03_level_target_summary.txt`, `crash.log`.
- 2026-05-18: refreshed the Google Play level screenshot after remaining-move and 3-star target polish. Final 1080x2400 capture confirmed package `ru.cisgame.colorquarter.debug`, `Ходы 0`, `Ост. 8`, `Лимит 8`, `Цель 3★: 6 ходов`, `Палитра цветов` and an empty crash buffer.
- Store screenshot refresh artifacts: `qa-artifacts/store-screenshot-refresh/08_onboarding_final.xml`, `09_home_final.xml`, `10_level_final.png`, `10_level_final.xml`, `10_level_final_summary.txt`, `crash.log`.
- 2026-05-18: added local active-attempt persistence. Unit tests cover valid/corrupted saved attempts. Early AVD attempts did not produce final evidence and were superseded by the dedicated 2026-05-23 smoke pass.
- 2026-05-23: active-attempt force-stop/relaunch smoke passed on dedicated AVD `project_52game_emulator`, resolved as `emulator-5560`. A foreign `project_53game_emulator` was detected on another serial and not used. Flow: clear only `ru.cisgame.colorquarter.debug`, onboarding -> home -> level 1, choose `Солнце`, verify `Ходы 1`, force-stop app package, relaunch, verify app opens the active level with `Ходы 1`, `Ост. 7`, `Текущий цвет Солнце`, available undo and no app crash entries. Artifacts: `qa-artifacts/active-attempt-restore-2026-05-23`.
- 2026-05-24: design-review visual smoke passed on dedicated AVD `project_52game_emulator`, resolved as `emulator-5556`; a foreign `project_53game_emulator` was detected on another serial and not used. Flow: install debug APK, clear only `ru.cisgame.colorquarter.debug`, onboarding -> home -> level 1. UI dump confirmed `Следующая цель`, `Уровень 1: Тихий двор`, `Осталось: 36`, `Продолжить: Тихий двор`, `Цель: собрать 100% за 8 ходов`, `Палитра цветов`, `Отмена`, `Подсказка`, `Заново`; app crash log was empty. Artifacts: `qa-artifacts/design-review-2026-05-24`.
- 2026-05-27: RTF recertification smoke passed on dedicated AVD `project_52game_emulator`, resolved as `emulator-5556`; a foreign `project_betano_emulator` was detected as `emulator-5554` and not used. Flow: install debug APK, clear only `ru.cisgame.colorquarter.debug`, onboarding -> home -> level 1, open hint, complete level 1 with known 6-move solution, verify victory result and tap `Следующий`. UI dumps confirmed `Следующая цель`, `Уровень 1: Тихий двор`, `Цель: собрать 100% за 8 ходов`, `Подсказка: Солнце, +2 клетки`, `Квартал собран`, `★★★`, `Первое прохождение: 6 ходов`, `Следующий`; app crash log was empty. Artifacts: `qa-artifacts/recertification-2026-05-27`.
- 2026-05-18: tightened active-attempt sanitization so restored attempts require complete undo history matching the move count; `./gradlew :app:test --console=plain` passed in 4s.
- 2026-05-19: active-attempt sanitization now also rejects already-solved saved boards; `./gradlew :app:test --console=plain` passed in 8s.
- 2026-05-19: active-attempt sanitization now rejects stale undo histories whose first snapshot is not the current catalog level start; `./gradlew :app:test --console=plain` passed in 1s.
- 2026-05-19: malformed row snapshots with empty segments are rejected instead of normalized; `./gradlew :app:test --console=plain` passed in 1s.
- 2026-05-19: active attempts with positive moves but unchanged current board are rejected; the valid-attempt unit fixture now uses a real `GameEngine.applyColor` move; `./gradlew :app:test --console=plain` passed in 1s.
- 2026-05-19: added pure progress codec coverage for duplicate local results and stable encoded ordering; `./gradlew :app:test --console=plain` passed in 1s.
- 2026-05-19: hint logic now returns no hint when no palette color expands the captured area; `./gradlew :app:test --console=plain` passed in 1s.
- 2026-05-19: active-attempt sanitization now rejects solved undo snapshots and adjacent duplicate states in the `history + current rows` chain; `./gradlew :app:test --console=plain` passed in 1s.
- 2026-05-19: hint UI now distinguishes not-requested from requested-but-unavailable hints, so the `hint_unavailable` string is reachable in-app.
- 2026-05-19: `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 10s after unavailable-hint UI polish.
- 2026-05-19: release automation now removes numbered generated `app/build/* 2` directories before validation/packaging; standard `app/build/outputs/bundle/release/app-release.aab` and `app/build/outputs/apk/debug/app-debug.apk` were regenerated successfully.
- 2026-05-19: progress sanitization now rejects corrupted best results above the level move limit; `./gradlew :app:test --console=plain` passed in 2s.
- 2026-05-19: active-attempt restore now clears saved attempts for levels that are no longer unlocked by sanitized progress; `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 7s.
- 2026-05-19: active-attempt sanitization now rejects saved histories whose adjacent states cannot be reproduced by a consumed palette move; `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 6s.
- 2026-05-19: `ProgressStore` now loads and saves active attempts through the same progress-aware sanitizer, so storage-level writes also reject locked-level attempts; `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 4s.
- 2026-05-19: board snapshot encoding for undo/history/active-attempt persistence is centralized in `BoardSnapshotCodec`; `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 4s.
- 2026-05-19: game result UI state now uses `LevelRunResult` instead of string literals for win/loss result flow; `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 4s.
- 2026-05-19: app navigation state now uses `AppScreen` instead of string route constants for onboarding/home/game/settings/about flow; `./gradlew :app:assembleDebug :app:lint :app:test --console=plain` passed in 4s.
- 2026-05-19: release validation/package scripts now quarantine numbered top-level `app/build/* N` directories outside the Android module when canonical paths exist, avoiding local APFS/Gradle cleanup stalls while keeping canonical build outputs checkable.
- 2026-05-19: startup cleanup for invalid active-attempt payloads now checks for active-attempt keys before clearing, avoiding a clean-launch SharedPreferences write when no saved attempt exists.
- 2026-05-19: release validation/package scripts now quarantine numbered build and old RC directories outside the Gradle project root; `scripts/validate_release_candidate.sh --normalize-only` exits after cleanup instead of running Gradle.
- 2026-05-19: removed the obsolete `privacy_policy_draft.md` from `play-assets` after placeholder validation correctly flagged release-surface draft copy.
- 2026-05-19: RC packaging now keeps `contact_sheet.png` under QA evidence instead of uploadable store screenshots; package validation checks exactly 6 phone screenshot PNGs in `store-listing/screenshots/phone`.
- 2026-05-19: full release validation passed after those changes with Gradle checks in 38s and signing report in 5s.
- 2026-05-18: added hint logic. `GameEngineTest` covers best immediate expansion and solved-board no-hint behavior; UI strings are localized in `strings.xml`.
- 2026-05-18: refreshed Google Play level screenshot after hint-button polish on Medium Phone API 36. Final 1080x2400 capture confirmed `Тихий двор`, `Ходы 0`, `Ост. 8`, `Лимит 8`, `Подсказка: попробуйте Солнце`, `Палитра цветов` and an empty crash buffer.
- Hint-polish artifacts: `qa-artifacts/hint-polish/01_level_before_hint.xml`, `02_level_hint.png`, `02_level_hint.xml`, `03_level_hint_2400.png`, `03_level_hint_2400.xml`, `03_level_hint_2400_summary.txt`, `crash.log`.
- 2026-05-19: final release automation passed with isolated Gradle flags after stopping stale emulator/ADB processes from the previous UI session. A first retry hit a local `clean` deletion race after Gradle had already removed `app/build`; rerunning from the clean directory passed.
