#!/usr/bin/env bash
# Single-source version bump.
#
# Usage: scripts/bump-version.sh <versionName> <versionCode>
#   e.g. scripts/bump-version.sh 1.1 2
#
# versionName: one to three dot-separated integers (1.1, 2.0.3) — the format
#   iOS accepts for CFBundleShortVersionString.
# versionCode: a positive integer (Android versionCode, iOS CFBundleVersion).
#
# Updates the canonical values in gradle.properties (consumed by
# androidApp/build.gradle.kts) and mirrors them into both iOS targets (app +
# widget extension) in iosApp/project.yml and the committed
# CocktailCraft.xcodeproj, so no `xcodegen generate` is needed afterwards.
# Both Info.plists read the version from those build settings.
#
# Works with BSD (macOS) and GNU sed. Fails without touching anything if an
# argument is malformed, and fails if any file did not end up with the new
# version.
set -euo pipefail

die() {
  echo "error: $*" >&2
  exit 1
}

[ $# -eq 2 ] || { echo "usage: $0 <versionName> <versionCode>" >&2; exit 1; }

NAME="$1"
CODE="$2"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROPS="$ROOT/gradle.properties"
SPEC="$ROOT/iosApp/project.yml"
PBXPROJ="$ROOT/iosApp/CocktailCraft.xcodeproj/project.pbxproj"

[[ "$NAME" =~ ^[0-9]+(\.[0-9]+){0,2}$ ]] ||
  die "versionName '$NAME' must be 1-3 dot-separated integers, e.g. 1.2 or 1.2.3"
[[ "$CODE" =~ ^[1-9][0-9]{0,9}$ ]] && [ "$CODE" -le 2100000000 ] ||
  die "versionCode '$CODE' must be an integer between 1 and 2100000000"

for f in "$PROPS" "$SPEC" "$PBXPROJ"; do
  [ -f "$f" ] || die "missing $f"
done

# count <regex> <file>: number of lines matching an extended regex
count() {
  grep -cE "$1" "$2" || true
}

# Each file must have the expected number of version lines before we edit it,
# so a renamed key fails here instead of being silently skipped.
expect() {
  local regex="$1" file="$2" want="$3" got
  got="$(count "$regex" "$file")"
  [ "$got" -eq "$want" ] ||
    die "expected $want line(s) matching '$regex' in ${file#"$ROOT"/}, found $got"
}

PBX_TARGET_CONFIGS="$(count '^[[:space:]]*MARKETING_VERSION = ' "$PBXPROJ")"
[ "$PBX_TARGET_CONFIGS" -gt 0 ] || die "no MARKETING_VERSION in ${PBXPROJ#"$ROOT"/}"

expect '^appVersionName=' "$PROPS" 1
expect '^appVersionCode=' "$PROPS" 1
expect '^[[:space:]]*MARKETING_VERSION: "' "$SPEC" 2
expect '^[[:space:]]*CURRENT_PROJECT_VERSION: "' "$SPEC" 2
expect '^[[:space:]]*CURRENT_PROJECT_VERSION = ' "$PBXPROJ" "$PBX_TARGET_CONFIGS"

# Portable in-place edit: sed to a temp file, then replace the original.
edit() {
  local file="$1" tmp
  shift
  tmp="$(mktemp "${file}.XXXXXX")"
  sed -E "$@" "$file" >"$tmp" || { rm -f "$tmp"; die "sed failed on $file"; }
  cat "$tmp" >"$file"
  rm -f "$tmp"
}

edit "$PROPS" \
  -e "s/^appVersionName=.*/appVersionName=${NAME}/" \
  -e "s/^appVersionCode=.*/appVersionCode=${CODE}/"

edit "$SPEC" \
  -e "s/^([[:space:]]*)MARKETING_VERSION: \".*\"/\1MARKETING_VERSION: \"${NAME}\"/" \
  -e "s/^([[:space:]]*)CURRENT_PROJECT_VERSION: \".*\"/\1CURRENT_PROJECT_VERSION: \"${CODE}\"/"

edit "$PBXPROJ" \
  -e "s/^([[:space:]]*)MARKETING_VERSION = [^;]*;/\1MARKETING_VERSION = ${NAME};/" \
  -e "s/^([[:space:]]*)CURRENT_PROJECT_VERSION = [^;]*;/\1CURRENT_PROJECT_VERSION = ${CODE};/"

# Verify every edit landed.
NAME_RE="${NAME//./\\.}"
expect "^appVersionName=${NAME_RE}\$" "$PROPS" 1
expect "^appVersionCode=${CODE}\$" "$PROPS" 1
expect "^[[:space:]]*MARKETING_VERSION: \"${NAME_RE}\"\$" "$SPEC" 2
expect "^[[:space:]]*CURRENT_PROJECT_VERSION: \"${CODE}\"\$" "$SPEC" 2
expect "^[[:space:]]*MARKETING_VERSION = ${NAME_RE};\$" "$PBXPROJ" "$PBX_TARGET_CONFIGS"
expect "^[[:space:]]*CURRENT_PROJECT_VERSION = ${CODE};\$" "$PBXPROJ" "$PBX_TARGET_CONFIGS"

echo "Version set to ${NAME} (${CODE}) in gradle.properties, iosApp/project.yml and the Xcode project."
