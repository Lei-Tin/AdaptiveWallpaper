# AdaptiveWallpaper Handoff

Last updated: 2026-08-04

## Current state

- Repository: `Lei-Tin/AdaptiveWallpaper`
- Default branch: `main`
- Latest application commit before these continuity records: `fa195f3`
- Latest public release: `v2.0.0` (published 2026-08-01)
- Package/application ID: `com.shouyihung.adaptivewallpaper`
- Version: `versionCode 4`, `versionName 2.0.0`
- Android SDK: minimum 24, target 36, compile 37.1
- Release signing identity: `CN=Ray Hung`
- Public certificate SHA-256: `4074b19aedde4215c747eb33ba53a05b42d2fb3d939862c01a5515809e9a32e8`
- Primary website: `https://aw.shouyihung.com`
- Alternate website: `https://adaptivewallpaper.shouyihung.com`

The latest known `main` Android CI and Cloudflare Pages checks were successful after the AndroidX dependency updates and compile SDK 37.1 migration.

## Product behavior

AdaptiveWallpaper is an Android live-wallpaper app that stores one user-selected image for light mode and one for dark mode. It switches the rendered wallpaper when Android changes theme.

Implemented behavior:

- Local image selection for light and dark slots.
- A wallpaper editor with crop/fill, fit, and stretch modes.
- Drag and pinch-to-zoom positioning in crop mode.
- Large-image loading through sampled bitmap decoding and normalized storage.
- Application through Android's live-wallpaper preview to home screen, lock screen, or both where the device supports it.
- Theme callbacks plus a five-second visible-engine polling fallback for OEMs such as some HyperOS devices.
- Chinese and English UI.
- Imported images stored in `noBackupFilesDir`; no network permission, analytics, account, ads, or cloud sync in the Android app.
- An in-app disable action that clears positions currently controlled by this service back to Android's system default wallpaper and stops this wallpaper service from remaining active.

Important Android limitation: a live wallpaper cannot act as a transparent stack over an arbitrary static wallpaper, and a normal third-party app cannot reliably read and restore every prior system/lock-screen wallpaper across Android versions and OEMs. Earlier restore experiments were intentionally replaced by the predictable “clear to system default and stop” behavior.

Closing or swiping away the activity does not stop an active wallpaper because Android owns the wallpaper service lifecycle. Users must use the in-app disable action or choose another wallpaper.

## Important source files

- `app/src/main/java/com/shouyihung/adaptivewallpaper/MainActivity.kt`: slot selection, live-wallpaper preview, active target detection, and disable flow.
- `app/src/main/java/com/shouyihung/adaptivewallpaper/AdaptiveWallpaperService.kt`: wallpaper engine, rendering lifecycle, theme detection, and five-second fallback.
- `app/src/main/java/com/shouyihung/adaptivewallpaper/WallpaperEditorActivity.kt`: crop/fit/stretch editing flow.
- `app/src/main/java/com/shouyihung/adaptivewallpaper/WallpaperPreviewView.kt`: editor gestures and preview.
- `app/src/main/java/com/shouyihung/adaptivewallpaper/BitmapLoader.kt`: sampled image decoding and image normalization.
- `app/src/main/java/com/shouyihung/adaptivewallpaper/WallpaperStore.kt`: private local images and settings.
- `app/src/main/java/com/shouyihung/adaptivewallpaper/WallpaperRenderer.kt`: scale-mode transforms and drawing.
- `app/build.gradle.kts`: package, SDK levels, version, and local release-signing configuration.

## Build and verification

Standard local verification:

```bash
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Signed release verification and build:

```bash
./gradlew --no-daemon testDebugUnitTest lintRelease assembleRelease
```

Signed release APK:

```text
app/build/outputs/apk/release/app-release.apk
```

`keystore.properties` and the keystore are local, ignored secrets. Future releases must use the same keystore and increment `versionCode`.

## CI and release flow

`.github/workflows/android-ci.yml` runs on pushes to `main` and pull requests. It performs unit tests, Debug lint, and `assembleDebug`. It does not retain the Debug APK, sign a release APK, create a GitHub Release, or upload release assets.

The current release flow is manual:

1. Increment `versionCode` and the intended `versionName`.
2. Build and verify the signed release locally with the existing keystore.
3. Rename/copy the release asset to the stable name `AdaptiveWallpaper.apk`.
4. Create the GitHub Release and attach `AdaptiveWallpaper.apk` before publishing it. A versioned filename and checksum may also be attached.
5. Publish the release.

`.github/workflows/refresh-cloudflare-pages.yml` reacts to a published GitHub Release. It updates `site/release-version.txt` on `main`, which triggers a Cloudflare Pages rebuild.

## Website and APK hosting

Cloudflare R2, KV, and D1 are not used for APK storage.

`site/build.sh` downloads this stable GitHub Release asset during every Pages build:

```text
https://github.com/Lei-Tin/AdaptiveWallpaper/releases/latest/download/AdaptiveWallpaper.apk
```

It creates the ignored build output:

```text
site/dist/download/AdaptiveWallpaper.apk
site/dist/download/AdaptiveWallpaper.apk.sha256
```

Cloudflare Pages then publishes `site/dist` as immutable deployment assets/CDN content. Pages has no user-facing file browser for these assets; the source of truth remains the GitHub Release.

Current public download path:

```text
https://aw.shouyihung.com/download/AdaptiveWallpaper.apk
```

## Compatibility and migration notes

- `v1.x` used `io.github.leitin.adaptivewallpaper`.
- `v2.0.0` changed to `com.shouyihung.adaptivewallpaper`; Android treats it as a separate app, so it cannot update v1.x in place.
- `v2.x` releases must preserve the current package and signing key to remain installable as updates.
- Device vendors differ in lock-screen live-wallpaper support and theme callback reliability. Keep the fallback lightweight and verify behavior on Pixel/AOSP and affected OEM devices when changing service lifecycle code.

## Local-only items currently present

At the time of this handoff, `Test Pic.jpg` and `social/` were untracked local items and were deliberately not included in the agent-record commit. `Test Pic.jpg` is a user test image. `social/` contains local Xiaohongshu publishing assets. Do not delete or stage them without an explicit request.

## Current follow-up context

The owner recently confirmed that release APK signing and GitHub Release uploads are still manual. A future task may automate signed releases, but that has not been authorized or implemented. Such automation would require carefully scoped GitHub Actions secrets for the keystore and passwords and should preserve the stable `AdaptiveWallpaper.apk` asset name.
