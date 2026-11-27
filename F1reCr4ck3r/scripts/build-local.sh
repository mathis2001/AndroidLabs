#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ -z "${JAVA_HOME:-}" && -d /usr/lib/jvm/java-17-openjdk-amd64 ]]; then
  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
fi

if command -v gradle >/dev/null 2>&1; then
  GRADLE_BIN="$(command -v gradle)"
else
  GRADLE_BIN="$(find "$HOME/.gradle/wrapper/dists/gradle-8.11.1-bin" -path '*/bin/gradle' -type f -print -quit 2>/dev/null || true)"
fi

if [[ -z "$GRADLE_BIN" ]]; then
  echo "Gradle 8.11.1 is required; build the app in Android Studio or install Gradle." >&2
  exit 1
fi

"$GRADLE_BIN" -p "$PROJECT_DIR" --no-daemon assembleDebug
echo "$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
