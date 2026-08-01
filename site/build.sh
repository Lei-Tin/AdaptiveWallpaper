#!/usr/bin/env bash
set -euo pipefail

SITE_DIR="$(cd "$(dirname "$0")" && pwd)"
OUTPUT_DIR="$SITE_DIR/dist"
DOWNLOAD_URL="https://github.com/Lei-Tin/AdaptiveWallpaper/releases/latest/download/AdaptiveWallpaper.apk"
APP_VERSION="$(tr -d '[:space:]' < "$SITE_DIR/release-version.txt")"

if [[ ! "$APP_VERSION" =~ ^[0-9A-Za-z.-]+$ ]]; then
  printf 'Invalid release version: %s\n' "$APP_VERSION" >&2
  exit 1
fi

mkdir -p "$OUTPUT_DIR/assets" "$OUTPUT_DIR/download" "$OUTPUT_DIR/privacy"

cp "$SITE_DIR/styles.css" "$SITE_DIR/app.js" "$SITE_DIR/_headers" "$OUTPUT_DIR/"
cp "$SITE_DIR/privacy/index.html" "$OUTPUT_DIR/privacy/index.html"
cp "$SITE_DIR/../docs/images/app-icon.png" "$OUTPUT_DIR/assets/app-icon.png"
cp "$SITE_DIR/../docs/images/app-main.png" "$OUTPUT_DIR/assets/app-main.png"
cp "$SITE_DIR/../docs/images/home-light.png" "$OUTPUT_DIR/assets/home-light.png"
cp "$SITE_DIR/../docs/images/home-dark.png" "$OUTPUT_DIR/assets/home-dark.png"

curl --fail --location --silent --show-error --retry 3 \
  "$DOWNLOAD_URL" \
  --output "$OUTPUT_DIR/download/AdaptiveWallpaper.apk"

if command -v sha256sum >/dev/null 2>&1; then
  APK_SHA256="$(sha256sum "$OUTPUT_DIR/download/AdaptiveWallpaper.apk" | awk '{print $1}')"
else
  APK_SHA256="$(shasum -a 256 "$OUTPUT_DIR/download/AdaptiveWallpaper.apk" | awk '{print $1}')"
fi

APK_BYTES="$(wc -c < "$OUTPUT_DIR/download/AdaptiveWallpaper.apk" | tr -d '[:space:]')"
APK_SIZE="$(awk -v bytes="$APK_BYTES" 'BEGIN { printf "%.1f MB", bytes / 1024 / 1024 }')"

sed \
  -e "s/__APP_VERSION__/$APP_VERSION/g" \
  -e "s/__APK_SIZE__/$APK_SIZE/g" \
  "$SITE_DIR/index.html" > "$OUTPUT_DIR/index.html"

printf '%s  %s\n' "$APK_SHA256" "AdaptiveWallpaper.apk" > "$OUTPUT_DIR/download/AdaptiveWallpaper.apk.sha256"
printf 'Built Cloudflare Pages site for v%s with APK SHA-256 %s\n' "$APP_VERSION" "$APK_SHA256"
