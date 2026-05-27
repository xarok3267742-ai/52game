#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -z "${JAVA_HOME:-}" && -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi

if [[ -z "${ANDROID_HOME:-}" && -d "$HOME/Library/Android/sdk" ]]; then
  export ANDROID_HOME="$HOME/Library/Android/sdk"
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
  export PATH="$JAVA_HOME/bin:${PATH}"
fi

if [[ -n "${ANDROID_HOME:-}" ]]; then
  export PATH="$ANDROID_HOME/platform-tools:${PATH}"
fi

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

if [[ "${1:-}" == "--normalize-only" ]]; then
  normalize_numbered_build_dirs
  exit 0
fi

GRADLE_CMD=("./gradlew")
if [[ -n "${GRADLE_EXTRA_ARGS:-}" ]]; then
  # shellcheck disable=SC2206
  EXTRA_ARGS=(${GRADLE_EXTRA_ARGS})
  GRADLE_CMD+=("${EXTRA_ARGS[@]}")
fi

require_file() {
  local path="$1"
  [[ -f "$path" ]] || {
    echo "Missing required file: $path" >&2
    exit 1
  }
}

echo "== Gradle release checks =="
normalize_numbered_build_dirs
"${GRADLE_CMD[@]}" clean test assembleDebug lint bundleRelease
normalize_numbered_build_dirs

echo "== Signing report =="
# AGP signingReport can trip over stale configuration-cache classloaders in local
# desktop sessions. Keep the publishing build cached, but run this diagnostic
# task isolated so release validation remains repeatable.
"${GRADLE_CMD[@]}" --no-configuration-cache :app:signingReport
normalize_numbered_build_dirs

echo "== Required artifacts =="
require_file "app/build/outputs/apk/debug/app-debug.apk"
require_file "app/build/outputs/bundle/release/app-release.aab"
require_file "play-assets/graphics/app_icon_512.png"
require_file "play-assets/graphics/feature_graphic.png"
require_file "play-assets/legal/privacy_policy_ru.html"
require_file "play-assets/metadata/ru-RU/play_console_submission.md"

echo "== Release automation script validation =="
for script in \
  "scripts/validate_release_candidate.sh" \
  "scripts/package_release_candidate.sh" \
  "scripts/verify_release_candidate_package.sh"; do
  require_file "$script"
  if [[ ! -x "$script" ]]; then
    echo "$script must be executable" >&2
    exit 1
  fi
  bash -n "$script"
  echo "$script: syntax and executable bit OK"
done

echo "== Signing hygiene check =="
python3 - <<'PY'
from pathlib import Path
import os

root = Path.cwd().resolve()
gitignore_path = root / ".gitignore"
if not gitignore_path.is_file():
    raise SystemExit(".gitignore is required for signing hygiene")

gitignore_lines = {
    line.strip()
    for line in gitignore_path.read_text(encoding="utf-8").splitlines()
    if line.strip() and not line.strip().startswith("#")
}
required_ignores = {
    "keystore.properties",
    "*.jks",
    "*.keystore",
    "*.p12",
    "*.pfx",
    "local.properties",
}
missing_ignores = sorted(required_ignores - gitignore_lines)
if missing_ignores:
    raise SystemExit(".gitignore misses private/local signing patterns: " + ", ".join(missing_ignores))

ignored_dirs = {
    ".gradle",
    ".idea",
    "app/build",
    "build",
    "release-candidate",
}
private_suffixes = {".jks", ".keystore", ".p12", ".pfx"}
private_files = []
for path in root.rglob("*"):
    if not path.is_file():
        continue
    relative = path.relative_to(root).as_posix()
    if any(relative == ignored or relative.startswith(f"{ignored}/") for ignored in ignored_dirs):
        continue
    if path.suffix.lower() in private_suffixes:
        private_files.append(relative)
if private_files:
    raise SystemExit(
        "Private binary signing material must stay outside the project tree: "
        + ", ".join(sorted(private_files))
    )

example_path = root / "keystore.properties.example"
if not example_path.is_file():
    raise SystemExit("keystore.properties.example is required for signing handoff")

example = example_path.read_text(encoding="utf-8")
for key in ("storeFile=", "storePassword=", "keyAlias=", "keyPassword="):
    if key not in example:
        raise SystemExit(f"keystore.properties.example missing {key}")

local_keystore_path = root / "keystore.properties"
if local_keystore_path.exists():
    properties = {}
    for raw_line in local_keystore_path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            raise SystemExit(f"keystore.properties has malformed line: {raw_line}")
        key, value = line.split("=", 1)
        properties[key.strip()] = value.strip()
    required_keys = ("storeFile", "storePassword", "keyAlias", "keyPassword")
    missing_keys = [key for key in required_keys if not properties.get(key)]
    if missing_keys:
        raise SystemExit("keystore.properties misses required keys: " + ", ".join(missing_keys))
    for secret_key in ("storePassword", "keyPassword"):
        lowered = properties[secret_key].lower()
        if lowered in {"change-me", "changeme", "password"} or "<" in lowered or ">" in lowered:
            raise SystemExit(f"keystore.properties {secret_key} still looks like a placeholder")
    store_file = Path(properties["storeFile"]).expanduser()
    if not store_file.is_absolute():
        store_file = (root / store_file).resolve()
    else:
        store_file = store_file.resolve()
    try:
        store_file.relative_to(root)
    except ValueError:
        pass
    else:
        raise SystemExit("Production upload keystore file must be outside the project tree")
    if not store_file.is_file():
        raise SystemExit("keystore.properties storeFile does not exist")
    print("Private keystore.properties present; keys are complete and storeFile is outside project tree.")
