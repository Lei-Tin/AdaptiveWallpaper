# Changelog

All notable changes to AdaptiveWallpaper are documented here.

## [2.0.0] - 2026-08-01

### Changed

- Change the application ID and namespace to `com.shouyihung.adaptivewallpaper`.
- Keep the existing `CN=Ray Hung` release signing certificate.
- Store imported wallpapers outside Android cloud backup storage.
- Add English as the default UI language and retain Chinese localization.
- Use official Google, Maven Central, and Gradle Plugin Portal repositories.

### Added

- Apache License 2.0.
- English documentation, contribution and security policies, and issue templates.
- GitHub Actions CI and Dependabot configuration.
- A stable `AdaptiveWallpaper.apk` download name for GitHub Releases.
- An OEM compatibility fallback that checks the system theme every five seconds while visible.

### Migration

Android treats v2.0.0 as a separate app from v1.x because the application ID changed. Existing users must select their wallpapers again and can then disable and uninstall the old app.

## [1.0.2] - 2026-07-28

- Improve theme switching compatibility on devices that do not reliably deliver wallpaper configuration callbacks.

## [1.0.1] - 2026-07-28

- Add the production app icon and README screenshots.

## [1.0.0] - 2026-07-28

- Initial public release.
