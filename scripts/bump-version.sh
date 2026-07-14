#!/usr/bin/env bash
# Single-source version bump (AR-9/AN-12).
#
# Usage: scripts/bump-version.sh <versionName> <versionCode>
#   e.g. scripts/bump-version.sh 1.1 2
#
# Updates the canonical values in gradle.properties (consumed by
# androidApp/build.gradle.kts) and mirrors them into both iOS targets in
# iosApp/project.yml (app + widget extension). Re-run
# `xcodegen generate && pod install` in iosApp/ afterwards to regenerate the
# Xcode project.
set -euo pipefail

if [ $# -ne 2 ]; then
  echo "usage: $0 <versionName> <versionCode>" >&2
  exit 1
fi

NAME="$1"
CODE="$2"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

sed -i '' \
  -e "s/^appVersionName=.*/appVersionName=${NAME}/" \
  -e "s/^appVersionCode=.*/appVersionCode=${CODE}/" \
  "$ROOT/gradle.properties"

sed -i '' \
  -e "s/CURRENT_PROJECT_VERSION: \".*\"/CURRENT_PROJECT_VERSION: \"${CODE}\"/" \
  -e "s/MARKETING_VERSION: \".*\"/MARKETING_VERSION: \"${NAME}\"/" \
  "$ROOT/iosApp/project.yml"

echo "Version set to ${NAME} (${CODE}) in gradle.properties and iosApp/project.yml."
echo "Now run: cd iosApp && xcodegen generate && pod install"