else:
    print("Private keystore.properties absent; unsigned local AAB is expected.")

print(".gitignore protects local/signing files; no private signing material found in project tree.")
PY

echo "== Gradle wrapper validation =="
python3 - <<'PY'
from pathlib import Path
from zipfile import ZipFile
import os
import re

wrapper_properties_path = Path("gradle/wrapper/gradle-wrapper.properties")
wrapper_jar_path = Path("gradle/wrapper/gradle-wrapper.jar")
gradlew_path = Path("gradlew")

expected_checksums = {
    "https://services.gradle.org/distributions/gradle-8.13-bin.zip":
        "20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78",
}

for path in (wrapper_properties_path, wrapper_jar_path, gradlew_path):
    if not path.exists():
        raise SystemExit(f"Missing Gradle wrapper file: {path}")

if not os.access(gradlew_path, os.X_OK):
    raise SystemExit("gradlew must be executable")

properties = {}
for raw_line in wrapper_properties_path.read_text(encoding="utf-8").splitlines():
    line = raw_line.strip()
    if not line or line.startswith("#"):
        continue
    if "=" not in line:
        raise SystemExit(f"{wrapper_properties_path}: malformed line: {raw_line}")
    key, value = line.split("=", 1)
    properties[key] = value

distribution_url = properties.get("distributionUrl", "").replace("\\:", ":")
distribution_sha = properties.get("distributionSha256Sum", "")

if properties.get("validateDistributionUrl") != "true":
    raise SystemExit("Gradle wrapper must set validateDistributionUrl=true")
if distribution_url not in expected_checksums:
    raise SystemExit(f"Unexpected Gradle distributionUrl: {distribution_url}")
if distribution_sha != expected_checksums[distribution_url]:
    raise SystemExit("Gradle distributionSha256Sum does not match the approved distribution")
if not re.fullmatch(r"[a-f0-9]{64}", distribution_sha):
    raise SystemExit("Gradle distributionSha256Sum must be a 64-char lowercase SHA-256")

with ZipFile(wrapper_jar_path) as wrapper_jar:
    names = set(wrapper_jar.namelist())
    required_entries = {
        "org/gradle/wrapper/GradleWrapperMain.class",
        "org/gradle/wrapper/WrapperExecutor.class",
        "org/gradle/wrapper/Download.class",
    }
    missing_entries = sorted(required_entries - names)
    if missing_entries:
        raise SystemExit(
            "Gradle wrapper jar misses required entries: " + ", ".join(missing_entries)
        )

print(f"Gradle wrapper pinned to {distribution_url} with verified SHA-256.")
PY

echo "== Build environment documentation check =="
python3 - <<'PY'
from pathlib import Path

doc_path = Path("docs/build_environment.md")
if not doc_path.is_file():
    raise SystemExit("docs/build_environment.md is required for release handoff")

text = doc_path.read_text(encoding="utf-8")
required_fragments = (
    "Gradle 8.13",
    "gradle-8.13-bin.zip",
    "20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78",
    "Android Gradle Plugin | 8.13.2",
    "Kotlin Android plugin | 2.2.20",
    "Kotlin Compose compiler plugin | 2.2.20",
    "Compose BOM | 2026.04.01",
    "JetBrains Runtime / OpenJDK 21.0.5",
    "JavaVersion.VERSION_17",
    "JvmTarget.JVM_17",
    "`compileSdk` | 36",
    "`targetSdk` | 35",
    "`minSdk` | 23",
    "Android command-line tools | 20.0",
    "Android emulator used in latest QA evidence | 35.4.9.0",
    "ADB used in latest QA evidence | 35.0.2 / 1.0.41",
    "project_52game_emulator",
)
missing = [fragment for fragment in required_fragments if fragment not in text]
if missing:
    raise SystemExit(
        "docs/build_environment.md misses required build environment facts: "
        + ", ".join(missing)
    )
print("Build environment documentation is present and matches the approved RC toolchain posture.")
PY

echo "== Local machine path leakage check =="
python3 - <<'PY'
from pathlib import Path
from zipfile import ZipFile
import re

blocked_name_patterns = [
    re.compile(r"(^|/)local\.properties$", re.I),
]
blocked_text_patterns = {
    "local_properties": re.compile(r"local\.properties", re.I),
    "sdk_dir": re.compile(r"(^|\n)\s*sdk\.dir\s*=", re.I),
    "android_sdk_user_path": re.compile(r"/" r"Users/" r"[^\\s\"'<>]+/Library/Android/sdk", re.I),
    "foreign_user_path": re.compile(r"niko" r"lay", re.I),
}
text_suffixes = {
    ".txt",
    ".xml",
    ".json",
    ".properties",
    ".mf",
    ".map",
    ".version",
}

def check_text(label, text):
    hits = []
    for name, pattern in blocked_text_patterns.items():
        if pattern.search(text):
            hits.append(name)
    if hits:
        raise SystemExit(f"Local machine path/config leakage in {label}: {', '.join(hits)}")

def check_zip(path):
    with ZipFile(path) as archive:
        for info in archive.infolist():
            for pattern in blocked_name_patterns:
                if pattern.search(info.filename):
                    raise SystemExit(f"{path}: forbidden local file packaged: {info.filename}")
            suffix = Path(info.filename).suffix.lower()
            if suffix in text_suffixes and info.file_size <= 2 * 1024 * 1024:
                data = archive.read(info)
                text = data.decode("utf-8", errors="ignore")
                check_text(f"{path}:{info.filename}", text)

check_zip(Path("app/build/outputs/bundle/release/app-release.aab"))
print("No local.properties, sdk.dir or user-specific Android SDK paths in release AAB.")
PY

echo "== Security and dependency surface scan =="
python3 - <<'PY'
from pathlib import Path
import re

