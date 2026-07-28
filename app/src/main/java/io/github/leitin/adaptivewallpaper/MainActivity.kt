package io.github.leitin.adaptivewallpaper

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
            confirmEnableWallpaper()
        }
        findViewById<MaterialButton>(R.id.disableWallpaperButton).setOnClickListener {
            confirmDisableWallpaper()
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

    private fun confirmEnableWallpaper() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.enable_wallpaper_title)
            .setMessage(R.string.enable_wallpaper_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.continue_to_preview) { _, _ ->
                openWallpaperPreview()
            }
            .show()
    }

    private fun confirmDisableWallpaper() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.disable_wallpaper_title)
            .setMessage(R.string.disable_wallpaper_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.restore_default_wallpaper) { _, _ ->
                disableWallpaper()
            }
            .show()
    }

    private fun disableWallpaper() {
        val disableButton = findViewById<MaterialButton>(R.id.disableWallpaperButton)
        val wallpaperManager = WallpaperManager.getInstance(this)
        val activeFlags = adaptiveWallpaperFlags(wallpaperManager)
        if (activeFlags == 0) {
            updateStatus()
            return
        }
        disableButton.isEnabled = false

        Thread {
            val result = runCatching {
                wallpaperManager.clear(activeFlags)
            }

            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread

                if (result.isSuccess) {
                    Toast.makeText(this, R.string.wallpaper_disabled, Toast.LENGTH_SHORT).show()
                    updateStatus()
                } else {
                    disableButton.isEnabled = true
                    Toast.makeText(
                        this,
                        R.string.wallpaper_disable_failed,
                        Toast.LENGTH_LONG,
                    ).show()
                    openSystemWallpaperChooser()
                }
            }
        }.start()
    }

    private fun openSystemWallpaperChooser() {
        try {
            startActivity(Intent(Intent.ACTION_SET_WALLPAPER))
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
        }
    }

    private fun updateStatus() {
        val darkModeActive = isDarkMode(resources.configuration.uiMode)
        val currentMode = getString(
            if (darkModeActive) R.string.current_mode_dark else R.string.current_mode_light,
        )

        val activeFlags = adaptiveWallpaperFlags(WallpaperManager.getInstance(this))
        val enabledStatus = getString(when (activeFlags) {
            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK ->
                R.string.wallpaper_enabled_both
            WallpaperManager.FLAG_SYSTEM -> R.string.wallpaper_enabled_home
            WallpaperManager.FLAG_LOCK -> R.string.wallpaper_enabled_lock
            else -> R.string.wallpaper_not_enabled
        })

        findViewById<MaterialButton>(R.id.disableWallpaperButton).isEnabled = activeFlags != 0

        findViewById<TextView>(R.id.statusText).text =
            getString(R.string.status_format, currentMode, enabledStatus)
    }

    private fun adaptiveWallpaperFlags(wallpaperManager: WallpaperManager): Int {
        var flags = 0
        val homeWallpaper = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runCatching {
                wallpaperManager.getWallpaperInfo(WallpaperManager.FLAG_SYSTEM)
            }.getOrNull()
        } else {
            wallpaperManager.wallpaperInfo
        }

        if (isAdaptiveWallpaper(homeWallpaper)) {
            flags = flags or WallpaperManager.FLAG_SYSTEM

            // A missing lock-specific wallpaper means the lock screen inherits the home
            // wallpaper. In that shared state getWallpaperInfo(FLAG_LOCK) returns null.
            if (wallpaperManager.getWallpaperId(WallpaperManager.FLAG_LOCK) < 0) {
                flags = flags or WallpaperManager.FLAG_LOCK
            }
        }

        val lockWallpaper = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runCatching {
                wallpaperManager.getWallpaperInfo(WallpaperManager.FLAG_LOCK)
            }.getOrNull()
        } else {
            null
        }
        if (isAdaptiveWallpaper(lockWallpaper)) {
            flags = flags or WallpaperManager.FLAG_LOCK
        }
        return flags
    }

    private fun isAdaptiveWallpaper(info: android.app.WallpaperInfo?): Boolean =
        info?.let {
            it.packageName == packageName &&
                it.serviceName == AdaptiveWallpaperService::class.java.name
        } == true

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
