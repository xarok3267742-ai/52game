#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

VERSION="${1:-1.0.0}"
RC_ROOT="release-candidate"
RC_DIR="$RC_ROOT/$VERSION"
ZIP_NAME="ColorQuarter-$VERSION-RC-publishing-pack.zip"

if [[ -z "${JAVA_HOME:-}" && -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
  export PATH="$JAVA_HOME/bin:${PATH}"
fi

require_file() {
  local path="$1"
  [[ -f "$path" ]] || {
    echo "Missing required file: $path" >&2
    exit 1
  }
}

normalize_numbered_build_dirs() {
  [[ -d app/build ]] || return 0
  local quarantine_root="$ROOT_DIR/../.colorquarter-build-numbered-quarantine/app"
  mkdir -p "$quarantine_root"
  while IFS= read -r -d '' duplicate_path; do
    local canonical_path="${duplicate_path% [0-9]*}"
    if [[ -d "$canonical_path" ]]; then
      local quarantine_path="$quarantine_root/$(basename "$duplicate_path")-$(date +%s)-$RANDOM"
      echo "$duplicate_path -> $quarantine_path"
      mv "$duplicate_path" "$quarantine_path"
    else
      echo "$duplicate_path -> $canonical_path"
      mv "$duplicate_path" "$canonical_path"
    fi
  done < <(find app/build -maxdepth 1 -type d -name "* [0-9]*" -print0 2>/dev/null)
}

normalize_numbered_build_dirs
require_file "app/build/outputs/bundle/release/app-release.aab"
require_file "play-assets/graphics/app_icon_512.png"
require_file "play-assets/graphics/feature_graphic.png"
require_file "play-assets/legal/privacy_policy_ru.html"
require_file "play-assets/metadata/ru-RU/play_console_submission.md"
require_file "play-assets/metadata/ru-RU/asset_alt_text.md"
require_file "scripts/verify_release_candidate_package.sh"

echo "== Release version guard =="
python3 - "$VERSION" <<'PY'
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

requested_version = sys.argv[1]
build_config_path = Path("app/build/generated/source/buildConfig/release/ru/cisgame/colorquarter/BuildConfig.java")
manifest_path = Path("app/build/intermediates/merged_manifests/release/processReleaseManifest/AndroidManifest.xml")

for path in (build_config_path, manifest_path):
    if not path.exists():
        raise SystemExit(f"Missing release metadata input: {path}. Run bundleRelease or validate_release_candidate.sh first.")

build_config = build_config_path.read_text(encoding="utf-8")

def java_value(name):
    match = re.search(rf"public static final .* {name} = (.+);", build_config)
    if not match:
        raise SystemExit(f"BuildConfig missing {name}")
    value = match.group(1).strip()
    if value.startswith('"') and value.endswith('"'):
        return value[1:-1]
    return value

build_type = java_value("BUILD_TYPE")
version_name = java_value("VERSION_NAME")
version_code = java_value("VERSION_CODE")
application_id = java_value("APPLICATION_ID")

android = "{http://schemas.android.com/apk/res/android}"
manifest_root = ET.parse(manifest_path).getroot()
manifest_version_name = manifest_root.attrib.get(android + "versionName")
manifest_version_code = manifest_root.attrib.get(android + "versionCode")
manifest_package = manifest_root.attrib.get("package")

if build_type != "release":
    raise SystemExit(f"Expected release BuildConfig, got BUILD_TYPE={build_type}")
if requested_version != version_name:
    raise SystemExit(f"RC package version {requested_version} does not match release VERSION_NAME {version_name}")
if manifest_version_name != version_name:
    raise SystemExit(f"Merged manifest versionName {manifest_version_name} does not match BuildConfig VERSION_NAME {version_name}")
if manifest_version_code != version_code:
    raise SystemExit(f"Merged manifest versionCode {manifest_version_code} does not match BuildConfig VERSION_CODE {version_code}")
if manifest_package != application_id:
    raise SystemExit(f"Merged manifest package {manifest_package} does not match BuildConfig APPLICATION_ID {application_id}")
if "debug" in f"{application_id} {version_name} {build_type}".lower():
    raise SystemExit("Release metadata contains debug marker")

print(f"Packaging release {application_id} {version_name} ({version_code})")
PY

if [[ -d "$RC_DIR" ]]; then
  rc_quarantine_root="$ROOT_DIR/../.colorquarter-release-candidate-quarantine"
  mkdir -p "$rc_quarantine_root"
  mv "$RC_DIR" "$rc_quarantine_root/$VERSION-$(date +%s)-$RANDOM"
fi
mkdir -p \
  "$RC_DIR/docs" \
  "$RC_DIR/legal" \
  "$RC_DIR/qa-artifacts" \
  "$RC_DIR/scripts" \
  "$RC_DIR/store-listing" \
  "$RC_DIR/unsigned-aab" \
  "$RC_DIR/signed-aab"

is_signed="unknown"
if command -v jarsigner >/dev/null 2>&1; then
  if jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab 2>&1 | grep -q "jar is unsigned"; then
    is_signed="false"
  else
    is_signed="true"
  fi
fi

if [[ "$is_signed" == "true" ]]; then
  cp app/build/outputs/bundle/release/app-release.aab "$RC_DIR/signed-aab/app-release.aab"
else
  cp app/build/outputs/bundle/release/app-release.aab "$RC_DIR/unsigned-aab/app-release-unsigned.aab"
fi

cp -R play-assets/graphics "$RC_DIR/store-listing/graphics"
cp -R play-assets/screenshots "$RC_DIR/store-listing/screenshots"
cp -R play-assets/metadata "$RC_DIR/store-listing/metadata"
cp -R play-assets/legal/. "$RC_DIR/legal/"

if find "$RC_DIR/store-listing/graphics" -maxdepth 1 -type f -name "*.svg" | grep -q .; then
  mkdir -p "$RC_DIR/qa-artifacts/store-listing-source/graphics"
  while IFS= read -r -d '' source_graphic; do
    mv "$source_graphic" \
      "$RC_DIR/qa-artifacts/store-listing-source/graphics/$(basename "$source_graphic")"
  done < <(find "$RC_DIR/store-listing/graphics" -maxdepth 1 -type f -name "*.svg" -print0)
fi

require_file "$RC_DIR/store-listing/graphics/app_icon_512.png"
require_file "$RC_DIR/store-listing/graphics/feature_graphic.png"

store_graphic_count="$(
  find "$RC_DIR/store-listing/graphics" -maxdepth 1 -type f -name "*.png" \
    | wc -l \
    | tr -d " "
)"
if [[ "$store_graphic_count" != "2" ]]; then
  echo "Expected exactly 2 uploadable store graphics in RC store listing, got $store_graphic_count" >&2
  exit 1
fi

if find "$RC_DIR/store-listing/graphics" -maxdepth 1 -type f ! -name "*.png" | grep -q .; then
  echo "Only uploadable PNG graphics may remain in RC store-listing/graphics" >&2
  exit 1
fi

if [[ -f "$RC_DIR/store-listing/screenshots/phone/contact_sheet.png" ]]; then
  mkdir -p "$RC_DIR/qa-artifacts/store-listing-preview"
  mv "$RC_DIR/store-listing/screenshots/phone/contact_sheet.png" \
    "$RC_DIR/qa-artifacts/store-listing-preview/contact_sheet.png"
fi

store_screenshot_count="$(
  find "$RC_DIR/store-listing/screenshots/phone" -maxdepth 1 -type f -name "*.png" \
    | wc -l \
    | tr -d " "
)"
if [[ "$store_screenshot_count" != "6" ]]; then
  echo "Expected exactly 6 uploadable phone screenshots in RC store listing, got $store_screenshot_count" >&2
  exit 1
