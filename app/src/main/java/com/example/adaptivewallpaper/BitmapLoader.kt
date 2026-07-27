package com.example.adaptivewallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.InputStream

object BitmapLoader {
    fun decodeUri(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int): Bitmap? {
        val resolver = context.contentResolver
        val orientation = resolver.openInputStream(uri)?.use(::readOrientation)
            ?: ExifInterface.ORIENTATION_NORMAL
        return decodeSampled(
            openStream = { resolver.openInputStream(uri) },
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            orientation = orientation,
        )
    }

    fun decodeFile(file: File, maxWidth: Int, maxHeight: Int): Bitmap? {
        if (!file.isFile) return null
        val orientation = runCatching {
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        return decodeSampled(
            openStream = file::inputStream,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            orientation = orientation,
        )
    }

    private fun decodeSampled(
        openStream: () -> InputStream?,
        maxWidth: Int,
        maxHeight: Int,
        orientation: Int,
    ): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openStream()?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(
                bounds.outWidth,
                bounds.outHeight,
                maxWidth.coerceAtLeast(1),
                maxHeight.coerceAtLeast(1),
            )
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = openStream()?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
        return applyOrientation(bitmap, orientation)
    }

    private fun readOrientation(stream: InputStream): Int = runCatching {
        ExifInterface(stream).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    private fun calculateSampleSize(
        width: Int,
        height: Int,
        maxWidth: Int,
        maxHeight: Int,
    ): Int {
        var sampleSize = 1
        while (decodedPixelCount(width, height, sampleSize) > MAX_DECODED_PIXELS) {
            sampleSize *= 2
        }
        while (width / (sampleSize * 2) >= maxWidth &&
            height / (sampleSize * 2) >= maxHeight
        ) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun decodedPixelCount(width: Int, height: Int, sampleSize: Int): Long =
        width.toLong() / sampleSize * (height.toLong() / sampleSize)

    private fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it !== bitmap) bitmap.recycle()
        }
    }

    private const val MAX_DECODED_PIXELS = 8_000_000L
}
