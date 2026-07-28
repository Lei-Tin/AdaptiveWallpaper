# AdaptiveWallpaper

一款轻量的 Android 动态壁纸应用，可根据系统浅色/深色模式自动切换壁纸。

## 功能

- 分别选择浅色模式与深色模式图片
- 支持裁剪填充、完整显示和拉伸三种适配方式
- 裁剪模式支持拖动定位与双指缩放
- 可应用到主屏幕、锁屏或两者
- 清晰显示当前模式与应用范围
- 可在 App 内停用服务并恢复 Android 系统默认壁纸
- 所选图片保存在 App 私有目录，不会上传到网络

## 系统要求

- Android 7.0（API 24）或更高版本

## 安装

从 [GitHub Releases](https://github.com/Lei-Tin/AdaptiveWallpaper/releases/latest) 下载最新 APK。在 Android 提示时，允许用于下载 APK 的浏览器或文件管理器“安装未知应用”。

## 使用方法

1. 点击浅色卡片的“选择图片”，调整显示方式并保存。
2. 点击深色卡片的“选择图片”，调整显示方式并保存。
3. 点击“启用自适应壁纸”。
4. 在 Android 系统预览中点击“设置壁纸”，选择主屏幕、锁屏或两者。
5. 切换系统浅色/深色模式，壁纸会自动切换。

关闭 App 或将其从最近任务中划掉不会停止动态壁纸，因为壁纸服务由 Android 系统管理。如需停用，请重新打开 App，点击“停用自适应壁纸”；当前由本 App 控制的位置会恢复为系统默认壁纸。

## 本地构建

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Debug APK 将生成在：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 构建签名 Release APK

Android 要求 APK 必须经过签名。Release keystore 是后续版本更新的永久身份凭证，请离线备份 keystore 和密码，且不要提交到 Git。

1. 使用 Android Studio 的 `Build > Generate Signed Bundle / APK` 创建或选择一个 keystore；也可以使用 JDK 的 `keytool` 创建。
2. 将 `keystore.properties.example` 复制为 `keystore.properties`，填写 keystore 的绝对路径、密码和 alias。
3. 构建：

   ```bash
   ./gradlew testDebugUnitTest lintRelease assembleRelease
   ```

4. 签名 APK 将生成在：

   ```text
   app/build/outputs/apk/release/app-release.apk
   ```

每次发布更新时必须继续使用同一份 keystore，并递增 `versionCode`。

## 隐私

AdaptiveWallpaper 不需要网络权限。用户选择的图片及裁剪设置仅保存在设备上的 App 私有存储中。

## 开发状态

项目仍处于早期阶段，欢迎通过 GitHub Issues 提交问题或建议。
