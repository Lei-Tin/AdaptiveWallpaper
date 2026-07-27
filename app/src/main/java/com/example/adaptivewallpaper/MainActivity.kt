package com.example.adaptivewallpaper

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
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
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
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
}
