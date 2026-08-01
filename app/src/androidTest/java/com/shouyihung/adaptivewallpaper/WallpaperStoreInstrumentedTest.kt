package com.shouyihung.adaptivewallpaper

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WallpaperStoreInstrumentedTest {
    private val fileName = "wallpaper_light.image"
    private lateinit var testRoot: File
    private lateinit var context: Context
    private lateinit var legacyFile: File
    private lateinit var privateFile: File

    @Before
    fun prepareFiles() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        testRoot = File(targetContext.cacheDir, "wallpaper-store-test").apply {
            deleteRecursively()
            mkdirs()
        }
        val files = File(testRoot, "files").apply { mkdirs() }
        val noBackupFiles = File(testRoot, "no_backup").apply { mkdirs() }
        context = object : ContextWrapper(targetContext) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir(): File = files
            override fun getNoBackupFilesDir(): File = noBackupFiles
        }
        legacyFile = File(files, fileName)
        privateFile = File(noBackupFiles, fileName)
    }

    @After
    fun cleanUpFiles() {
        testRoot.deleteRecursively()
    }

    @Test
    fun imageFileMigratesLegacyWallpaperOutOfBackupStorage() {
        val original = byteArrayOf(1, 3, 3, 7)
        legacyFile.writeBytes(original)

        val result = WallpaperStore(context).imageFile(WallpaperSlot.LIGHT)

        assertEquals(privateFile.absolutePath, result.absolutePath)
        assertArrayEquals(original, result.readBytes())
        assertFalse(legacyFile.exists())
    }
}
