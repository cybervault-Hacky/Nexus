package com.nexus.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nexus.app.NexusApplication
import com.nexus.app.ui.screens.actions.QuickActionsScreen
import com.nexus.app.ui.screens.actionEditor.ActionEditorScreen
import com.nexus.app.ui.screens.actionEditor.ActionViewModel
import com.nexus.app.ui.screens.appPicker.AppPickerScreen
import com.nexus.app.ui.screens.appPicker.AppViewModel
import com.nexus.app.ui.screens.automationDetail.AutomationDetailScreen
import com.nexus.app.ui.screens.automationEditor.AutomationEditorScreen
import com.nexus.app.ui.screens.automationHistory.AutomationHistoryScreen
import com.nexus.app.ui.screens.automations.AutomationScreen
import com.nexus.app.ui.screens.automations.AutomationViewModel
import com.nexus.app.ui.screens.environmentSources.EnvironmentSourcesScreen
import com.nexus.app.ui.screens.capsuleDetail.CapsuleDetailScreen
import com.nexus.app.ui.screens.capsuleEditor.CapsuleEditorScreen
import com.nexus.app.ui.screens.capsules.CapsuleViewModel
import com.nexus.app.ui.screens.capsules.CapsulesScreen
import com.nexus.app.ui.screens.restoreFlow.CapsuleRestoreViewModel
import com.nexus.app.ui.screens.restoreFlow.RestoreFlowScreen
import com.nexus.app.ui.screens.contextDetail.ContextDetailScreen
import com.nexus.app.ui.screens.contextEditor.ContextEditorScreen
import com.nexus.app.ui.screens.contexts.ContextViewModel
import com.nexus.app.ui.screens.contexts.ContextsScreen
import com.nexus.app.ui.screens.home.HomeScreen
import com.nexus.app.ui.screens.settings.SettingsScreen

private const val TRANSITION_DURATION = 300

