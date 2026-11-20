#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_DIR="${ANDROID_SDK_ROOT:-/home/s1rn3tz-admin/Android/Sdk}"
TOOLS_DIR="$SDK_DIR/build-tools/35.0.0"
ANDROID_JAR="$SDK_DIR/platforms/android-34/android.jar"
WORK_DIR="$PROJECT_DIR/app/build/manual"
OUTPUT_DIR="$PROJECT_DIR/app/build/outputs/apk/debug"

for tool in "$TOOLS_DIR/aapt2" "$TOOLS_DIR/d8" "$TOOLS_DIR/zipalign" "$TOOLS_DIR/apksigner" "$ANDROID_JAR"; do
  if [[ ! -e "$tool" ]]; then
    echo "Missing Android SDK component: $tool" >&2
    exit 1
  fi
done

rm -rf "$WORK_DIR/classes" "$WORK_DIR/dex" "$WORK_DIR/res"
mkdir -p "$WORK_DIR/classes" "$WORK_DIR/dex" "$OUTPUT_DIR"
sed 's/<manifest /<manifest package="org.northstar.banklab" /' \
  "$PROJECT_DIR/app/src/main/AndroidManifest.xml" > "$WORK_DIR/AndroidManifest.xml"

find "$PROJECT_DIR/app/src/main/java" -name '*.java' -print0 | \
  xargs -0 javac --release 8 -cp "$ANDROID_JAR" -d "$WORK_DIR/classes"
jar cf "$WORK_DIR/classes.jar" -C "$WORK_DIR/classes" .
"$TOOLS_DIR/d8" --min-api 26 --lib "$ANDROID_JAR" --output "$WORK_DIR/dex" "$WORK_DIR/classes.jar"
mkdir -p "$WORK_DIR/res"
"$TOOLS_DIR/aapt2" compile --dir "$PROJECT_DIR/app/src/main/res" -o "$WORK_DIR/res"
RESOURCE_ARGS=()
while IFS= read -r -d '' resource; do
  RESOURCE_ARGS+=("$resource")
done < <(find "$WORK_DIR/res" -name '*.flat' -print0)
"$TOOLS_DIR/aapt2" link \
  -o "$WORK_DIR/unsigned.apk" \
  --manifest "$WORK_DIR/AndroidManifest.xml" \
  --min-sdk-version 26 \
  --target-sdk-version 34 \
  --version-code 2 \
  --version-name 1.1 \
  -I "$ANDROID_JAR" \
  -A "$PROJECT_DIR/app/src/main/assets" \
  "${RESOURCE_ARGS[@]}"
zip -q -j "$WORK_DIR/unsigned.apk" "$WORK_DIR/dex/classes.dex"
"$TOOLS_DIR/zipalign" -f 4 "$WORK_DIR/unsigned.apk" "$WORK_DIR/aligned.apk"

KEYSTORE="$WORK_DIR/debug.keystore"
if [[ ! -e "$KEYSTORE" ]]; then
  keytool -genkeypair -noprompt -keystore "$KEYSTORE" -storepass android \
    -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Northstar Lab,C=FR" \
    -keyalg RSA -keysize 2048 -validity 3650 >/dev/null 2>&1
fi
"$TOOLS_DIR/apksigner" sign --ks "$KEYSTORE" --ks-pass pass:android \
  --key-pass pass:android --out "$OUTPUT_DIR/app-debug.apk" "$WORK_DIR/aligned.apk"
"$TOOLS_DIR/apksigner" verify "$OUTPUT_DIR/app-debug.apk"
echo "$OUTPUT_DIR/app-debug.apk"
