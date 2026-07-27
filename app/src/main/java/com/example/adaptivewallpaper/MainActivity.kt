package com.example.adaptivewallpaper

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private val wallpaperStore by lazy { WallpaperStore(this) }

    private val pickLightWallpaper = registerForActivityResult(PickVisualMedia()) { uri ->
        uri?.let { openWallpaperEditor(it, WallpaperSlot.LIGHT) }
    }

    private val pickDarkWallpaper = registerForActivityResult(PickVisualMedia()) { uri ->
        uri?.let { openWallpaperEditor(it, WallpaperSlot.DARK) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.enableWallpaperButton).setOnClickListener {
            openWallpaperPreview()
        }
        findViewById<MaterialButton>(R.id.chooseLightWallpaperButton).setOnClickListener {
            pickLightWallpaper.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
        findViewById<MaterialButton>(R.id.chooseDarkWallpaperButton).setOnClickListener {
            pickDarkWallpaper.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
        findViewById<WallpaperPreviewView>(R.id.lightPreviewImage).apply {
            setCropGesturesEnabled(false)
            setOnClickListener {
                editExistingWallpaper(WallpaperSlot.LIGHT)
            }
        }
        findViewById<WallpaperPreviewView>(R.id.darkPreviewImage).apply {
            setCropGesturesEnabled(false)
            setOnClickListener {
                editExistingWallpaper(WallpaperSlot.DARK)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        updatePreviews()
    }

    private fun openWallpaperEditor(uri: Uri, slot: WallpaperSlot) {
        startActivity(WallpaperEditorActivity.createIntent(this, uri, slot))
    }

    private fun editExistingWallpaper(slot: WallpaperSlot) {
        if (wallpaperStore.hasImage(slot)) {
            openWallpaperEditor(Uri.fromFile(wallpaperStore.imageFile(slot)), slot)
        } else if (slot == WallpaperSlot.LIGHT) {
            pickLightWallpaper.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        } else {
            pickDarkWallpaper.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
    }

    private fun openWallpaperPreview() {
        val component = ComponentName(this, AdaptiveWallpaperService::class.java)
        val previewIntent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
        }

        try {
            startActivity(previewIntent)
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
        }
    }

    private fun updateStatus() {
        val darkModeActive = isDarkMode(resources.configuration.uiMode)
        val currentMode = getString(
            if (darkModeActive) R.string.current_mode_dark else R.string.current_mode_light,
        )

        val wallpaperInfo = WallpaperManager.getInstance(this).wallpaperInfo
        val isEnabled = wallpaperInfo?.let {
            it.packageName == packageName &&
                it.serviceName == AdaptiveWallpaperService::class.java.name
        } == true
        val enabledStatus = getString(
            if (isEnabled) R.string.wallpaper_enabled else R.string.wallpaper_not_enabled,
        )

        findViewById<TextView>(R.id.statusText).text =
            getString(R.string.status_format, currentMode, enabledStatus)
    }

    private fun updatePreviews() {
        updatePreview(
            viewId = R.id.lightPreviewImage,
            slot = WallpaperSlot.LIGHT,
            fallbackResource = R.drawable.wallpaper_light,
        )
        updatePreview(
            viewId = R.id.darkPreviewImage,
            slot = WallpaperSlot.DARK,
            fallbackResource = R.drawable.wallpaper_dark,
        )
    }

    private fun updatePreview(viewId: Int, slot: WallpaperSlot, fallbackResource: Int) {
        val preview = findViewById<WallpaperPreviewView>(viewId)
        val bitmap = BitmapLoader.decodeFile(
            wallpaperStore.imageFile(slot),
            PREVIEW_MAX_SIZE,
            PREVIEW_MAX_SIZE,
        )
        if (bitmap == null) {
            preview.setBackgroundResource(fallbackResource)
            preview.setWallpaper(null, WallpaperSettings())
        } else {
            preview.setBackgroundColor(Color.BLACK)
            preview.setWallpaper(bitmap, wallpaperStore.settings(slot))
        }
    }

    companion object {
        private const val PREVIEW_MAX_SIZE = 600
    }
}
