package com.shouyihung.adaptivewallpaper

import org.junit.Assert.assertEquals
import org.junit.Test

class WallpaperRendererTest {
    @Test
    fun cropFillsCanvasAndCentersImage() {
        val transform = WallpaperRenderer.calculateTransform(
            imageWidth = 100f,
            imageHeight = 100f,
            canvasWidth = 200f,
            canvasHeight = 100f,
            settings = WallpaperSettings(scaleMode = WallpaperScaleMode.CROP),
        )

        assertTransform(transform, scaleX = 2f, scaleY = 2f, left = 0f, top = -50f)
    }

    @Test
    fun fitShowsWholeImageWithLetterboxing() {
        val transform = WallpaperRenderer.calculateTransform(
            imageWidth = 100f,
            imageHeight = 100f,
            canvasWidth = 200f,
            canvasHeight = 100f,
            settings = WallpaperSettings(scaleMode = WallpaperScaleMode.FIT),
        )

        assertTransform(transform, scaleX = 1f, scaleY = 1f, left = 50f, top = 0f)
    }

    @Test
    fun stretchUsesIndependentAxisScales() {
        val transform = WallpaperRenderer.calculateTransform(
            imageWidth = 100f,
            imageHeight = 100f,
            canvasWidth = 200f,
            canvasHeight = 100f,
            settings = WallpaperSettings(scaleMode = WallpaperScaleMode.STRETCH),
        )

        assertTransform(transform, scaleX = 2f, scaleY = 1f, left = 0f, top = 0f)
    }

    private fun assertTransform(
        actual: RenderTransform,
        scaleX: Float,
        scaleY: Float,
        left: Float,
        top: Float,
    ) {
        assertEquals(scaleX, actual.scaleX, TOLERANCE)
        assertEquals(scaleY, actual.scaleY, TOLERANCE)
        assertEquals(left, actual.left, TOLERANCE)
        assertEquals(top, actual.top, TOLERANCE)
    }

    companion object {
        private const val TOLERANCE = 0.001f
    }
}
