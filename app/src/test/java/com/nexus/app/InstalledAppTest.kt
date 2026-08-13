package com.nexus.app

import com.nexus.app.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the InstalledApp domain model.
 */
class InstalledAppTest {

    @Test
    fun `InstalledApp holds correct data`() {
        val app = InstalledApp(
            packageName = "com.termux",
            appName = "Termux",
            isLaunchable = true,
        )
        assertEquals("com.termux", app.packageName)
        assertEquals("Termux", app.appName)
        assertTrue(app.isLaunchable)
    }

    @Test
    fun `InstalledApp default isLaunchable is true`() {
        val app = InstalledApp(packageName = "com.example", appName = "Example")
        assertTrue(app.isLaunchable)
    }

    @Test
    fun `packageName is the stable identity`() {
        val app1 = InstalledApp(packageName = "com.termux", appName = "Termux")
        val app2 = InstalledApp(packageName = "com.termux", appName = "Different Name")
        assertEquals(app1.packageName, app2.packageName)
        assertEquals(app1, app2.copy(appName = app1.appName))
    }

    @Test
    fun `different packageNames are different`() {
        val app1 = InstalledApp(packageName = "com.a", appName = "A")
        val app2 = InstalledApp(packageName = "com.b", appName = "B")
        assertNotEquals(app1.packageName, app2.packageName)
    }

    @Test
    fun `copy preserves packageName`() {
        val original = InstalledApp(packageName = "com.termux", appName = "Termux")
        val copy = original.copy(appName = "Modified")
        assertEquals("com.termux", copy.packageName)
        assertEquals("Modified", copy.appName)
    }
}