@Composable
fun NexusNavGraph(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as NexusApplication

    val contextViewModel: ContextViewModel = viewModel(
        factory = ContextViewModel.Factory(app.contextRepository),
    )
    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModel.Factory(
            installedAppRepository = app.installedAppRepository,
            contextAppRepository = app.contextAppRepository,
            appLauncher = app.appLauncher,
        ),
    )
    val actionViewModel: ActionViewModel = viewModel(
        factory = ActionViewModel.Factory(
            actionRepository = app.actionRepository,
            workflowExecutor = app.workflowExecutor,
        ),
    )
    val capsuleViewModel: CapsuleViewModel = viewModel(
        factory = CapsuleViewModel.Factory(app.capsuleRepository),
    )
    val capsuleRestoreViewModel: CapsuleRestoreViewModel = viewModel(
        factory = CapsuleRestoreViewModel.Factory(
            capsuleRepository = app.capsuleRepository,
            contextRepository = app.contextRepository,
            contextAppRepository = app.contextAppRepository,
            actionRepository = app.actionRepository,
            previewEngine = app.restorePreviewEngine,
            restoreEngine = app.capsuleRestoreEngine,
        ),
    )
    val automationViewModel: AutomationViewModel = viewModel(
        factory = AutomationViewModel.Factory(
            repository = app.automationRepository,
            triggerEngine = app.triggerEngine,
            scheduler = app.automationScheduler,
        ),
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION)) +
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(TRANSITION_DURATION))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(TRANSITION_DURATION)) +
                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(TRANSITION_DURATION))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION)) +
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(TRANSITION_DURATION))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(TRANSITION_DURATION)) +
                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(TRANSITION_DURATION))
        },
    ) {
        // ── Top-level destinations ───────────────────────────
        composable(Screen.Home.route) {
            val activeContext by contextViewModel.activeContext.collectAsState()
            val capsules by capsuleViewModel.capsules.collectAsState()
            val enabledAutomations by automationViewModel.enabledCount.collectAsState()
            HomeScreen(
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToContextDetail = { navController.navigate(Screen.ContextDetail.createRoute(it)) },
                onNavigateToCapsuleDetail = { navController.navigate(Screen.CapsuleDetail.createRoute(it)) },
                activeContext = activeContext,
                recentCapsules = capsules,
                activeAutomationCount = enabledAutomations,
            )
        }
        composable(Screen.Contexts.route) {
            ContextsScreen(viewModel = contextViewModel, onNavigateToDetail = { navController.navigate(Screen.ContextDetail.createRoute(it)) }, onNavigateToCreate = { navController.navigate(Screen.ContextEditor.createRoute()) })
        }
        composable(Screen.Capsules.route) {
            CapsulesScreen(viewModel = capsuleViewModel, onNavigateToDetail = { navController.navigate(Screen.CapsuleDetail.createRoute(it)) })
        }
        composable(Screen.Actions.route) { QuickActionsScreen() }
        composable(Screen.Automations.route) {
            AutomationScreen(viewModel = automationViewModel, onNavigateToDetail = { navController.navigate(Screen.AutomationDetail.createRoute(it)) }, onNavigateToCreate = { navController.navigate(Screen.AutomationEditor.createRoute()) })
        }
        composable(Screen.Settings.route) { SettingsScreen() }

        // ── Context sub-screens ──────────────────────────────
        composable(Screen.ContextDetail.route, arguments = listOf(navArgument("contextId") { type = NavType.StringType })) { entry ->
            val ctxId = entry.arguments?.getString("contextId") ?: return@composable
            ContextDetailScreen(ctxId, contextViewModel, appViewModel, actionViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate(Screen.ContextEditor.createRoute(it)) },
                onNavigateToAddApps = { navController.navigate(Screen.AppPicker.createRoute(it)) },
                onNavigateToAddAction = { navController.navigate(Screen.ActionEditor.createRoute(it)) },
                onNavigateToEditAction = { navController.navigate(Screen.ActionEditor.createRoute(ctxId, it)) },
                onNavigateToCaptureCapsule = { navController.navigate(Screen.CapsuleEditor.createRoute(it, contextViewModel.contexts.value.find { c -> c.id == it }?.name ?: "")) },
            )
        }
        composable(Screen.ContextEditor.route, arguments = listOf(navArgument("contextId") { type = NavType.StringType; defaultValue = "" })) { entry ->
            val editingId = entry.arguments?.getString("contextId")?.takeIf { it.isNotBlank() }
            ContextEditorScreen(contextViewModel, editingId, onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.AppPicker.route, arguments = listOf(navArgument("contextId") { type = NavType.StringType })) { entry ->
            val ctxId = entry.arguments?.getString("contextId") ?: return@composable
            AppPickerScreen(ctxId, appViewModel, onConfirm = { navController.popBackStack() }, onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.ActionEditor.route, arguments = listOf(navArgument("contextId") { type = NavType.StringType }, navArgument("actionId") { type = NavType.StringType; defaultValue = "" })) { entry ->
            val ctxId = entry.arguments?.getString("contextId") ?: return@composable
            val actId = entry.arguments?.getString("actionId")?.takeIf { it.isNotBlank() }
            ActionEditorScreen(ctxId, actId, actionViewModel, onNavigateBack = { navController.popBackStack() })
        }

        // ── Capsule sub-screens ──────────────────────────────
        composable(Screen.CapsuleDetail.route, arguments = listOf(navArgument("capsuleId") { type = NavType.StringType })) { entry ->
            val capId = entry.arguments?.getString("capsuleId") ?: return@composable
            CapsuleDetailScreen(capId, capsuleViewModel, onNavigateBack = { navController.popBackStack() }, onNavigateToRestore = { navController.navigate(Screen.CapsuleRestore.createRoute(it)) })
        }
        composable(Screen.CapsuleEditor.route, arguments = listOf(navArgument("contextId") { type = NavType.StringType }, navArgument("contextName") { type = NavType.StringType; defaultValue = "" })) { entry ->
            val ctxId = entry.arguments?.getString("contextId") ?: return@composable
            val ctxName = entry.arguments?.getString("contextName") ?: ""
            CapsuleEditorScreen(ctxId, ctxName, capsuleViewModel, onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.CapsuleRestore.route, arguments = listOf(navArgument("capsuleId") { type = NavType.StringType })) { entry ->
            val capId = entry.arguments?.getString("capsuleId") ?: return@composable
            RestoreFlowScreen(capId, capsuleRestoreViewModel, onNavigateBack = { navController.popBackStack() }, onNavigateToContext = { navController.popBackStack(); navController.navigate(Screen.ContextDetail.createRoute(it)) })
        }

        // ── Automation sub-screens ────────────────────────────
        composable(Screen.AutomationDetail.route, arguments = listOf(navArgument("automationId") { type = NavType.StringType })) { entry ->
            val autoId = entry.arguments?.getString("automationId") ?: return@composable
            AutomationDetailScreen(autoId, automationViewModel, onNavigateBack = { navController.popBackStack() }, onNavigateToEdit = { navController.navigate(Screen.AutomationEditor.createRoute(it)) })
        }
        composable(Screen.AutomationEditor.route, arguments = listOf(navArgument("automationId") { type = NavType.StringType; defaultValue = "" })) { entry ->
            val editingId = entry.arguments?.getString("automationId")?.takeIf { it.isNotBlank() }
            AutomationEditorScreen(editingId, automationViewModel, availableContexts = contextViewModel.contexts.value, onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.AutomationHistory.route) {
            AutomationHistoryScreen(automationViewModel, onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.EnvironmentSources.route) {
            EnvironmentSourcesScreen(
                registry = app.eventSourceRegistry,
                settings = app.automationSettings,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
