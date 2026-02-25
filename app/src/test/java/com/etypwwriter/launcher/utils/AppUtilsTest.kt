package com.etypwwriter.launcher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppUtilsTest {

    @Test
    fun `getInstalledApps returns sorted list excluding launcher`() = runTest {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()

        every { context.packageName } returns "com.etypwwriter.launcher"
        every { context.packageManager } returns packageManager

        // Instantiate ResolveInfo with Robolectric shadows present
        val zebraInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply { packageName = "com.example.app1" }
            nonLocalizedLabel = "Zebra App"
        }
        
        val appleInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply { packageName = "com.example.app2" }
            nonLocalizedLabel = "Apple App"
        }
        
        val launcherInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply { packageName = "com.etypwwriter.launcher" }
            nonLocalizedLabel = "Launcher"
        }

        every { 
            packageManager.queryIntentActivities(any<Intent>(), PackageManager.MATCH_ALL) 
        } returns listOf(zebraInfo, appleInfo, launcherInfo)

        val result = getInstalledApps(context)

        // Verifications
        assertEquals(2, result.size)
        // Should be sorted by label
        assertEquals("Apple App", result[0].label)
        assertEquals("com.example.app2", result[0].packageName)
        assertEquals("Zebra App", result[1].label)
        assertEquals("com.example.app1", result[1].packageName)
    }
}
