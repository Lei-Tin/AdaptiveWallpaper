# AdaptiveWallpaper

**中文** · [English](README_EN.md)

<p align="center">
  <img src="docs/images/app-icon.png" alt="AdaptiveWallpaper 图标" width="144" />
</p>

一款轻量、开源的 Android 动态壁纸应用，可根据系统浅色/深色模式自动切换用户选择的壁纸。

<p align="center">
  <a href="https://adaptivewallpaper.shouyihung.com/download/AdaptiveWallpaper.apk"><strong>下载最新版 APK</strong></a>
  ·
  <a href="https://adaptivewallpaper.shouyihung.com">访问官网</a>
  ·
  <a href="https://github.com/Lei-Tin/AdaptiveWallpaper/releases/latest">GitHub 镜像</a>
</p>

## 演示

<p align="center">
  <img src="docs/images/app-main.png" alt="AdaptiveWallpaper 设置界面" width="30%" />
  <img src="docs/images/home-light.png" alt="浅色模式桌面壁纸" width="30%" />
  <img src="docs/images/home-dark.png" alt="深色模式桌面壁纸" width="30%" />
</p>

<p align="center"><sub>设置界面 · 浅色模式 · 深色模式</sub></p>

## 功能

- 分别选择浅色模式与深色模式图片
- 支持裁剪填充、完整显示和拉伸三种适配方式
- 裁剪模式支持拖动定位与双指缩放
- 可应用到主屏幕、锁屏或两者
- 兼容部分不会可靠发送主题配置回调的系统，包括部分 HyperOS 设备
- 可在 App 内停用服务并恢复 Android 系统默认壁纸
- 无网络权限；图片保存在 App 私有且禁止备份的目录中
- 中文和英文界面

## 系统要求

- Android 7.0（API 24）或更高版本
- 设备必须支持 Android 动态壁纸

## 安装

1. [从 Cloudflare 下载最新版 APK](https://adaptivewallpaper.shouyihung.com/download/AdaptiveWallpaper.apk)。
2. 打开下载完成的 `AdaptiveWallpaper.apk`。
3. 如果 Android 提示，请允许当前浏览器或文件管理器“安装未知应用”。
4. 安装更新时直接覆盖旧版本即可；请勿先卸载，否则 App 内保存的图片和设置会被删除。

APK 安装版本不会自动更新。新版本发布后，可再次下载最新 APK 覆盖安装。

### 从 v1.x 迁移

v2.0.0 起，应用包名从 `io.github.leitin.adaptivewallpaper` 调整为 `com.shouyihung.adaptivewallpaper`，Android 会将它视为一个新 App，无法覆盖安装 v1.x。请安装新版本、重新选择两张壁纸并启用，然后在旧 App 中停用壁纸并卸载旧 App。之后的 v2.x 更新可以直接覆盖安装。

## 使用方法

1. 点击浅色卡片的“选择图片”，调整显示方式并保存。
2. 点击深色卡片的“选择图片”，调整显示方式并保存。
3. 点击“启用自适应壁纸”。
4. 在 Android 系统预览中点击“设置壁纸”，选择主屏幕、锁屏或两者。
5. 切换系统浅色/深色模式，壁纸会自动切换。

关闭 App 或将其从最近任务中划掉不会停止动态壁纸，因为壁纸服务由 Android 系统管理。如需停用，请重新打开 App，点击“停用自适应壁纸”；当前由本 App 控制的位置会恢复为系统默认壁纸。

## 隐私

AdaptiveWallpaper 不申请网络权限，不包含分析或广告 SDK。用户导入的图片保存在 `noBackupFilesDir`，不会进入 Android 云备份；裁剪设置也仅保存在 App 私有存储中。

## 验证下载

官网提供 [SHA-256 校验文件](https://adaptivewallpaper.shouyihung.com/download/AdaptiveWallpaper.apk.sha256)，每个 GitHub Release 也保留对应校验文件。正式 APK 使用以下证书签名：

```text
Package: com.shouyihung.adaptivewallpaper
Certificate: CN=Ray Hung
SHA-256: 4074b19aedde4215c747eb33ba53a05b42d2fb3d939862c01a5515809e9a32e8
```

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
3. 执行：

   ```bash
   ./gradlew testDebugUnitTest lintRelease assembleRelease
   ```

4. 签名 APK 将生成在 `app/build/outputs/apk/release/app-release.apk`。

每次发布更新时必须继续使用同一份 keystore，并递增 `versionCode`。

## 参与贡献

欢迎提交 Issue 和 Pull Request。开始之前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)；安全问题请按照 [SECURITY.md](SECURITY.md) 私下报告。

## 许可证

本项目使用 [Apache License 2.0](LICENSE)。
