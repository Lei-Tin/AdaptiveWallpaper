package io.github.leitin.adaptivewallpaper

enum class WallpaperSlot(val storageName: String) {
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromStorageName(value: String?): WallpaperSlot? = entries.find {
            it.storageName == value
        }
    }
}

enum class WallpaperScaleMode {
    CROP,
    FIT,
    STRETCH,
}

data class WallpaperSettings(
    val scaleMode: WallpaperScaleMode = WallpaperScaleMode.CROP,
    val zoom: Float = 1f,
    val focusX: Float = 0.5f,
    val focusY: Float = 0.5f,
)
