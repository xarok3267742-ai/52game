#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

VERSION="${1:-1.0.0}"
RC_ROOT="release-candidate"
ZIP_NAME="ColorQuarter-$VERSION-RC-publishing-pack.zip"
ZIP_PATH="$RC_ROOT/$ZIP_NAME"
SHA_PATH="$ZIP_PATH.sha256"

require_file() {
  local path="$1"
  [[ -f "$path" ]] || {
    echo "Missing required file: $path" >&2
    exit 1
  }
}

require_file "$ZIP_PATH"
require_file "$SHA_PATH"

echo "== RC archive checksum =="
(
  cd "$RC_ROOT"
  shasum -a 256 -c "$ZIP_NAME.sha256"
)

echo "== RC archive extraction =="
tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT
unzip -q "$ZIP_PATH" -d "$tmp_dir"
unzip -t "$ZIP_PATH" >/tmp/colorquarter_verify_rc_unzip.log
tail -n 5 /tmp/colorquarter_verify_rc_unzip.log

extracted_rc_dir="$tmp_dir/$VERSION"
[[ -d "$extracted_rc_dir" ]] || {
  echo "RC archive does not contain top-level directory $VERSION" >&2
  exit 1
}

echo "== RC internal checksums =="
(
  cd "$extracted_rc_dir"
  shasum -a 256 -c checksums.sha256 >/tmp/colorquarter_verify_rc_checksums.log
)
tail -n 12 /tmp/colorquarter_verify_rc_checksums.log

echo "== RC manifest and handoff validation =="
python3 - "$extracted_rc_dir" "$VERSION" <<'PY'
from pathlib import Path
import hashlib
import json
import re
import sys

rc_dir = Path(sys.argv[1])
expected_version = sys.argv[2]
manifest_path = rc_dir / "release_manifest.json"

if not manifest_path.is_file():
    raise SystemExit(f"Missing release_manifest.json in {rc_dir}")

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

product = manifest.get("product", {})
require(product.get("name") == "Цветной Квартал", "unexpected product name")
require(product.get("application_id") == "ru.cisgame.colorquarter", "unexpected application_id")
require(product.get("version_name") == expected_version, "version_name mismatch")
require(str(product.get("version_code")) == "1", "version_code mismatch")
require(product.get("build_type") == "release", "build_type must be release")
require(product.get("target_sdk") == 35, "target_sdk mismatch")

aab = manifest.get("aab", {})
aab_status = aab.get("status")
require(aab_status in {"signed", "unsigned"}, "AAB status must be signed or unsigned")
expected_aab_path = (
    "signed-aab/app-release.aab"
    if aab_status == "signed"
    else "unsigned-aab/app-release-unsigned.aab"
)
require(aab.get("path") == expected_aab_path, "AAB path/status mismatch")
require(aab.get("upload_ready") is (aab_status == "signed"), "AAB upload_ready mismatch")
require_path(aab["path"])

store_listing = manifest.get("store_listing", {})
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
required_metadata = {
    "store-listing/metadata/ru-RU/app_access_notes.md",
    "store-listing/metadata/ru-RU/asset_alt_text.md",
    "store-listing/metadata/ru-RU/content_rating_notes.md",
    "store-listing/metadata/ru-RU/data_safety.md",
    "store-listing/metadata/ru-RU/play_console_submission.md",
    "store-listing/metadata/ru-RU/privacy_policy.md",
    "store-listing/metadata/ru-RU/target_audience_notes.md",
}
require(store_listing.get("graphics") == expected_graphics, "unexpected graphics inventory")
require(store_listing.get("phone_screenshots") == expected_screenshots, "unexpected screenshot inventory")
require(required_metadata.issubset(set(store_listing.get("metadata", []))), "metadata inventory incomplete")
for relative_path in expected_graphics + expected_screenshots + list(required_metadata):
    require_path(relative_path)
require_path(store_listing.get("privacy_policy_html"))
require_path(store_listing.get("privacy_policy_markdown"))

graphics_dir = rc_dir / "store-listing/graphics"
actual_graphics = sorted(path.relative_to(rc_dir).as_posix() for path in graphics_dir.iterdir() if path.is_file())
require(actual_graphics == expected_graphics, f"unexpected uploadable graphics files: {actual_graphics}")

screenshots_dir = rc_dir / "store-listing/screenshots/phone"
actual_screenshots = sorted(path.relative_to(rc_dir).as_posix() for path in screenshots_dir.iterdir() if path.is_file())
require(actual_screenshots == expected_screenshots, f"unexpected uploadable screenshot files: {actual_screenshots}")

documentation = manifest.get("documentation", {})
required_doc_keys = {
    "agents",
    "build_environment",
    "google_play_checklist",
    "play_console_runbook",
    "readme",
    "release_report",
    "security_audit",
    "signing_guide",
    "third_party_notices",
}
require(set(documentation) == required_doc_keys, "documentation inventory mismatch")
for relative_path in documentation.values():
    require_path(relative_path)

critical_files = manifest.get("integrity", {}).get("critical_files")
require(isinstance(critical_files, list), "critical_files must be a list")
expected_critical_paths = [
    aab["path"],
    *expected_graphics,
    *expected_screenshots,
    "legal/privacy_policy_ru.html",
    "store-listing/metadata/ru-RU/play_console_submission.md",
    "store-listing/metadata/ru-RU/asset_alt_text.md",
]
require(
    [record.get("path") for record in critical_files] == expected_critical_paths,
    "critical_files inventory mismatch",
)
require(
    len({record.get("path") for record in critical_files}) == len(critical_files),
    "critical_files must not contain duplicate paths",
)
for record in critical_files:
    require(set(record) == {"path", "bytes", "sha256"}, f"invalid critical file record keys: {record}")
    require(isinstance(record["bytes"], int) and record["bytes"] > 0, f"invalid size for {record['path']}")
    require(
        isinstance(record["sha256"], str) and re.fullmatch(r"[0-9a-f]{64}", record["sha256"]),
        f"invalid sha256 for {record['path']}",
    )
    path = require_path(record["path"])
    require(path.stat().st_size == record["bytes"], f"size mismatch for {record['path']}")
    require(hashlib.sha256(path.read_bytes()).hexdigest() == record["sha256"], f"sha mismatch for {record['path']}")

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
    ".txt",
    ".xml",
}
for path in rc_dir.rglob("*"):
    if not path.is_file():
        continue
    relative = path.relative_to(rc_dir).as_posix()
    if relative.startswith("scripts/"):
        continue
    for pattern in blocked_name_patterns:
        require(not pattern.search(relative), f"forbidden local file in RC pack: {relative}")
    if path.suffix.lower() not in text_suffixes or path.stat().st_size > 2 * 1024 * 1024:
        continue
    text = path.read_text(encoding="utf-8", errors="ignore")
    hits = [name for name, pattern in blocked_text_patterns.items() if pattern.search(text)]
    require(not hits, f"local path/config leakage in {relative}: {', '.join(hits)}")

print(f"Verified RC manifest for {product['application_id']} {product['version_name']} ({product['version_code']}).")
print(f"AAB status: {aab_status}; upload_ready={aab['upload_ready']}.")
print(f"Critical files verified: {len(critical_files)}.")
PY

echo "Release candidate package verification completed."
