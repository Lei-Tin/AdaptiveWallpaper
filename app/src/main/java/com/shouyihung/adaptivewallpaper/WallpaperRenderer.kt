package com.shouyihung.adaptivewallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.withSave
import kotlin.math.max
import kotlin.math.min

data class RenderTransform(
    val scaleX: Float,
    val scaleY: Float,
    val left: Float,
    val top: Float,
)

object WallpaperRenderer {
    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
    }

    fun drawBitmap(
        canvas: Canvas,
        bitmap: Bitmap,
        settings: WallpaperSettings,
        backgroundColor: Int = Color.BLACK,
    ) {
        canvas.drawColor(backgroundColor)
        val transform = calculateTransform(
            imageWidth = bitmap.width.toFloat(),
            imageHeight = bitmap.height.toFloat(),
            canvasWidth = canvas.width.toFloat(),
            canvasHeight = canvas.height.toFloat(),
            settings = settings,
        )

        canvas.withSave {
            translate(transform.left, transform.top)
            scale(transform.scaleX, transform.scaleY)
            drawBitmap(bitmap, 0f, 0f, paint)
        }
    }

    fun calculateTransform(
        imageWidth: Float,
        imageHeight: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        settings: WallpaperSettings,
    ): RenderTransform {
        require(imageWidth > 0f && imageHeight > 0f)
        require(canvasWidth > 0f && canvasHeight > 0f)

        return when (settings.scaleMode) {
            WallpaperScaleMode.STRETCH -> RenderTransform(
                scaleX = canvasWidth / imageWidth,
                scaleY = canvasHeight / imageHeight,
                left = 0f,
                top = 0f,
            )

            WallpaperScaleMode.FIT -> {
                val scale = min(canvasWidth / imageWidth, canvasHeight / imageHeight)
                RenderTransform(
                    scaleX = scale,
                    scaleY = scale,
                    left = (canvasWidth - imageWidth * scale) / 2f,
                    top = (canvasHeight - imageHeight * scale) / 2f,
                )
            }

            WallpaperScaleMode.CROP -> {
                val scale = max(canvasWidth / imageWidth, canvasHeight / imageHeight) *
                    settings.zoom.coerceIn(1f, WallpaperStore.MAX_ZOOM)
                val scaledWidth = imageWidth * scale
                val scaledHeight = imageHeight * scale
                val unclampedLeft = canvasWidth / 2f -
                    settings.focusX.coerceIn(0f, 1f) * scaledWidth
                val unclampedTop = canvasHeight / 2f -
                    settings.focusY.coerceIn(0f, 1f) * scaledHeight
                RenderTransform(
                    scaleX = scale,
                    scaleY = scale,
                    left = unclampedLeft.coerceIn(canvasWidth - scaledWidth, 0f),
                    top = unclampedTop.coerceIn(canvasHeight - scaledHeight, 0f),
                )
            }
        }
    }
}
