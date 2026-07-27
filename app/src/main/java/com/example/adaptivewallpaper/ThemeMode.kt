package com.example.adaptivewallpaper

import android.content.res.Configuration

internal fun isDarkMode(uiMode: Int): Boolean =
    uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
