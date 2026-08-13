package com.nexus.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.nexus.app.ui.components.NexusScaffold
import com.nexus.app.ui.theme.NexusTheme
import org.junit.Rule
import org.junit.Test

/**
 * UI tests verifying that key screens render and navigation works.
 * Updated for Phase 2 — the database starts empty in tests,
 * so the Contexts screen shows the empty state.
 */
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysNexusTitle() {
        composeTestRule.setContent {
            NexusTheme {
                NexusScaffold()
            }
        }
        composeTestRule.onNodeWithText("NEXUS").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysGreeting() {
        composeTestRule.setContent {
            NexusTheme {
                NexusScaffold()
            }
        }
        val greetings = listOf("Good morning", "Good afternoon", "Good evening")
        val found = greetings.any { greeting ->
            try {
                composeTestRule.onNodeWithText(greeting).assertIsDisplayed()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        assert(found) { "None of the expected greetings were displayed" }
    }

    @Test
    fun bottomNav_navigatesToContexts() {
        composeTestRule.setContent {
            NexusTheme {
                NexusScaffold()
            }
        }
        composeTestRule.onNodeWithText("Contexts").performClick()
        composeTestRule.waitForIdle()
        // Database starts empty, so we should see the empty state
        composeTestRule.onNodeWithText("Contexts").assertIsDisplayed()
    }

    @Test
    fun bottomNav_navigatesToCapsules() {
        composeTestRule.setContent {
            NexusTheme {
                NexusScaffold()
            }
        }
        composeTestRule.onNodeWithText("Capsules").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Capsules").assertIsDisplayed()
    }

    @Test
    fun bottomNav_navigatesToActions() {
        composeTestRule.setContent {
            NexusTheme {
                NexusScaffold()
            }
        }
        composeTestRule.onNodeWithText("Actions").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Actions").assertIsDisplayed()
    }

    @Test
    fun bottomNav_navigatesToSettings() {
        composeTestRule.setContent {
            NexusTheme {
                NexusScaffold()
            }
        }
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun darkTheme_doesNotCrash() {
        composeTestRule.setContent {
            NexusTheme(themeMode = com.nexus.app.domain.model.ThemeMode.DARK) {
                NexusScaffold()
            }
        }
        composeTestRule.onNodeWithText("NEXUS").assertIsDisplayed()
    }

    @Test
    fun lightTheme_doesNotCrash() {
        composeTestRule.setContent {
            NexusTheme(themeMode = com.nexus.app.domain.model.ThemeMode.LIGHT) {
                NexusScaffold()
            }
        }
        composeTestRule.onNodeWithText("NEXUS").assertIsDisplayed()
    }

    @Test
    fun contextsScreen_showsEmptyStateWhenNoContexts() {
        composeTestRule.setContent {
            NexusTheme {
                NexusScaffold()
            }
        }
        composeTestRule.onNodeWithText("Contexts").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("No contexts yet").assertIsDisplayed()
    }
}
