#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLS="$ROOT/tools"
AAPT2="$TOOLS/android-14/aapt2"
D8="$TOOLS/android-14/d8"
ZIPALIGN="$TOOLS/android-14/zipalign"
APKSIGNER="$TOOLS/android-14/apksigner"
ANDROID_JAR="$TOOLS/android-34/android.jar"

BUILD="$ROOT/build"
rm -rf "$BUILD"
mkdir -p "$BUILD/gen" "$BUILD/obj" "$BUILD/dex" "$BUILD/apk"

echo "[1/5] Linking resources & assets with aapt2..."
"$AAPT2" link \
  -I "$ANDROID_JAR" \
  --manifest "$ROOT/app/src/main/AndroidManifest.xml" \
  -A "$ROOT/app/src/main/assets" \
  --java "$BUILD/gen" \
  -o "$BUILD/apk/unsigned.unaligned.apk"

echo "[2/5] Compiling Java sources with javac..."
javac -g:none -parameters -source 8 -target 8 \
  -bootclasspath "$ANDROID_JAR" \
  -d "$BUILD/obj" \
  $(find "$ROOT/app/src/main/java" "$BUILD/gen" -name "*.java")

echo "[3/5] Converting bytecode to DEX with d8..."
"$D8" \
  --lib "$ANDROID_JAR" \
  --min-api 21 \
  --output "$BUILD/dex" \
  $(find "$BUILD/obj" -name "*.class")

echo "[4/5] Packaging classes.dex & zipaligning..."
cd "$BUILD/dex"
zip -q "$BUILD/apk/unsigned.unaligned.apk" classes.dex
"$ZIPALIGN" -f -p 4 "$BUILD/apk/unsigned.unaligned.apk" "$BUILD/apk/unsigned.apk"

echo "[5/5] Signing APK..."
KEYSTORE="${KEYSTORE_PATH:-$HOME/.android/debug.keystore}"
KS_ALIAS="${KEYSTORE_ALIAS:-androiddebugkey}"
KS_PASS="${KEYSTORE_PASS:-android}"

if [ ! -f "$KEYSTORE" ]; then
  mkdir -p "$(dirname "$KEYSTORE")"
  keytool -genkeypair \
    -keystore "$KEYSTORE" \
    -alias "$KS_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storepass "$KS_PASS" \
    -keypass "$KS_PASS" \
    -dname "CN=Android Debug,O=Android,C=US"
fi

"$APKSIGNER" sign \
  --ks "$KEYSTORE" \
  --ks-key-alias "$KS_ALIAS" \
  --ks-pass "pass:$KS_PASS" \
  --key-pass "pass:$KS_PASS" \
  --out "$ROOT/omarchy-tv-launcher.apk" \
  "$BUILD/apk/unsigned.apk"

rm -f "$ROOT/omarchy-tv-launcher.apk.idsig"
ls -lh "$ROOT/omarchy-tv-launcher.apk"
echo "BUILD SUCCESS!"