build_surface_files = [
    Path("settings.gradle.kts"),
    Path("build.gradle.kts"),
    Path("gradle.properties"),
    Path("app/build.gradle.kts"),
]

forbidden_dependency_markers = {
    "ads": [
        "play-services-ads",
        "admob",
        "applovin",
        "unity-ads",
        "ironsource",
    ],
    "analytics_or_attribution": [
        "firebase-analytics",
        "google-services",
        "appmetrica",
        "yandex-metrica",
        "amplitude",
        "mixpanel",
        "appsflyer",
        "adjust",
        "facebook-android-sdk",
    ],
    "crash_reporting": [
        "crashlytics",
        "sentry",
        "bugsnag",
    ],
    "payments": [
        "billingclient",
        "com.android.billingclient",
        "stripe",
        "revenuecat",
    ],
    "backend_or_networking": [
        "firebase-database",
        "firebase-firestore",
        "firebase-auth",
        "firebase-storage",
        "retrofit",
        "okhttp",
        "ktor-client",
    ],
}

build_text = "\n".join(
    f"\n# {path}\n{path.read_text(encoding='utf-8')}"
    for path in build_surface_files
    if path.exists()
).lower()

blocked_dependency_hits = []
for category, markers in forbidden_dependency_markers.items():
    for marker in markers:
        if marker in build_text:
            blocked_dependency_hits.append(f"{category}:{marker}")

if blocked_dependency_hits:
    raise SystemExit(
        "Forbidden SDK/dependency markers found in Gradle surfaces: "
        + ", ".join(sorted(blocked_dependency_hits))
    )

root_build_path = Path("build.gradle.kts")
settings_path = Path("settings.gradle.kts")
app_build_path = Path("app/build.gradle.kts")
root_build = root_build_path.read_text(encoding="utf-8")
settings_text = settings_path.read_text(encoding="utf-8")
app_build = app_build_path.read_text(encoding="utf-8")

expected_plugins = {
    "com.android.application": "8.13.2",
    "org.jetbrains.kotlin.android": "2.2.20",
    "org.jetbrains.kotlin.plugin.compose": "2.2.20",
}
actual_plugins = dict(
    re.findall(r'id\("([^"]+)"\)\s+version\s+"([^"]+)"\s+apply\s+false', root_build)
)
if actual_plugins != expected_plugins:
    raise SystemExit(f"Unexpected Gradle plugin inventory: {actual_plugins}")

if "repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)" not in settings_text:
    raise SystemExit("settings.gradle.kts must keep RepositoriesMode.FAIL_ON_PROJECT_REPOS")
if settings_text.count("google()") != 2 or settings_text.count("mavenCentral()") != 2:
    raise SystemExit("settings.gradle.kts must only use google() and mavenCentral() in plugin/dependency repositories")
if "gradlePluginPortal()" not in settings_text:
    raise SystemExit("settings.gradle.kts must keep gradlePluginPortal() for plugin resolution")

expected_dependency_coordinates = {
    "androidx.activity:activity-compose:1.13.0",
    "androidx.compose:compose-bom:2026.04.01",
    "androidx.compose.material3:material3",
    "androidx.compose.ui:ui",
    "androidx.compose.ui:ui-test-manifest",
    "androidx.compose.ui:ui-tooling",
    "androidx.compose.ui:ui-tooling-preview",
    "junit:junit:4.13.2",
}
actual_dependency_coordinates = set(
    re.findall(r'"([A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+(?::[A-Za-z0-9_.+-]+)?)"', app_build)
)
if actual_dependency_coordinates != expected_dependency_coordinates:
    missing = sorted(expected_dependency_coordinates - actual_dependency_coordinates)
    extra = sorted(actual_dependency_coordinates - expected_dependency_coordinates)
    raise SystemExit(f"Direct dependency inventory changed; missing={missing}, extra={extra}")

allowed_dependency_expressions = {
    'implementation(composeBom)',
    'androidTestImplementation(composeBom)',
    'implementation("androidx.activity:activity-compose:1.13.0")',
    'implementation("androidx.compose.material3:material3")',
    'implementation("androidx.compose.ui:ui")',
    'implementation("androidx.compose.ui:ui-tooling-preview")',
    'debugImplementation("androidx.compose.ui:ui-tooling")',
    'debugImplementation("androidx.compose.ui:ui-test-manifest")',
    'testImplementation("junit:junit:4.13.2")',
}
dependency_call_pattern = re.compile(
    r'^\s*(implementation|androidTestImplementation|debugImplementation|testImplementation)\((.+)\)\s*$',
    re.M,
)
actual_dependency_expressions = {
    f"{method}({expression.strip()})"
    for method, expression in dependency_call_pattern.findall(app_build)
}
if actual_dependency_expressions != allowed_dependency_expressions:
    missing = sorted(allowed_dependency_expressions - actual_dependency_expressions)
    extra = sorted(actual_dependency_expressions - allowed_dependency_expressions)
    raise SystemExit(f"Unexpected dependency declarations; missing={missing}, extra={extra}")

print("Direct Gradle plugin and dependency inventories match the approved v1.0 allowlist.")

notices_path = Path("docs/third_party_notices.md")
if not notices_path.is_file():
    raise SystemExit("docs/third_party_notices.md is required for release handoff")
notices = notices_path.read_text(encoding="utf-8")
required_notice_fragments = (
    "AndroidX",
    "Jetpack Compose",
    "Kotlin standard library",
    "Kotlinx coroutines",
    "JUnit 4.13.2",
    "Hamcrest",
    "Apache License 2.0",
    "Eclipse Public License 1.0",
    "BSD-style license",
    "GPL, LGPL or AGPL direct dependencies",
    "releaseRuntimeClasspath",
)
missing_notice_fragments = [
    fragment for fragment in required_notice_fragments if fragment not in notices
]
if missing_notice_fragments:
    raise SystemExit(
        "docs/third_party_notices.md misses required notices: "
        + ", ".join(missing_notice_fragments)
    )