fi

if [[ -e "$RC_DIR/store-listing/screenshots/phone/contact_sheet.png" ]]; then
  echo "contact_sheet.png must stay in QA evidence, not in uploadable store-listing screenshots" >&2
  exit 1
fi

if [[ -d "qa-artifacts" ]]; then
  cp -R qa-artifacts/. "$RC_DIR/qa-artifacts/"
fi
if [[ -d "archive/rejected-assets" ]]; then
  mkdir -p "$RC_DIR/qa-artifacts/rejected-assets"
  cp -R archive/rejected-assets/. "$RC_DIR/qa-artifacts/rejected-assets/"
fi

cp README.md "$RC_DIR/docs/README.md"
python3 - "$ROOT_DIR" "$RC_DIR/docs/AGENTS.md" <<'PY'
from pathlib import Path
import sys

root_dir = Path(sys.argv[1]).as_posix()
output_path = Path(sys.argv[2])
text = Path("AGENTS.md").read_text(encoding="utf-8")
text = text.replace(root_dir, "<project-root>")
output_path.write_text(text, encoding="utf-8")
PY
cp -R docs/. "$RC_DIR/docs/"
cp scripts/validate_release_candidate.sh "$RC_DIR/scripts/validate_release_candidate.sh"
cp scripts/package_release_candidate.sh "$RC_DIR/scripts/package_release_candidate.sh"
cp scripts/verify_release_candidate_package.sh "$RC_DIR/scripts/verify_release_candidate_package.sh"

