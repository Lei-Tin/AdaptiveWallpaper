package io.github.leitin.adaptivewallpaper

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.system.Os
import androidx.core.content.edit
import java.io.File
import java.io.IOException

class WallpaperStore(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun imageFile(slot: WallpaperSlot): File =
        File(appContext.filesDir, "wallpaper_${slot.storageName}.image")

    fun hasImage(slot: WallpaperSlot): Boolean = imageFile(slot).isFile

    fun settings(slot: WallpaperSlot): WallpaperSettings {
        val prefix = slot.storageName
        val scaleMode = runCatching {
            WallpaperScaleMode.valueOf(
                preferences.getString("${prefix}_scale_mode", null)
                    ?: WallpaperScaleMode.CROP.name,
            )
        }.getOrDefault(WallpaperScaleMode.CROP)

        return WallpaperSettings(
            scaleMode = scaleMode,
            zoom = preferences.getFloat("${prefix}_zoom", 1f).coerceIn(1f, MAX_ZOOM),
            focusX = preferences.getFloat("${prefix}_focus_x", 0.5f).coerceIn(0f, 1f),
            focusY = preferences.getFloat("${prefix}_focus_y", 0.5f).coerceIn(0f, 1f),
        )
    }

    fun version(slot: WallpaperSlot): Int =
        preferences.getInt(versionKey(slot), 0)

    @Throws(IOException::class)
    fun save(slot: WallpaperSlot, source: Uri, settings: WallpaperSettings) {
        val destination = imageFile(slot)
        val temporary = File(appContext.filesDir, "${destination.name}.pending")

        appContext.contentResolver.openInputStream(source)?.use { input ->
            temporary.outputStream().use(input::copyTo)
        } ?: throw IOException("无法读取所选图片")

        try {
            Os.rename(temporary.absolutePath, destination.absolutePath)
        } catch (error: Exception) {
            temporary.delete()
            throw IOException("无法保存壁纸", error)
        }

        val prefix = slot.storageName
        preferences.edit {
            putString("${prefix}_scale_mode", settings.scaleMode.name)
            putFloat("${prefix}_zoom", settings.zoom.coerceIn(1f, MAX_ZOOM))
            putFloat("${prefix}_focus_x", settings.focusX.coerceIn(0f, 1f))
            putFloat("${prefix}_focus_y", settings.focusY.coerceIn(0f, 1f))
            putInt(versionKey(slot), version(slot) + 1)
        }
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        const val MAX_ZOOM = 4f
        private const val PREFERENCES_NAME = "adaptive_wallpaper"

        fun versionKey(slot: WallpaperSlot): String = "${slot.storageName}_version"
    }
}