print("Third-party notices document is present and matches the approved dependency posture.")

source_roots = [
    Path("app/src/main"),
]

text_file_extensions = {
    ".kt",
    ".kts",
    ".java",
    ".xml",
    ".properties",
    ".json",
}

source_files = []
for root in source_roots:
    if not root.exists():
        continue
    source_files.extend(
        path
        for path in root.rglob("*")
        if path.is_file() and path.suffix.lower() in text_file_extensions
    )

secret_patterns = {
    "private_key": re.compile(r"BEGIN (RSA |EC |OPENSSH |)PRIVATE KEY", re.I),
    "api_key_assignment": re.compile(r"\b(api[_-]?key|client[_-]?secret|access[_-]?token)\b\s*[:=]\s*[\"'][A-Za-z0-9_\-]{16,}[\"']", re.I),
    "bearer_token": re.compile(r"\bBearer\s+[A-Za-z0-9_\-\.]{20,}", re.I),
}

url_pattern = re.compile(r"https?://[^\s\"'<>]+", re.I)
allowed_source_url_prefixes = (
    "http://schemas.android.com/",
)

secret_hits = []
url_hits = []
for path in source_files:
    text = path.read_text(encoding="utf-8", errors="ignore")
    for name, pattern in secret_patterns.items():
        if pattern.search(text):
            secret_hits.append(f"{path}:{name}")
    for url in url_pattern.findall(text):
        if not url.startswith(allowed_source_url_prefixes):
            url_hits.append(f"{path}:{url}")

if secret_hits:
    raise SystemExit("Secret-like values found in app release source: " + ", ".join(secret_hits))
if url_hits:
    raise SystemExit("Network/backend URL found in app release source: " + ", ".join(url_hits))

print("No forbidden ads/analytics/crash/payment/backend SDK markers in Gradle surfaces.")
print("No secret-like values or network/backend URLs in app release source.")
PY

echo "== Asset and metadata validation =="
python3 - <<'PY'
from pathlib import Path
import re
import struct
import sys
import zlib

def png_info(path):
    data = Path(path).read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise SystemExit(f"{path}: not a PNG")
    if data[12:16] != b"IHDR":
        raise SystemExit(f"{path}: missing IHDR")
    width, height, bit_depth, color_type = struct.unpack(">IIBB", data[16:26])
    return width, height, bit_depth, color_type

def png_chunks(path):
    data = Path(path).read_bytes()
    chunks = []
    position = 8
    while position + 12 <= len(data):
        length = struct.unpack(">I", data[position:position + 4])[0]
        chunk_type = data[position + 4:position + 8].decode("ascii")
        chunk_data = data[position + 8:position + 8 + length]
        chunks.append((chunk_type, chunk_data))
        position += 12 + length
        if chunk_type == "IEND":
            break
    return chunks

def paeth_predictor(left, up, upper_left):
    estimate = left + up - upper_left
    distance_left = abs(estimate - left)
    distance_up = abs(estimate - up)
    distance_upper_left = abs(estimate - upper_left)
    if distance_left <= distance_up and distance_left <= distance_upper_left:
        return left
    if distance_up <= distance_upper_left:
        return up
    return upper_left

