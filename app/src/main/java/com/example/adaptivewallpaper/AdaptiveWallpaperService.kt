package com.example.adaptivewallpaper

import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat

class AdaptiveWallpaperService : WallpaperService() {
    private val engines = mutableSetOf<AdaptiveWallpaperEngine>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var wallpaperStore: WallpaperStore
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        val changedSlot = WallpaperSlot.entries.find { WallpaperStore.versionKey(it) == key }
        changedSlot?.let { slot ->
            mainHandler.post {
                engines.forEach { engine -> engine.onWallpaperChanged(slot) }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        wallpaperStore = WallpaperStore(this)
        wallpaperStore.registerListener(preferenceListener)
    }

    override fun onCreateEngine(): Engine = AdaptiveWallpaperEngine().also(engines::add)

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        engines.forEach { engine -> engine.onThemeChanged(newConfig) }
    }

    override fun onDestroy() {
        wallpaperStore.unregisterListener(preferenceListener)
        super.onDestroy()
    }

    private inner class AdaptiveWallpaperEngine : Engine() {
        private var isDarkMode = isDarkMode(resources.configuration.uiMode)
        private var cachedBitmap: Bitmap? = null
        private var cachedSlot: WallpaperSlot? = null
        private var cachedVersion = -1
        private var cachedWidth = 0
        private var cachedHeight = 0

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(false)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) drawWallpaper()
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            drawWallpaper()
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int,
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            drawWallpaper()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            clearBitmapCache()
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            engines.remove(this)
            clearBitmapCache()
            super.onDestroy()
        }

        fun onThemeChanged(configuration: Configuration) {
            val newDarkMode = isDarkMode(configuration.uiMode)
            if (newDarkMode != isDarkMode) {
                isDarkMode = newDarkMode
                drawWallpaper()
            }
        }

        fun onWallpaperChanged(slot: WallpaperSlot) {
            if (slot == currentSlot()) {
                clearBitmapCache()
                drawWallpaper()
            }
        }

        private fun drawWallpaper() {
            if (!surfaceHolder.surface.isValid) return

            val frame = surfaceHolder.surfaceFrame
            val width = frame.width().coerceAtLeast(1)
            val height = frame.height().coerceAtLeast(1)
            val slot = currentSlot()
            val settings = wallpaperStore.settings(slot)
            val bitmap = loadBitmap(slot, settings, width, height)

            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas() ?: return
                if (bitmap != null) {
                    val backgroundColor = if (slot == WallpaperSlot.DARK) {
                        Color.BLACK
                    } else {
                        Color.rgb(247, 244, 238)
                    }
                    WallpaperRenderer.drawBitmap(canvas, bitmap, settings, backgroundColor)
                } else {
                    canvas.drawColor(Color.BLACK)
                    val wallpaperResource = if (slot == WallpaperSlot.DARK) {
                        R.drawable.wallpaper_dark
                    } else {
                        R.drawable.wallpaper_light
                    }
                    ContextCompat.getDrawable(
                        this@AdaptiveWallpaperService,
                        wallpaperResource,
                    )?.run {
                        setBounds(0, 0, canvas.width, canvas.height)
                        draw(canvas)
                    }
                }
            } finally {
                canvas?.let(surfaceHolder::unlockCanvasAndPost)
            }
        }

        private fun loadBitmap(
            slot: WallpaperSlot,
            settings: WallpaperSettings,
            width: Int,
            height: Int,
        ): Bitmap? {
            val version = wallpaperStore.version(slot)
            if (cachedSlot == slot && cachedVersion == version &&
                cachedWidth == width && cachedHeight == height
            ) {
                return cachedBitmap
            }

            clearBitmapCache()
            val zoom = if (settings.scaleMode == WallpaperScaleMode.CROP) settings.zoom else 1f
            cachedBitmap = BitmapLoader.decodeFile(
                wallpaperStore.imageFile(slot),
                (width * zoom).toInt().coerceAtMost(MAX_DECODE_DIMENSION),
                (height * zoom).toInt().coerceAtMost(MAX_DECODE_DIMENSION),
            )
            cachedSlot = slot
            cachedVersion = version
            cachedWidth = width
            cachedHeight = height
            return cachedBitmap
        }

        private fun clearBitmapCache() {
            cachedBitmap?.recycle()
            cachedBitmap = null
            cachedSlot = null
            cachedVersion = -1
            cachedWidth = 0
            cachedHeight = 0
        }

        private fun currentSlot(): WallpaperSlot =
            if (isDarkMode) WallpaperSlot.DARK else WallpaperSlot.LIGHT
    }

    companion object {
        private const val MAX_DECODE_DIMENSION = 4096
    }
}
