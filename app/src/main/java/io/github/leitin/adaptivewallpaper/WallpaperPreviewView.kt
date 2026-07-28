package io.github.leitin.adaptivewallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.max

class WallpaperPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var bitmap: Bitmap? = null
    private var settings = WallpaperSettings()
    private var cropGesturesEnabled = true
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (settings.scaleMode != WallpaperScaleMode.CROP) return false
                settings = settings.copy(
                    zoom = (settings.zoom * detector.scaleFactor)
                        .coerceIn(1f, WallpaperStore.MAX_ZOOM),
                )
                invalidate()
                return true
            }
        },
    )

    fun setWallpaper(bitmap: Bitmap?, settings: WallpaperSettings) {
        if (this.bitmap !== bitmap) {
            this.bitmap?.recycle()
        }
        this.bitmap = bitmap
        this.settings = settings
        invalidate()
    }

    fun setScaleMode(scaleMode: WallpaperScaleMode) {
        settings = settings.copy(scaleMode = scaleMode)
        invalidate()
    }

    fun currentSettings(): WallpaperSettings = settings

    fun setCropGesturesEnabled(enabled: Boolean) {
        cropGesturesEnabled = enabled
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            WallpaperRenderer.drawBitmap(canvas, it, settings, Color.BLACK)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!cropGesturesEnabled) {
            return super.onTouchEvent(event)
        }
        if (!isEnabled || bitmap == null || settings.scaleMode != WallpaperScaleMode.CROP) {
            return super.onTouchEvent(event)
        }

        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                lastTouchX = event.x
                lastTouchY = event.y
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && event.pointerCount == 1) {
                    updateFocus(event.x - lastTouchX, event.y - lastTouchY)
                }
                lastTouchX = event.x
                lastTouchY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                performClick()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        bitmap?.recycle()
        bitmap = null
        super.onDetachedFromWindow()
    }

    private fun updateFocus(deltaX: Float, deltaY: Float) {
        val source = bitmap ?: return
        if (width <= 0 || height <= 0) return
        val transform = WallpaperRenderer.calculateTransform(
            source.width.toFloat(),
            source.height.toFloat(),
            width.toFloat(),
            height.toFloat(),
            settings,
        )
        val scaledWidth = max(1f, source.width * transform.scaleX)
        val scaledHeight = max(1f, source.height * transform.scaleY)
        settings = settings.copy(
            focusX = (settings.focusX - deltaX / scaledWidth).coerceIn(0f, 1f),
            focusY = (settings.focusY - deltaY / scaledHeight).coerceIn(0f, 1f),
        )
        invalidate()
    }
}
