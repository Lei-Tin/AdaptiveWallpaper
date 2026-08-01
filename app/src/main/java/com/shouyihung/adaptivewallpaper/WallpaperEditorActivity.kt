package com.shouyihung.adaptivewallpaper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class WallpaperEditorActivity : AppCompatActivity() {
    private lateinit var previewView: WallpaperPreviewView
    private lateinit var saveButton: MaterialButton
    private lateinit var sourceUri: Uri
    private lateinit var slot: WallpaperSlot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uriValue = intent.getStringExtra(EXTRA_URI)
        val slotValue = intent.getStringExtra(EXTRA_SLOT)
        val parsedSlot = WallpaperSlot.fromStorageName(slotValue)
        if (uriValue == null || parsedSlot == null) {
            finish()
            return
        }
        sourceUri = uriValue.toUri()
        slot = parsedSlot

        enableEdgeToEdge()
        setContentView(R.layout.activity_wallpaper_editor)
        configurePreviewAspectRatio()
        val editorRoot = findViewById<android.view.View>(R.id.editorRoot)
        val initialLeft = editorRoot.paddingLeft
        val initialTop = editorRoot.paddingTop
        val initialRight = editorRoot.paddingRight
        val initialBottom = editorRoot.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(editorRoot) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                initialLeft + systemBars.left,
                initialTop + systemBars.top,
                initialRight + systemBars.right,
                initialBottom + systemBars.bottom,
            )
            insets
        }

        previewView = findViewById(R.id.wallpaperPreview)
        saveButton = findViewById(R.id.saveWallpaperButton)
        findViewById<TextView>(R.id.editorTitle).setText(
            if (slot == WallpaperSlot.LIGHT) {
                R.string.edit_light_wallpaper
            } else {
                R.string.edit_dark_wallpaper
            },
        )

        configureScaleModeControls()
        findViewById<MaterialButton>(R.id.cancelWallpaperButton).setOnClickListener { finish() }
        saveButton.setOnClickListener { saveWallpaper() }
        loadPreview()
    }

    private fun configurePreviewAspectRatio() {
        val bounds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds
        } else {
            @Suppress("DEPRECATION")
            resources.displayMetrics.run { android.graphics.Rect(0, 0, widthPixels, heightPixels) }
        }
        val previewCard = findViewById<android.view.View>(R.id.previewCard)
        val layoutParams = previewCard.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.dimensionRatio = "${bounds.width()}:${bounds.height()}"
        previewCard.layoutParams = layoutParams
    }

    private fun configureScaleModeControls() {
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.scaleModeToggle)
        toggleGroup.check(R.id.cropModeButton)
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val scaleMode = when (checkedId) {
                R.id.fitModeButton -> WallpaperScaleMode.FIT
                R.id.stretchModeButton -> WallpaperScaleMode.STRETCH
                else -> WallpaperScaleMode.CROP
            }
            previewView.setScaleMode(scaleMode)
            findViewById<TextView>(R.id.gestureHint).setText(
                if (scaleMode == WallpaperScaleMode.CROP) {
                    R.string.crop_gesture_hint
                } else {
                    R.string.scale_mode_hint
                },
            )
        }
    }

    private fun loadPreview() {
        saveButton.isEnabled = false
        Thread {
            val bitmap = runCatching {
                BitmapLoader.decodeUri(this, sourceUri, PREVIEW_MAX_SIZE, PREVIEW_MAX_SIZE)
            }.getOrNull()
            runOnUiThread {
                if (isFinishing || isDestroyed) {
                    bitmap?.recycle()
                    return@runOnUiThread
                }
                if (bitmap == null) {
                    Toast.makeText(this, R.string.image_load_failed, Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val initialSettings = if (sourceUri.scheme == "file") {
                        WallpaperStore(this).settings(slot)
                    } else {
                        WallpaperSettings()
                    }
                    previewView.setWallpaper(bitmap, initialSettings)
                    selectScaleMode(initialSettings.scaleMode)
                    saveButton.isEnabled = true
                }
            }
        }.start()
    }

    private fun selectScaleMode(scaleMode: WallpaperScaleMode) {
        val buttonId = when (scaleMode) {
            WallpaperScaleMode.CROP -> R.id.cropModeButton
            WallpaperScaleMode.FIT -> R.id.fitModeButton
            WallpaperScaleMode.STRETCH -> R.id.stretchModeButton
        }
        findViewById<MaterialButtonToggleGroup>(R.id.scaleModeToggle).check(buttonId)
    }

    private fun saveWallpaper() {
        saveButton.isEnabled = false
        val settings = previewView.currentSettings()
        Thread {
            val result = runCatching {
                WallpaperStore(this).save(slot, sourceUri, settings)
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                result.onSuccess {
                    Toast.makeText(this, R.string.wallpaper_saved, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }.onFailure {
                    saveButton.isEnabled = true
                    Toast.makeText(this, R.string.wallpaper_save_failed, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    companion object {
        private const val EXTRA_URI = "image_uri"
        private const val EXTRA_SLOT = "wallpaper_slot"
        private const val PREVIEW_MAX_SIZE = 2048

        fun createIntent(context: Context, uri: Uri, slot: WallpaperSlot): Intent =
            Intent(context, WallpaperEditorActivity::class.java).apply {
                putExtra(EXTRA_URI, uri.toString())
                putExtra(EXTRA_SLOT, slot.storageName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
    }
}
