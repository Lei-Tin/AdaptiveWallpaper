package io.github.leitin.adaptivewallpaper

import android.content.res.Configuration
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun detectsDarkModeWhilePreservingOtherUiModeBits() {
        val uiMode = Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_YES

        assertTrue(isDarkMode(uiMode))
    }

    @Test
    fun detectsLightModeWhilePreservingOtherUiModeBits() {
        val uiMode = Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_NO

        assertFalse(isDarkMode(uiMode))
    }
}
