# Contributing to AdaptiveWallpaper

感谢你帮助改进 AdaptiveWallpaper。Issues and pull requests in Chinese or English are welcome.

## Before opening an issue

- Search existing issues first.
- For bugs, include the device model, Android version, system/ROM version, whether the problem affects the home screen or lock screen, and exact reproduction steps.
- Do not post private images, signing keys, passwords, or other secrets.
- Report security issues privately as described in [SECURITY.md](SECURITY.md).

## Development setup

1. Install the latest stable Android Studio and Android SDK 36.1.
2. Clone the repository and let Android Studio finish Gradle sync.
3. Run the checks used by CI:

   ```bash
   ./gradlew testDebugUnitTest lintDebug assembleDebug
   ```

4. Test wallpaper activation, light/dark switching, image cropping, and disabling the wallpaper on an emulator or physical device.

## Pull requests

- Keep each pull request focused on one change.
- Explain the user impact and how the change was tested.
- Add or update tests for behavior changes where practical.
- Do not commit `.idea`, `local.properties`, `keystore.properties`, keystores, APKs, or personal test images.
- Keep user-facing strings in Android resources and update both English and Chinese translations.

By contributing, you agree that your contribution is licensed under the repository's [Apache License 2.0](LICENSE).
