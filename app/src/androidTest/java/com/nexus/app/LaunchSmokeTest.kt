package com.nexus.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Launches the manifest activity through the real Application startup path. */
@RunWith(AndroidJUnit4::class)
class LaunchSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivity_launchesAndDisplaysHomeScreen() {
        assertTrue(composeRule.activity.application is NexusApplication)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("NEXUS").assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
    }
}
