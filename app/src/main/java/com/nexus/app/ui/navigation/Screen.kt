package com.nexus.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Defines every top-level destination and its nav-bar metadata.
 * Also includes Phase 2 sub-destinations (detail, editor).
 */
sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    data object Home : Screen(
        route = "home",
        label = "Home",
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home,
    )

    data object Contexts : Screen(
        route = "contexts",
        label = "Contexts",
        selectedIcon = Icons.Rounded.GridView,
        unselectedIcon = Icons.Outlined.GridView,
    )

    data object Capsules : Screen(
        route = "capsules",
        label = "Capsules",
        selectedIcon = Icons.Rounded.Inventory2,
        unselectedIcon = Icons.Outlined.Inventory2,
    )

    data object Actions : Screen(
        route = "actions",
        label = "Actions",
        selectedIcon = Icons.Rounded.Bolt,
        unselectedIcon = Icons.Outlined.Bolt,
    )

    data object Settings : Screen(
        route = "settings",
        label = "Settings",
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    )

    /** Context detail — argument: contextId */
    data object ContextDetail {
        const val route = "context_detail/{contextId}"
        fun createRoute(contextId: String) = "context_detail/$contextId"
    }

    /** Context editor — argument: contextId (null for create) */
    data object ContextEditor {
        const val route = "context_editor?contextId={contextId}"
        fun createRoute(contextId: String? = null): String =
            if (contextId != null) "context_editor?contextId=$contextId"
            else "context_editor"
    }

    /** App picker — argument: contextId */
    data object AppPicker {
        const val route = "app_picker/{contextId}"
        fun createRoute(contextId: String) = "app_picker/$contextId"
    }

    /** Action editor — arguments: contextId, actionId (empty for create) */
    data object ActionEditor {
        const val route = "action_editor/{contextId}?actionId={actionId}"
        fun createRoute(contextId: String, actionId: String? = null): String =
            if (actionId != null) "action_editor/$contextId?actionId=$actionId"
            else "action_editor/$contextId"
    }

    /** Capsule detail — argument: capsuleId */
    data object CapsuleDetail {
        const val route = "capsule_detail/{capsuleId}"
        fun createRoute(capsuleId: String) = "capsule_detail/$capsuleId"
    }

    /** Capsule editor (capture from context) — arguments: contextId, contextName */
    data object CapsuleEditor {
        const val route = "capsule_editor/{contextId}?contextName={contextName}"
        fun createRoute(contextId: String, contextName: String = ""): String =
            "capsule_editor/$contextId?contextName=$contextName"
    }

    /** Capsule restore flow — argument: capsuleId */
    data object CapsuleRestore {
        const val route = "capsule_restore/{capsuleId}"
        fun createRoute(capsuleId: String) = "capsule_restore/$capsuleId"
    }

    /** Automations top-level tab. */
    data object Automations : Screen(
        route = "automations",
        label = "Automate",
        selectedIcon = androidx.compose.material.icons.rounded.AutoAwesome,
        unselectedIcon = androidx.compose.material.icons.outlined.AutoAwesome,
    )

    /** Automation detail — argument: automationId */
    data object AutomationDetail {
        const val route = "automation_detail/{automationId}"
        fun createRoute(automationId: String) = "automation_detail/$automationId"
    }

    /** Automation editor — argument: automationId (empty for create) */
    data object AutomationEditor {
        const val route = "automation_editor?automationId={automationId}"
        fun createRoute(automationId: String? = null): String =
            if (automationId != null) "automation_editor?automationId=$automationId"
            else "automation_editor"
    }

    /** Automation execution history */
    data object AutomationHistory {
        const val route = "automation_history"
    }

    /** Environment trigger sources */
    data object EnvironmentSources {
        const val route = "environment_sources"
    }

    /** Privacy settings */
    data object PrivacySettings {
        const val route = "privacy_settings"
    }

    /** Event diagnostics */
    data object EventDiagnostics {
        const val route = "event_diagnostics"
    }

    /** Automation simulator */
    data object AutomationSimulator {
        const val route = "automation_simulator"
    }

    companion object {
        /** Ordered list of screens shown in the bottom nav bar. */
        val bottomNavItems = listOf(Home, Contexts, Capsules, Actions, Automations, Settings)
    }
}
