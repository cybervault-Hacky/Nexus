package com.nexus.app

import com.nexus.app.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for app search/filtering logic.
 * Tests the pure filtering logic without Android framework dependencies.
 */
class AppSearchTest {

    private val testApps = listOf(
        InstalledApp(packageName = "com.termux", appName = "Termux"),
        InstalledApp(packageName = "com.android.chrome", appName = "Chrome"),
        InstalledApp(packageName = "com.github.android", appName = "GitHub"),
        InstalledApp(packageName = "com.google.android.youtube", appName = "YouTube"),
        InstalledApp(packageName = "org.mozilla.firefox", appName = "Firefox"),
    )

    // Replicate the search logic from InstalledAppRepositoryImpl
    private fun searchApps(query: String): List<InstalledApp> {
        if (query.isBlank()) return testApps
        val lowerQuery = query.lowercase()
        return testApps.filter { app ->
            app.appName.lowercase().contains(lowerQuery) ||
                app.packageName.lowercase().contains(lowerQuery)
        }
    }

    @Test
    fun `empty query returns all apps`() {
        assertEquals(5, searchApps("").size)
        assertEquals(5, searchApps("  ").size)
    }

    @Test
    fun `search by app name`() {
        val results = searchApps("termux")
        assertEquals(1, results.size)
        assertEquals("com.termux", results[0].packageName)
    }

    @Test
    fun `search by app name case insensitive`() {
        assertEquals(1, searchApps("TERMUX").size)
        assertEquals(1, searchApps("Termux").size)
        assertEquals(1, searchApps("termux").size)
    }

    @Test
    fun `search by package name`() {
        val results = searchApps("mozilla")
        assertEquals(1, results.size)
        assertEquals("org.mozilla.firefox", results[0].packageName)
    }

    @Test
    fun `search by partial name`() {
        val results = searchApps("ro")
        // Matches Chrome (name "Chrome" / pkg "com.android.chrome"),
        // GitHub (pkg "com.github.android") and YouTube (pkg
        // "com.google.android.youtube") — all contain "ro" via name or package.
        assertEquals(3, results.size)
    }

    @Test
    fun `search with no matches`() {
        val results = searchApps("nonexistent")
        assertEquals(0, results.size)
    }

    @Test
    fun `search by partial package name`() {
        val results = searchApps("github")
        assertEquals(1, results.size)
        assertEquals("com.github.android", results[0].packageName)
    }
}
