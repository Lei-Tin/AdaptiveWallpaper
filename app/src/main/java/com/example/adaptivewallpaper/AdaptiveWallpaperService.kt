package com.example.adaptivewallpaper

import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat

class AdaptiveWallpaperService : WallpaperService() {
    private val engines = mutableSetOf<AdaptiveWallpaperEngine>()

    override fun onCreateEngine(): Engine = AdaptiveWallpaperEngine().also(engines::add)

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        engines.forEach { engine -> engine.onThemeChanged(newConfig) }
    }

    private inner class AdaptiveWallpaperEngine : Engine() {
        private var isDarkMode = isDarkMode(resources.configuration.uiMode)

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
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            engines.remove(this)
            super.onDestroy()
        }

        fun onThemeChanged(configuration: Configuration) {
            val newDarkMode = isDarkMode(configuration.uiMode)
            if (newDarkMode != isDarkMode) {
                isDarkMode = newDarkMode
                drawWallpaper()
            }
        }

        private fun drawWallpaper() {
            if (!surfaceHolder.surface.isValid) return

            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas() ?: return
                canvas.drawColor(Color.BLACK)

                val wallpaperResource = if (isDarkMode) {
                    R.drawable.wallpaper_dark
                } else {
                    R.drawable.wallpaper_light
                }
                ContextCompat.getDrawable(this@AdaptiveWallpaperService, wallpaperResource)?.run {
                    setBounds(0, 0, canvas.width, canvas.height)
                    draw(canvas)
                }
            } finally {
                canvas?.let(surfaceHolder::unlockCanvasAndPost)
            }
        }
    }
}
