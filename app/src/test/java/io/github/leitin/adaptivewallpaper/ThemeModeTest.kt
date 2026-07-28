package io.github.leitin.adaptivewallpaper

import android.app.UiModeManager
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

    @Test
    fun explicitSystemDarkModeOverridesStaleLightConfiguration() {
        val staleUiMode = Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_NO

        assertTrue(resolveDarkMode(staleUiMode, UiModeManager.MODE_NIGHT_YES))
    }

    @Test
    fun automaticSystemModeUsesCurrentDisplayConfiguration() {
        val uiMode = Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_YES

        assertTrue(resolveDarkMode(uiMode, UiModeManager.MODE_NIGHT_AUTO))
    }
}