def rgba_rows(path):
    width, height, bit_depth, color_type = png_info(path)
    if bit_depth != 8 or color_type != 6:
        raise SystemExit(f"{path}: expected 8-bit RGBA PNG for alpha validation")

    compressed = b"".join(
        chunk_data
        for chunk_type, chunk_data in png_chunks(path)
        if chunk_type == "IDAT"
    )
    if not compressed:
        raise SystemExit(f"{path}: PNG has no IDAT data")

    raw = zlib.decompress(compressed)
    bytes_per_pixel = 4
    row_length = width * bytes_per_pixel
    expected_length = height * (1 + row_length)
    if len(raw) != expected_length:
        raise SystemExit(
            f"{path}: decompressed PNG length {len(raw)} does not match expected {expected_length}"
        )

    rows = []
    previous = bytearray(row_length)
    offset = 0
    for _ in range(height):
        filter_type = raw[offset]
        offset += 1
        source = raw[offset:offset + row_length]
        offset += row_length
        row = bytearray(row_length)

        for index, value in enumerate(source):
            left = row[index - bytes_per_pixel] if index >= bytes_per_pixel else 0
            up = previous[index]
            upper_left = previous[index - bytes_per_pixel] if index >= bytes_per_pixel else 0
            if filter_type == 0:
                decoded = value
            elif filter_type == 1:
                decoded = value + left
            elif filter_type == 2:
                decoded = value + up
            elif filter_type == 3:
                decoded = value + ((left + up) // 2)
            elif filter_type == 4:
                decoded = value + paeth_predictor(left, up, upper_left)
            else:
                raise SystemExit(f"{path}: unsupported PNG filter type {filter_type}")
            row[index] = decoded & 0xFF

        rows.append(bytes(row))
        previous = row
    return rows

def assert_fully_opaque_rgba(path):
    alpha_values = {
        row[index]
        for row in rgba_rows(path)
        for index in range(3, len(row), 4)
    }
    if alpha_values != {255}:
        raise SystemExit(f"{path}: app icon alpha must be fully opaque, got {sorted(alpha_values)[:8]}")
    print(f"{path}: alpha channel fully opaque")

png_expectations = {
    "play-assets/graphics/app_icon_512.png": (512, 512, 8, 6),
    "play-assets/graphics/feature_graphic.png": (1024, 500, 8, 2),
    "play-assets/screenshots/phone/01_onboarding.png": (1080, 2400, 8, 2),
    "play-assets/screenshots/phone/02_home.png": (1080, 2400, 8, 2),
    "play-assets/screenshots/phone/03_level.png": (1080, 2400, 8, 2),
    "play-assets/screenshots/phone/04_victory.png": (1080, 2400, 8, 2),
    "play-assets/screenshots/phone/05_settings.png": (1080, 2400, 8, 2),
    "play-assets/screenshots/phone/06_about_privacy.png": (1080, 2400, 8, 2),
}

for path, expected in png_expectations.items():
    actual = png_info(path)
    width, height, bit_depth, color_type = actual
    exp_width, exp_height, exp_bit_depth, exp_color_type = expected
    if (width, height, bit_depth) != (exp_width, exp_height, exp_bit_depth):
        raise SystemExit(f"{path}: expected {expected}, got {actual}")
    if exp_color_type is not None and color_type != exp_color_type:
        raise SystemExit(f"{path}: expected PNG color type {exp_color_type}, got {color_type}")
    print(f"{path}: {width}x{height}, bit_depth={bit_depth}, color_type={color_type}")

app_icon_path = "play-assets/graphics/app_icon_512.png"
app_icon_size = Path(app_icon_path).stat().st_size
if app_icon_size > 1024 * 1024:
    raise SystemExit(f"{app_icon_path}: {app_icon_size} bytes exceeds Google Play 1024 KB app icon limit")
print(f"{app_icon_path}: {app_icon_size} bytes / 1048576 bytes")
assert_fully_opaque_rgba(app_icon_path)

graphics_dir = Path("play-assets/graphics")
unexpected_graphics = sorted(
    path.name
    for path in graphics_dir.iterdir()
    if path.is_file() and path.suffix.lower() != ".png"
)
if unexpected_graphics:
    raise SystemExit(
        "Only uploadable PNG graphics may remain in play-assets/graphics: "
        + ", ".join(unexpected_graphics)
    )
print("play-assets/graphics: only uploadable PNG graphics remain")

limits = {
    "play-assets/metadata/ru-RU/title.txt": 30,
    "play-assets/metadata/ru-RU/short_description.txt": 80,
    "play-assets/metadata/ru-RU/full_description.txt": 4000,
    "play-assets/metadata/ru-RU/release_notes.txt": 500,
}

for path, limit in limits.items():
    text = Path(path).read_text(encoding="utf-8").strip()
    if len(text) > limit:
        raise SystemExit(f"{path}: {len(text)} chars exceeds limit {limit}")
    print(f"{path}: {len(text)} / {limit} chars")

submission_path = Path("play-assets/metadata/ru-RU/play_console_submission.md")
submission = submission_path.read_text(encoding="utf-8")
submission_fields = {
    "Title": Path("play-assets/metadata/ru-RU/title.txt").read_text(encoding="utf-8").strip(),
    "Short description": Path("play-assets/metadata/ru-RU/short_description.txt").read_text(encoding="utf-8").strip(),
    "Full description": Path("play-assets/metadata/ru-RU/full_description.txt").read_text(encoding="utf-8").strip(),
    "Release notes": Path("play-assets/metadata/ru-RU/release_notes.txt").read_text(encoding="utf-8").strip(),
}

for label, expected_text in submission_fields.items():
    pattern = rf"{re.escape(label)}:\s*```text\n(.*?)\n```"
    match = re.search(pattern, submission, re.S)
    if not match:
        raise SystemExit(f"{submission_path}: missing {label} code block")
    actual_text = match.group(1).strip()
    if actual_text != expected_text:
        raise SystemExit(
            f"{submission_path}: {label} does not match canonical metadata file"
        )
    print(f"{submission_path}: {label} matches canonical metadata")

app_name = submission_fields["Title"]
if f"- App name: `{app_name}`" not in submission:
    raise SystemExit(f"{submission_path}: App name detail does not match title.txt")
for required_path in (
    "play-assets/graphics/app_icon_512.png",
    "play-assets/graphics/feature_graphic.png",
    "play-assets/screenshots/phone/01_onboarding.png",
    "play-assets/screenshots/phone/02_home.png",
    "play-assets/screenshots/phone/03_level.png",
    "play-assets/screenshots/phone/04_victory.png",
    "play-assets/screenshots/phone/05_settings.png",
    "play-assets/screenshots/phone/06_about_privacy.png",
    "play-assets/legal/privacy_policy_ru.html",
):
    if required_path not in submission:
        raise SystemExit(f"{submission_path}: missing referenced path {required_path}")
print(f"{submission_path}: store listing copy and required asset references are consistent")

asset_alt_text_path = Path("play-assets/metadata/ru-RU/asset_alt_text.md")
asset_alt_text = asset_alt_text_path.read_text(encoding="utf-8")
asset_alt_text_entries = dict(
    re.findall(r"- `([^`]+)`: ([^\n]+)", asset_alt_text)
)
expected_alt_text_paths = (
    "play-assets/graphics/app_icon_512.png",
    "play-assets/graphics/feature_graphic.png",
    "play-assets/screenshots/phone/01_onboarding.png",
    "play-assets/screenshots/phone/02_home.png",
    "play-assets/screenshots/phone/03_level.png",
    "play-assets/screenshots/phone/04_victory.png",
    "play-assets/screenshots/phone/05_settings.png",
    "play-assets/screenshots/phone/06_about_privacy.png",
)
if set(asset_alt_text_entries) != set(expected_alt_text_paths):
    missing = sorted(set(expected_alt_text_paths) - set(asset_alt_text_entries))
    extra = sorted(set(asset_alt_text_entries) - set(expected_alt_text_paths))
    raise SystemExit(
        f"{asset_alt_text_path}: alt text inventory mismatch; missing={missing}, extra={extra}"
    )
for asset_path in expected_alt_text_paths:
    text = asset_alt_text_entries[asset_path].strip()
    if not text:
        raise SystemExit(f"{asset_alt_text_path}: empty alt text for {asset_path}")
    if len(text) > 140:
        raise SystemExit(f"{asset_alt_text_path}: alt text for {asset_path} is {len(text)} chars, limit 140")
    if any(marker in text.lower() for marker in ("todo", "placeholder", "lorem", "ipsum")):
        raise SystemExit(f"{asset_alt_text_path}: placeholder-like alt text for {asset_path}")
print(f"{asset_alt_text_path}: {len(expected_alt_text_paths)} alt text entries are present and <=140 chars")

html = Path("play-assets/legal/privacy_policy_ru.html").read_text(encoding="utf-8")
for needle in ("<html", "</html>", "Политика конфиденциальности", "Цветной Квартал"):
    if needle not in html:
        raise SystemExit(f"privacy_policy_ru.html missing {needle!r}")
print("play-assets/legal/privacy_policy_ru.html: basic content OK")
PY

echo "== Privacy and Play form consistency check =="
python3 - <<'PY'
from pathlib import Path
import re

files = {
    "privacy_md": Path("play-assets/legal/privacy_policy_ru.md"),
    "privacy_html": Path("play-assets/legal/privacy_policy_ru.html"),
    "data_safety": Path("play-assets/metadata/ru-RU/data_safety.md"),
    "submission": Path("play-assets/metadata/ru-RU/play_console_submission.md"),
    "app_access": Path("play-assets/metadata/ru-RU/app_access_notes.md"),
    "target_audience": Path("play-assets/metadata/ru-RU/target_audience_notes.md"),
    "privacy_docs": Path("docs/privacy_and_permissions.md"),
}

missing = [str(path) for path in files.values() if not path.is_file()]
if missing:
    raise SystemExit("Missing privacy consistency inputs: " + ", ".join(missing))

texts = {
    name: path.read_text(encoding="utf-8").lower()
    for name, path in files.items()
}

def assert_contains(name, needle):
    if needle.lower() not in texts[name]:
        raise SystemExit(f"{files[name]} missing required privacy claim: {needle}")

def assert_regex(name, pattern, label):
    if not re.search(pattern, texts[name], re.I | re.S):
        raise SystemExit(f"{files[name]} missing required privacy claim: {label}")

for name in ("privacy_md", "privacy_html", "privacy_docs"):
    assert_contains(name, "не собирает")
    assert_contains(name, "не переда")
    assert_contains(name, "без аккаунта")
    assert_contains(name, "android auto backup отключ")
    assert_contains(name, "нет аналитики")
    assert_contains(name, "реклам")
    assert_contains(name, "покуп")

assert_contains("privacy_md", "незавершённая попытка")
assert_contains("privacy_html", "незавершённая попытка")
assert_contains("privacy_docs", "незавершённая попытка")

assert_regex("data_safety", r"data collected\s*\|\s*no", "Data collected | No")
assert_regex("data_safety", r"data shared with third parties\s*\|\s*no", "Data shared with third parties | No")
assert_contains("data_safety", "app works offline")
assert_contains("data_safety", "no user data transmitted")
assert_contains("data_safety", "не покидают устройство")
assert_contains("data_safety", "не запрашивает android system permissions")

assert_contains("submission", "contains ads: no")
assert_contains("submission", "paid app: no")
assert_contains("submission", "не требует интернета")
assert_contains("submission", "аккаунта")
assert_contains("submission", "покупок")
assert_contains("submission", "сбора данных")
assert_contains("submission", "data safety: use `play-assets/metadata/ru-ru/data_safety.md`")
assert_contains("submission", "permissions: no android system permissions/runtime prompts")

assert_contains("app_access", "all app functionality is available without special access")
assert_contains("app_access", "нет аккаунта")
assert_contains("app_access", "нет логина")

assert_contains("target_audience", "13+")
assert_contains("target_audience", "не выбирать группы младше 13")
assert_contains("target_audience", "нет рекламы")
assert_contains("target_audience", "платеж")
assert_contains("target_audience", "сбора данных")

print("Privacy policy, Data Safety, App Access, Target Audience and Play submission claims are consistent.")
PY

echo "== Manifest privacy check =="
python3 - <<'PY'
from pathlib import Path
import xml.etree.ElementTree as ET

manifest_path = Path("app/build/intermediates/merged_manifests/release/processReleaseManifest/AndroidManifest.xml")
if not manifest_path.exists():
    raise SystemExit(f"Missing merged release manifest: {manifest_path}")

android = "{http://schemas.android.com/apk/res/android}"
root = ET.parse(manifest_path).getroot()
package_name = root.attrib.get("package")
allowed_internal_permissions = {
    f"{package_name}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
}

uses_permissions = [
    node.attrib.get(android + "name", "")
    for node in root.findall("uses-permission")
]
unexpected_permissions = [
    name for name in uses_permissions
    if name not in allowed_internal_permissions
]
android_permissions = [
    name for name in uses_permissions
    if name.startswith("android.permission.")
]

if unexpected_permissions:
    raise SystemExit(f"Unexpected permissions in merged release manifest: {unexpected_permissions}")
if android_permissions:
    raise SystemExit(f"Android system permissions must not be requested: {android_permissions}")

uses_sdk = root.find("uses-sdk")
if uses_sdk is None:
    raise SystemExit("Merged manifest missing uses-sdk")
target_sdk = uses_sdk.attrib.get(android + "targetSdkVersion")
min_sdk = uses_sdk.attrib.get(android + "minSdkVersion")
if target_sdk != "35":
    raise SystemExit(f"Expected targetSdkVersion 35, got {target_sdk}")
if min_sdk != "23":
    raise SystemExit(f"Expected minSdkVersion 23, got {min_sdk}")

application = root.find("application")
if application is None:
    raise SystemExit("Merged manifest missing application node")
allow_backup = application.attrib.get(android + "allowBackup")
if allow_backup != "false":
    raise SystemExit(f"android:allowBackup must remain false, got {allow_backup}")
data_extraction_rules = application.attrib.get(android + "dataExtractionRules")
if data_extraction_rules != "@xml/data_extraction_rules":
    raise SystemExit(f"android:dataExtractionRules must point to @xml/data_extraction_rules, got {data_extraction_rules}")
full_backup_content = application.attrib.get(android + "fullBackupContent")
if full_backup_content != "@xml/backup_rules":
    raise SystemExit(f"android:fullBackupContent must point to @xml/backup_rules, got {full_backup_content}")

print(f"Release manifest package: {package_name}")
print(f"Release manifest minSdk={min_sdk}, targetSdk={target_sdk}")
if uses_permissions:
    print(f"Allowed internal permissions: {', '.join(uses_permissions)}")
else:
    print("No uses-permission entries.")
print("No Android system permissions requested; Auto Backup is disabled and backup/data extraction rules are explicit.")
PY

echo "== Release identity check =="
python3 - <<'PY'
from pathlib import Path
import re
import xml.etree.ElementTree as ET

expected = {
    "application_id": "ru.cisgame.colorquarter",
    "version_code": "1",
    "version_name": "1.0.0",
    "app_name": "Цветной Квартал",
    "main_activity": "ru.cisgame.colorquarter.MainActivity",
    "icon": "@mipmap/ic_launcher",
    "round_icon": "@mipmap/ic_launcher_round",
}

android = "{http://schemas.android.com/apk/res/android}"
manifest_path = Path("app/build/intermediates/merged_manifests/release/processReleaseManifest/AndroidManifest.xml")
strings_path = Path("app/src/main/res/values/strings.xml")
build_config_path = Path("app/build/generated/source/buildConfig/release/ru/cisgame/colorquarter/BuildConfig.java")

for path in (manifest_path, strings_path, build_config_path):
    if not path.exists():
        raise SystemExit(f"Missing release identity input: {path}")

root = ET.parse(manifest_path).getroot()
manifest_package = root.attrib.get("package")
version_code = root.attrib.get(android + "versionCode")
version_name = root.attrib.get(android + "versionName")

if manifest_package != expected["application_id"]:
    raise SystemExit(f"Release package mismatch: expected {expected['application_id']}, got {manifest_package}")
if version_code != expected["version_code"]:
    raise SystemExit(f"Release versionCode mismatch: expected {expected['version_code']}, got {version_code}")
if version_name != expected["version_name"]:
    raise SystemExit(f"Release versionName mismatch: expected {expected['version_name']}, got {version_name}")
if "debug" in f"{manifest_package} {version_name}".lower():
    raise SystemExit("Release manifest identity contains debug marker")

application = root.find("application")
if application is None:
    raise SystemExit("Merged release manifest missing application node")

for attr, expected_value in (
    ("label", "@string/app_name"),
    ("icon", expected["icon"]),
    ("roundIcon", expected["round_icon"]),
):
    actual = application.attrib.get(android + attr)
    if actual != expected_value:
        raise SystemExit(f"Release application android:{attr} mismatch: expected {expected_value}, got {actual}")

strings_root = ET.parse(strings_path).getroot()
app_name = None
for node in strings_root.findall("string"):
    if node.attrib.get("name") == "app_name":
        app_name = "".join(node.itertext())
        break
if app_name != expected["app_name"]:
    raise SystemExit(f"app_name mismatch: expected {expected['app_name']!r}, got {app_name!r}")

build_config = build_config_path.read_text(encoding="utf-8")

def java_value(name):
    match = re.search(rf"public static final .* {name} = (.+);", build_config)
    if not match:
        raise SystemExit(f"BuildConfig missing {name}")
    value = match.group(1).strip()
    if value.startswith('"') and value.endswith('"'):
        return value[1:-1]
    return value

checks = {
    "DEBUG": "false",
    "APPLICATION_ID": expected["application_id"],
    "BUILD_TYPE": "release",
    "VERSION_CODE": expected["version_code"],
    "VERSION_NAME": expected["version_name"],
}
for name, expected_value in checks.items():
    actual = java_value(name)
    if actual != expected_value:
        raise SystemExit(f"Release BuildConfig {name} mismatch: expected {expected_value}, got {actual}")
    if "debug" in actual.lower() and name != "DEBUG":
        raise SystemExit(f"Release BuildConfig {name} contains debug marker: {actual}")

activities = application.findall("activity")
main_activities = []
for activity in activities:
    name = activity.attrib.get(android + "name")
    exported = activity.attrib.get(android + "exported")
    actions = [node.attrib.get(android + "name") for node in activity.findall("intent-filter/action")]
    categories = [node.attrib.get(android + "name") for node in activity.findall("intent-filter/category")]
    if "android.intent.action.MAIN" in actions and "android.intent.category.LAUNCHER" in categories:
        main_activities.append((name, exported))

if main_activities != [(expected["main_activity"], "true")]:
    raise SystemExit(f"Expected exactly one exported launcher activity {expected['main_activity']}, got {main_activities}")

allowed_exported_components = {
    ("activity", expected["main_activity"], None),
    ("receiver", "androidx.profileinstaller.ProfileInstallReceiver", "android.permission.DUMP"),
}
exported_components = []
for tag in ("activity", "service", "receiver", "provider"):
    for node in application.findall(tag):
        if node.attrib.get(android + "exported") == "true":
            exported_components.append(
                (tag, node.attrib.get(android + "name"), node.attrib.get(android + "permission")),
            )

unexpected_exported = [
    item for item in exported_components
    if item not in allowed_exported_components
]
if unexpected_exported:
    raise SystemExit(f"Unexpected exported release components: {unexpected_exported}")

print(f"Release identity: {manifest_package} {version_name} ({version_code}), label={app_name}")
print("Launcher/icon identity is stable; no debug suffix in release BuildConfig.")
print("Exported components are limited to launcher activity and AndroidX profile installer receiver.")
PY

echo "== Placeholder scan =="
if command -v rg >/dev/null 2>&1; then
  if rg -n -i "lorem|todo|fixme|замените|privacy_policy_draft" app/src/main play-assets; then
    echo "Placeholder-like text found in release surfaces" >&2
    exit 1
  fi
else
  if grep -R -n -i -E "lorem|todo|fixme|замените|privacy_policy_draft" app/src/main play-assets; then
    echo "Placeholder-like text found in release surfaces" >&2
    exit 1
  fi
fi
echo "No blocking placeholder-like text found."

echo "== Localization surface check =="
if command -v rg >/dev/null 2>&1; then
  if rg -n "[А-Яа-яЁё]" app/src/main/java/ru/cisgame/colorquarter/ui; then
    echo "Russian UI chrome text must live in string resources, not Compose UI code" >&2
    exit 1
  fi
else
  if grep -R -n -E "[А-Яа-яЁё]" app/src/main/java/ru/cisgame/colorquarter/ui; then
    echo "Russian UI chrome text must live in string resources, not Compose UI code" >&2
    exit 1
  fi
fi
echo "UI chrome Russian text is stored in string resources; authored game content remains in data catalog."

echo "== AAB signing status =="
if command -v jarsigner >/dev/null 2>&1; then
  if jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab 2>&1 | grep -q "jar is unsigned"; then
    echo "AAB is unsigned. This is expected until private keystore.properties is configured."
  else
    echo "AAB appears signed or jarsigner returned a non-standard status."
  fi
else
  echo "jarsigner not found; skipped signing status check."
fi

echo "== AAB content check =="
if unzip -l app/build/outputs/bundle/release/app-release.aab | grep -q "DebugProbesKt.bin"; then
  echo "DebugProbesKt.bin must not be packaged in release AAB" >&2
  exit 1
fi
echo "No Kotlin debug probes packaged in release AAB."

echo "== Performance and size budget check =="
python3 - <<'PY'
from pathlib import Path

budgets = {
    "release_aab": 8 * 1024 * 1024,
    "debug_apk": 25 * 1024 * 1024,
    "in_app_webp_total": 256 * 1024,
    "single_in_app_webp": 96 * 1024,
}

release_aab = Path("app/build/outputs/bundle/release/app-release.aab")
debug_apk = Path("app/build/outputs/apk/debug/app-debug.apk")
for label, path in (
    ("release_aab", release_aab),
    ("debug_apk", debug_apk),
):
    size = path.stat().st_size
    limit = budgets[label]
    if size > limit:
        raise SystemExit(f"{path}: {size} bytes exceeds {label} budget {limit} bytes")
    print(f"{path}: {size} / {limit} bytes")

res_root = Path("app/src/main/res")
forbidden_suffixes = {
    ".aac",
    ".flac",
    ".m4a",
    ".mov",
    ".mp3",
    ".mp4",
    ".ogg",
    ".otf",
    ".ttf",
    ".wav",
    ".webm",
}
forbidden_resources = sorted(
    path
    for path in res_root.rglob("*")
    if path.is_file() and path.suffix.lower() in forbidden_suffixes
)
if forbidden_resources:
    raise SystemExit(
        "Unexpected heavy media/font resources in v1.0 release: "
        + ", ".join(str(path) for path in forbidden_resources)
    )

expected_webp_names = {
    "action_panel_texture.webp",
    "app_background.webp",
    "defeat_illustration.webp",
    "home_illustration.webp",
    "level_illustration.webp",
    "onboarding_illustration.webp",
    "victory_illustration.webp",
}
webp_paths = sorted(res_root.rglob("*.webp"))
actual_webp_names = {path.name for path in webp_paths}
if actual_webp_names != expected_webp_names:
    missing = sorted(expected_webp_names - actual_webp_names)
    extra = sorted(actual_webp_names - expected_webp_names)
    raise SystemExit(
        f"In-app WebP inventory changed; update budget/docs. missing={missing}, extra={extra}"
    )

total_webp_size = 0
for path in webp_paths:
    size = path.stat().st_size
    total_webp_size += size
    if size > budgets["single_in_app_webp"]:
        raise SystemExit(
            f"{path}: {size} bytes exceeds single WebP budget {budgets['single_in_app_webp']} bytes"
        )
    print(f"{path}: {size} / {budgets['single_in_app_webp']} bytes")
if total_webp_size > budgets["in_app_webp_total"]:
    raise SystemExit(
        f"In-app WebP total {total_webp_size} bytes exceeds budget {budgets['in_app_webp_total']} bytes"
    )
print(f"In-app WebP total: {total_webp_size} / {budgets['in_app_webp_total']} bytes")
print("No audio/video/custom font resources in app release resources.")
PY

echo "Release candidate validation completed."