cat > "$RC_DIR/README.md" <<EOF
# Цветной Квартал $VERSION Release Candidate

Дата сборки пакета: $(date '+%Y-%m-%d %H:%M:%S %Z')

Этот каталог содержит материалы для ручной публикации в Google Play Console.

## Статус AAB

$(if [[ "$is_signed" == "true" ]]; then echo '`signed-aab/app-release.aab` — release bundle с подписью.'; else echo '`unsigned-aab/app-release-unsigned.aab` — release bundle без production upload key.'; fi)

Если AAB unsigned, перед загрузкой в Google Play нужно создать upload keystore, заполнить приватный \`keystore.properties\` и пересобрать signed AAB по инструкции:

\`\`\`text
docs/signing_guide.md
\`\`\`

## Что загружать в Play Console

После production signing:
- signed \`app-release.aab\`;
- \`store-listing/graphics/app_icon_512.png\`;
- \`store-listing/graphics/feature_graphic.png\`;
- \`store-listing/screenshots/phone/01_onboarding.png\`;
- \`store-listing/screenshots/phone/02_home.png\`;
- \`store-listing/screenshots/phone/03_level.png\`;
- \`store-listing/screenshots/phone/04_victory.png\`;
- \`store-listing/screenshots/phone/05_settings.png\`;
- \`store-listing/screenshots/phone/06_about_privacy.png\`.

## Что копировать в формы

\`\`\`text
store-listing/metadata/ru-RU/play_console_submission.md
\`\`\`

Дополнительные формы:
- \`store-listing/metadata/ru-RU/data_safety.md\`;
- \`store-listing/metadata/ru-RU/content_rating_notes.md\`;
- \`store-listing/metadata/ru-RU/target_audience_notes.md\`;
- \`store-listing/metadata/ru-RU/app_access_notes.md\`.
- \`store-listing/metadata/ru-RU/asset_alt_text.md\`.

Play readiness notes:
- \`docs/play_requirements_audit.md\`;
- \`docs/google_play_checklist.md\`;
- \`docs/play_console_runbook.md\`;
- \`docs/security_audit.md\`.
- \`docs/build_environment.md\`.
- \`docs/third_party_notices.md\`.
- \`docs/AGENTS.md\` — engineering/runbook правила проекта.

## Privacy Policy

Разместить на публичном HTTPS URL:

\`\`\`text
legal/privacy_policy_ru.html
\`\`\`

После публикации URL вставить в Play Console.

## Проверка целостности

\`\`\`bash
cd release-candidate/$VERSION
shasum -a 256 -c checksums.sha256
\`\`\`

## Машинный манифест

\`\`\`text
release_manifest.json
\`\`\`

В манифесте зафиксированы applicationId, versionName/versionCode, статус подписи AAB, uploadable Play assets, QA/source assets, ручные блокеры перед публикацией, а также размер и SHA-256 для критичных upload/handoff файлов.

## Automation

Скрипты, которыми собран и проверен пакет:
- \`scripts/validate_release_candidate.sh\`;
- \`scripts/package_release_candidate.sh\`;
- \`scripts/verify_release_candidate_package.sh\`.

## QA Evidence

Внутренние QA-артефакты:
- \`qa-artifacts/font-scale-1.3/\`;
- \`qa-artifacts/font-scale-1.5/\`.
- \`qa-artifacts/store-listing-preview/contact_sheet.png\` — preview листинга, не Play upload asset.
- \`qa-artifacts/store-listing-source/graphics/\` — optional archived source graphics if present, not Play upload assets.
- \`qa-artifacts/rejected-assets/\` — rejected/obsolete graphics, not Play upload assets.

## Не загружать

- \`unsigned-aab/app-release-unsigned.aab\` без production signing.
- QA preview/contact-sheet файлы из \`qa-artifacts\`.
- Any source/archive graphics from \`qa-artifacts/store-listing-source\` or \`archive/rejected-assets\`.
EOF

python3 - "$VERSION" "$is_signed" <<'PY'
from datetime import datetime, timezone
from pathlib import Path
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET

version = sys.argv[1]
is_signed = sys.argv[2]
rc_dir = Path("release-candidate") / version
build_config_path = Path("app/build/generated/source/buildConfig/release/ru/cisgame/colorquarter/BuildConfig.java")
manifest_path = Path("app/build/intermediates/merged_manifests/release/processReleaseManifest/AndroidManifest.xml")

build_config = build_config_path.read_text(encoding="utf-8")

def java_value(name):
    match = re.search(rf"public static final .* {name} = (.+);", build_config)
    if not match:
        raise SystemExit(f"BuildConfig missing {name}")
    value = match.group(1).strip()
    if value.startswith('"') and value.endswith('"'):
        return value[1:-1]
    return int(value)

android = "{http://schemas.android.com/apk/res/android}"
manifest_root = ET.parse(manifest_path).getroot()

def rel(path):
    return str(path.relative_to(rc_dir))

def sorted_files(relative_dir, pattern="*"):
    base = rc_dir / relative_dir
    if not base.exists():
        return []
    return sorted(rel(path) for path in base.glob(pattern) if path.is_file())

def require_manifest_file(relative_path):
    path = rc_dir / relative_path
    if not path.is_file():
        raise SystemExit(f"Release manifest missing required file: {relative_path}")
    return relative_path

def file_record(relative_path):
    path = rc_dir / relative_path
    if not path.is_file():
        raise SystemExit(f"Release manifest checksum input missing: {relative_path}")
    return {
        "path": relative_path,
        "bytes": path.stat().st_size,
        "sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
    }

aab_path = "signed-aab/app-release.aab" if is_signed == "true" else "unsigned-aab/app-release-unsigned.aab"
store_graphics = sorted_files("store-listing/graphics", "*.png")
phone_screenshots = sorted_files("store-listing/screenshots/phone", "*.png")
source_graphics = sorted_files("qa-artifacts/store-listing-source/graphics", "*.svg")
rejected_graphics = sorted_files("qa-artifacts/rejected-assets/play-graphics", "*.svg")
play_submission_path = "store-listing/metadata/ru-RU/play_console_submission.md"
asset_alt_text_path = "store-listing/metadata/ru-RU/asset_alt_text.md"
privacy_policy_html_path = "legal/privacy_policy_ru.html"

expected_graphics = [
    "store-listing/graphics/app_icon_512.png",
    "store-listing/graphics/feature_graphic.png",
]
for path in expected_graphics + [aab_path]:
    require_manifest_file(path)

if store_graphics != expected_graphics:
    raise SystemExit(f"Unexpected uploadable graphics in RC manifest: {store_graphics}")
if len(phone_screenshots) != 6:
    raise SystemExit(f"Expected 6 phone screenshots in RC manifest, got {len(phone_screenshots)}")

manifest = {
    "schema_version": 1,
    "generated_at_utc": datetime.now(timezone.utc).replace(microsecond=0).isoformat(),
    "product": {
        "name": "Цветной Квартал",
        "type": "offline Android puzzle",
        "application_id": java_value("APPLICATION_ID"),
        "version_name": java_value("VERSION_NAME"),
        "version_code": java_value("VERSION_CODE"),
        "build_type": java_value("BUILD_TYPE"),
        "manifest_package": manifest_root.attrib.get("package"),
        "manifest_version_name": manifest_root.attrib.get(android + "versionName"),
        "manifest_version_code": manifest_root.attrib.get(android + "versionCode"),
        "min_sdk": 23,
        "target_sdk": 35,
        "compile_sdk": 36,
    },
    "aab": {
        "status": "signed" if is_signed == "true" else "unsigned",
        "path": aab_path,
        "upload_ready": is_signed == "true",
        "note": "Upload to Google Play only after production upload-key signing." if is_signed != "true" else "Ready for Play upload after final manual review.",
    },
    "store_listing": {
        "metadata": sorted_files("store-listing/metadata/ru-RU", "*.md"),
        "graphics": store_graphics,
        "phone_screenshots": phone_screenshots,
        "privacy_policy_html": require_manifest_file(privacy_policy_html_path),
        "privacy_policy_markdown": require_manifest_file("legal/privacy_policy_ru.md"),
    },
    "qa_and_source_assets": {
        "source_graphics": source_graphics,
        "rejected_graphics": rejected_graphics,
        "contact_sheet": "qa-artifacts/store-listing-preview/contact_sheet.png"
            if (rc_dir / "qa-artifacts/store-listing-preview/contact_sheet.png").is_file()
            else None,
        "font_scale_1_3": "qa-artifacts/font-scale-1.3",
        "font_scale_1_5": "qa-artifacts/font-scale-1.5",
    },
    "documentation": {
        "readme": require_manifest_file("README.md"),
        "agents": require_manifest_file("docs/AGENTS.md"),
        "release_report": require_manifest_file("docs/release_report.md"),
        "build_environment": require_manifest_file("docs/build_environment.md"),
        "google_play_checklist": require_manifest_file("docs/google_play_checklist.md"),
        "play_console_runbook": require_manifest_file("docs/play_console_runbook.md"),
        "signing_guide": require_manifest_file("docs/signing_guide.md"),
        "security_audit": require_manifest_file("docs/security_audit.md"),
        "third_party_notices": require_manifest_file("docs/third_party_notices.md"),
    },
    "manual_google_play_blockers": [
        "Create or connect Google Play Console app.",
        "Create production upload keystore outside the repository.",
        "Fill private keystore.properties and rebuild signed AAB.",
        "Host legal/privacy_policy_ru.html on a public HTTPS URL.",
        "Upload store listing graphics/screenshots and fill Data Safety, Content Rating, Target Audience and App Access forms.",
        "Run internal testing and review the Play pre-launch report.",
    ],
    "integrity": {
        "checksums": "checksums.sha256",
        "archive_checksum": f"ColorQuarter-{version}-RC-publishing-pack.zip.sha256",
        "critical_files": [
            file_record(relative_path)
            for relative_path in (
                [aab_path]
                + expected_graphics
                + phone_screenshots
                + [
                    privacy_policy_html_path,
                    play_submission_path,
                    asset_alt_text_path,
                ]
            )
        ],
    },
}

(rc_dir / "release_manifest.json").write_text(
    json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
    encoding="utf-8",
)
PY

echo "== Release manifest validation =="
python3 - "$RC_DIR" "$VERSION" "$is_signed" <<'PY'
import hashlib
import json
import re
import sys
from pathlib import Path

rc_dir = Path(sys.argv[1])
expected_version = sys.argv[2]
is_signed = sys.argv[3]
manifest_path = rc_dir / "release_manifest.json"

manifest = json.loads(manifest_path.read_text(encoding="utf-8"))

def fail(message):
    raise SystemExit(f"{manifest_path}: {message}")

def require(condition, message):
    if not condition:
        fail(message)

def require_path(relative_path, *, file=True):
    require(isinstance(relative_path, str) and relative_path, "path must be a non-empty string")
    path = Path(relative_path)
    require(not path.is_absolute(), f"path must be relative: {relative_path}")
    require(".." not in path.parts, f"path must not escape RC dir: {relative_path}")
    full_path = rc_dir / path
    if file:
        require(full_path.is_file(), f"missing required file: {relative_path}")
    else:
        require(full_path.is_dir(), f"missing required directory: {relative_path}")
    return full_path

required_top_level = {
    "schema_version",
    "generated_at_utc",
    "product",
    "aab",
    "store_listing",
    "qa_and_source_assets",
    "documentation",
    "manual_google_play_blockers",
    "integrity",
}
require(set(manifest) == required_top_level, f"unexpected top-level keys: {sorted(set(manifest) ^ required_top_level)}")
require(manifest["schema_version"] == 1, "schema_version must be 1")

product = manifest["product"]
require(product["name"] == "Цветной Квартал", "unexpected product name")
require(product["application_id"] == "ru.cisgame.colorquarter", "unexpected application_id")
require(product["manifest_package"] == product["application_id"], "manifest package mismatch")
require(product["version_name"] == expected_version, "version_name mismatch")
require(product["manifest_version_name"] == expected_version, "manifest_version_name mismatch")
require(str(product["manifest_version_code"]) == str(product["version_code"]), "version_code mismatch")
require(product["build_type"] == "release", "build_type must be release")
require(product["min_sdk"] == 23, "min_sdk mismatch")
require(product["target_sdk"] == 35, "target_sdk mismatch")
require(product["compile_sdk"] == 36, "compile_sdk mismatch")

aab = manifest["aab"]
expected_status = "signed" if is_signed == "true" else "unsigned"
expected_aab_path = "signed-aab/app-release.aab" if is_signed == "true" else "unsigned-aab/app-release-unsigned.aab"
require(aab["status"] == expected_status, "AAB signing status mismatch")
require(aab["path"] == expected_aab_path, "AAB path mismatch")
require(aab["upload_ready"] is (is_signed == "true"), "AAB upload_ready mismatch")
require_path(aab["path"])

store_listing = manifest["store_listing"]
expected_graphics = [
    "store-listing/graphics/app_icon_512.png",
    "store-listing/graphics/feature_graphic.png",
]
expected_screenshots = [
    "store-listing/screenshots/phone/01_onboarding.png",
    "store-listing/screenshots/phone/02_home.png",
    "store-listing/screenshots/phone/03_level.png",
    "store-listing/screenshots/phone/04_victory.png",
    "store-listing/screenshots/phone/05_settings.png",
    "store-listing/screenshots/phone/06_about_privacy.png",
]
require(store_listing["graphics"] == expected_graphics, "unexpected uploadable graphics list")
require(store_listing["phone_screenshots"] == expected_screenshots, "unexpected phone screenshot list")
for relative_path in expected_graphics + expected_screenshots:
    require_path(relative_path)
require_path(store_listing["privacy_policy_html"])
require_path(store_listing["privacy_policy_markdown"])
require("store-listing/screenshots/phone/contact_sheet.png" not in store_listing["phone_screenshots"], "contact sheet must not be uploadable")

metadata = store_listing["metadata"]
required_metadata = {
    "store-listing/metadata/ru-RU/app_access_notes.md",
    "store-listing/metadata/ru-RU/asset_alt_text.md",
    "store-listing/metadata/ru-RU/content_rating_notes.md",
    "store-listing/metadata/ru-RU/data_safety.md",
    "store-listing/metadata/ru-RU/play_console_submission.md",
    "store-listing/metadata/ru-RU/privacy_policy.md",
    "store-listing/metadata/ru-RU/target_audience_notes.md",
}
require(required_metadata.issubset(set(metadata)), "metadata list misses required Play form notes")
for relative_path in metadata:
    require_path(relative_path)

qa_assets = manifest["qa_and_source_assets"]
for key in ("font_scale_1_3", "font_scale_1_5"):
    require_path(qa_assets[key], file=False)
if qa_assets.get("contact_sheet") is not None:
    require_path(qa_assets["contact_sheet"])
for relative_path in qa_assets.get("source_graphics", []) + qa_assets.get("rejected_graphics", []):
    require_path(relative_path)

documentation = manifest["documentation"]
required_doc_keys = {
    "readme",
    "agents",
    "release_report",
    "build_environment",
    "google_play_checklist",
    "play_console_runbook",
    "signing_guide",
    "security_audit",
    "third_party_notices",
}
require(set(documentation) == required_doc_keys, "documentation keys mismatch")
for relative_path in documentation.values():
    require_path(relative_path)

blockers = manifest["manual_google_play_blockers"]
require(isinstance(blockers, list) and len(blockers) >= 5, "manual blockers must be explicit")
for required_fragment in (
    "Google Play Console app",
    "production upload keystore",
    "keystore.properties",
    "public HTTPS URL",
    "pre-launch report",
):
    require(
        any(required_fragment in blocker for blocker in blockers),
        f"manual blockers missing fragment: {required_fragment}",
    )

integrity = manifest["integrity"]
require(integrity["checksums"] == "checksums.sha256", "checksums path mismatch")
require(integrity["archive_checksum"].endswith("-RC-publishing-pack.zip.sha256"), "archive checksum name mismatch")

expected_critical_paths = [
    aab["path"],
    *expected_graphics,
    *expected_screenshots,
    "legal/privacy_policy_ru.html",
    "store-listing/metadata/ru-RU/play_console_submission.md",
    "store-listing/metadata/ru-RU/asset_alt_text.md",
]
critical_files = integrity.get("critical_files")
require(isinstance(critical_files, list), "critical_files must be a list")
require(
    [record.get("path") for record in critical_files] == expected_critical_paths,
    "critical_files list does not match expected upload artifact inventory",
)
require(
    len({record.get("path") for record in critical_files}) == len(critical_files),
    "critical_files must not contain duplicate paths",
)
for record in critical_files:
    require(set(record) == {"path", "bytes", "sha256"}, f"invalid critical file record keys: {record}")
    require(isinstance(record["bytes"], int) and record["bytes"] > 0, f"invalid critical file size: {record['path']}")
    require(
        isinstance(record["sha256"], str) and re.fullmatch(r"[0-9a-f]{64}", record["sha256"]),
        f"invalid critical file SHA-256 format: {record['path']}",
    )
    path = require_path(record["path"])
    actual_size = path.stat().st_size
    actual_sha = hashlib.sha256(path.read_bytes()).hexdigest()
    require(record["bytes"] == actual_size, f"critical file size mismatch: {record['path']}")
    require(record["sha256"] == actual_sha, f"critical file SHA-256 mismatch: {record['path']}")

print("release_manifest.json structure and required paths are valid.")
PY

echo "== Local machine path leakage check =="
python3 - "$RC_DIR" <<'PY'
from pathlib import Path
import re
import sys

rc_dir = Path(sys.argv[1])
blocked_name_patterns = [
    re.compile(r"(^|/)local\.properties$", re.I),
]
blocked_text_patterns = {
    "sdk_dir": re.compile(r"(^|\n)\s*sdk\.dir\s*=", re.I),
    "absolute_user_path": re.compile(r"/" r"Users/", re.I),
    "foreign_user_path": re.compile(r"niko" r"lay", re.I),
}
text_suffixes = {
    ".html",
    ".json",
    ".md",
    ".properties",
    ".sh",
    ".txt",
    ".xml",
}

for path in rc_dir.rglob("*"):
    if not path.is_file():
        continue
    relative = path.relative_to(rc_dir).as_posix()
    if relative in {
        "scripts/package_release_candidate.sh",
        "scripts/validate_release_candidate.sh",
    }:
        continue
    for pattern in blocked_name_patterns:
        if pattern.search(relative):
            raise SystemExit(f"Forbidden local file in RC pack: {relative}")
    if path.suffix.lower() not in text_suffixes or path.stat().st_size > 2 * 1024 * 1024:
        continue
    text = path.read_text(encoding="utf-8", errors="ignore")
    hits = [
        name
        for name, pattern in blocked_text_patterns.items()
        if pattern.search(text)
    ]
    if hits:
        raise SystemExit(f"Local machine path/config leakage in {relative}: {', '.join(hits)}")

print("No local.properties, sdk.dir or absolute macOS user-home paths in RC text surfaces.")
PY

(
  cd "$RC_DIR"
  find . -type f ! -name "checksums.sha256" -print0 | sort -z | xargs -0 shasum -a 256 > checksums.sha256
  shasum -a 256 -c checksums.sha256 >/tmp/colorquarter_rc_checksums.log
)

rm -f "$RC_ROOT/$ZIP_NAME" "$RC_ROOT/$ZIP_NAME.sha256"
find "$RC_ROOT" -maxdepth 1 -type f \( \
  -name "ColorQuarter-$VERSION-RC-publishing-pack *.zip" -o \
  -name "ColorQuarter-$VERSION-RC-publishing-pack.zip *.sha256" \
\) -delete
(
  cd "$RC_ROOT"
  zip -r "$ZIP_NAME" "$VERSION" >/tmp/colorquarter_rc_zip.log
  shasum -a 256 "$ZIP_NAME" > "$ZIP_NAME.sha256"
  unzip -t "$ZIP_NAME" >/tmp/colorquarter_rc_unzip.log
)

cat /tmp/colorquarter_rc_checksums.log
tail -n 5 /tmp/colorquarter_rc_unzip.log
cat "$RC_ROOT/$ZIP_NAME.sha256"
normalize_numbered_build_dirs
echo "Release candidate package created: $RC_ROOT/$ZIP_NAME"
