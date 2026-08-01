#!/usr/bin/env bash
set -euo pipefail

SITE_DIR="$(cd "$(dirname "$0")" && pwd)"
OUTPUT_DIR="$SITE_DIR/dist"
DOWNLOAD_URL="https://github.com/Lei-Tin/AdaptiveWallpaper/releases/latest/download/AdaptiveWallpaper.apk"

mkdir -p "$OUTPUT_DIR/assets" "$OUTPUT_DIR/download" "$OUTPUT_DIR/privacy"

cp "$SITE_DIR/index.html" "$SITE_DIR/styles.css" "$SITE_DIR/app.js" "$SITE_DIR/_headers" "$OUTPUT_DIR/"
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

printf '%s  %s\n' "$APK_SHA256" "AdaptiveWallpaper.apk" > "$OUTPUT_DIR/download/AdaptiveWallpaper.apk.sha256"
printf 'Built Cloudflare Pages site with APK SHA-256 %s\n' "$APK_SHA256"
