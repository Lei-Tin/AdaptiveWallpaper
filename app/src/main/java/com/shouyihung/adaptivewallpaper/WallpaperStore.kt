package com.shouyihung.adaptivewallpaper

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

    fun imageFile(slot: WallpaperSlot): File {
        val destination = privateImageFile(slot)
        val legacy = legacyImageFile(slot)

        synchronized(MIGRATION_LOCK) {
            if (destination.isFile) {
                legacy.delete()
                return destination
            }
            if (!legacy.isFile) return destination

            val temporary = File(destination.parentFile, "${destination.name}.migration")
            runCatching {
                temporary.delete()
                legacy.inputStream().use { input ->
                    temporary.outputStream().use(input::copyTo)
                }
                replaceFile(temporary, destination)
                legacy.delete()
            }.onFailure {
                temporary.delete()
            }
        }

        return if (destination.isFile) destination else legacy
    }

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
        val destination = privateImageFile(slot)
        val temporary = File(destination.parentFile, "${destination.name}.pending")

        appContext.contentResolver.openInputStream(source)?.use { input ->
            temporary.outputStream().use(input::copyTo)
        } ?: throw IOException("无法读取所选图片")

        try {
            replaceFile(temporary, destination)
            legacyImageFile(slot).delete()
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

    private fun privateImageFile(slot: WallpaperSlot): File =
        File(appContext.noBackupFilesDir, imageFileName(slot))

    private fun legacyImageFile(slot: WallpaperSlot): File =
        File(appContext.filesDir, imageFileName(slot))

    private fun imageFileName(slot: WallpaperSlot): String =
        "wallpaper_${slot.storageName}.image"

    private fun replaceFile(source: File, destination: File) {
        Os.rename(source.absolutePath, destination.absolutePath)
    }

    companion object {
        const val MAX_ZOOM = 4f
        private const val PREFERENCES_NAME = "adaptive_wallpaper"
        private val MIGRATION_LOCK = Any()

        fun versionKey(slot: WallpaperSlot): String = "${slot.storageName}_version"
    }
}
