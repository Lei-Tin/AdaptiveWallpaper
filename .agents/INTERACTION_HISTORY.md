# Interaction and Decision History

This is a sanitized, decision-focused project history for future Codex sessions. It intentionally does not contain a verbatim transcript, credentials, private keys, passwords, auth tokens, or private correspondence.

## 2026-07-27 — Project bootstrap and first implementation

- Created and validated the Android Studio project and base build.
- Initialized Git with `main` as the default branch and connected `Lei-Tin/AdaptiveWallpaper` as the GitHub repository.
- Implemented the initial live-wallpaper service that chooses light/dark wallpaper content based on Android system theme.
- Established the Android Studio emulator and device installation/testing workflow.
- Added user-selected local images rather than fixed bundled wallpapers.
- Added the wallpaper editor with crop/fill, fit, stretch, drag, and pinch-to-zoom behavior.
- Added home/lock-screen target awareness and investigated OEM/device differences.

## 2026-07-28 — Disable semantics, compatibility, and initial releases

- Explored restoring the exact wallpaper that existed before activation. Android security and OEM APIs do not provide a reliable general solution, especially when a live wallpaper is already active or home and lock states differ.
- Rejected the conceptual “transparent live-wallpaper stack over an existing static wallpaper” model because Android live wallpapers replace the wallpaper provider rather than layering over it.
- Settled on deterministic disable behavior: clear positions controlled by AdaptiveWallpaper back to Android's system default and ensure this service is no longer the active provider.
- Added a compatibility fallback for devices that do not reliably deliver theme configuration callbacks. The final interval is five seconds while the wallpaper engine is visible.
- Published `v1.0.0`, `v1.0.1` (icon and README screenshots), and `v1.0.2` (theme-switch compatibility).
- Added signing and GitHub Release guidance and kept the release keystore outside Git.

## 2026-08-01 — Open-source hardening and v2.0.0

- Migrated the application ID and namespace from `io.github.leitin.adaptivewallpaper` to `com.shouyihung.adaptivewallpaper` while retaining the existing `CN=Ray Hung` signing certificate.
- Accepted that Android treats v2 as a new app relative to v1 because the package name changed; documented the migration process.
- Moved imported images into no-backup private storage.
- Added English as the default UI while preserving Chinese localization.
- Added Apache License 2.0, English documentation, contribution/security guidance, issue templates, GitHub Actions CI, and Dependabot.
- Published signed `v2.0.0` with a stable `AdaptiveWallpaper.apk` Release asset for downstream website downloads.
- Added the Cloudflare Pages website and custom domains `adaptivewallpaper.shouyihung.com` and `aw.shouyihung.com`.
- Added `site/build.sh`. It is a project-authored build script, not a Cloudflare-generated file. It copies explicit website assets, downloads the latest stable GitHub Release APK, calculates its SHA-256, and produces `site/dist`.
- Clarified that APK source storage is GitHub Releases; Cloudflare Pages only republishes the APK inside its deployment assets. No R2 bucket is involved.

## 2026-08-02 — Dependency/CI repair and publishing assets

- Diagnosed failing Dependabot checks: stale PR branches lacked the newer `site/` directory, causing Cloudflare Pages to report a missing working directory. Updating the PR bases fixed Pages previews.
- Upgraded compile SDK from 36.1 to 37.1 while keeping target SDK at 36 so AndroidX Core 1.19.0 could compile without opting into a new target-SDK behavior set.
- Updated and merged Dependabot PRs for Material, AndroidX JUnit, Espresso, Activity, Core KTX, `actions/checkout`, and `actions/setup-java`.
- Verified the final combined state locally with unit tests, lint, and Debug APK assembly, then confirmed both GitHub Android CI and Cloudflare Pages were green.
- Created Xiaohongshu publishing assets. After rejecting AI-styled marketing graphics and a webpage screenshot, the final fifth image became a simple short-domain card showing `aw.shouyihung.com`. These assets remain local under untracked `social/` unless explicitly requested otherwise.

## 2026-08-04 — Cross-device agent continuity

- The owner requested that project context and interaction decisions be committed to GitHub so a Codex session on another device can resume without reconstructing the full chat.
- Added root `AGENTS.md`, `.agents/HANDOFF.md`, and this sanitized interaction history.
- Established the ongoing rule that meaningful project decisions and completed work should update the handoff/history in the same GitHub change.
- Kept agent Markdown out of the Cloudflare website build. Because the GitHub repository itself is public, records must remain free of secrets and private transcript content.
