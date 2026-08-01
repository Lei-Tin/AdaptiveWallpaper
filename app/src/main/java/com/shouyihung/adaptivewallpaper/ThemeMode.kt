package com.shouyihung.adaptivewallpaper

import android.app.UiModeManager
import android.content.res.Configuration

internal fun isDarkMode(uiMode: Int): Boolean =
    uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

internal fun resolveDarkMode(uiMode: Int, requestedNightMode: Int): Boolean =
    when (requestedNightMode) {
        UiModeManager.MODE_NIGHT_YES -> true
        UiModeManager.MODE_NIGHT_NO -> false
        else -> isDarkMode(uiMode)
    }
