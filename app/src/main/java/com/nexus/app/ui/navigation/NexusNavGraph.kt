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

/** Duration used for all navigation transitions. */
private const val TRANSITION_DURATION = 300

/**
 * Top-level NavHost for NEXUS.
 * Creates shared ViewModels and routes to all destinations.
 */
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

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION),
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(TRANSITION_DURATION)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION),
                )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION),
                )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(TRANSITION_DURATION)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION),
                )
        },
    ) {
        // ── Top-level destinations ───────────────────────────
        composable(Screen.Home.route) {
            val activeContext by contextViewModel.activeContext.collectAsState()
            val capsules by capsuleViewModel.capsules.collectAsState()
            HomeScreen(
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToContextDetail = { contextId ->
                    navController.navigate(Screen.ContextDetail.createRoute(contextId))
                },
                onNavigateToCapsuleDetail = { capsuleId ->
                    navController.navigate(Screen.CapsuleDetail.createRoute(capsuleId))
                },
                activeContext = activeContext,
                recentCapsules = capsules,
            )
        }
        composable(Screen.Contexts.route) {
            ContextsScreen(
                viewModel = contextViewModel,
                onNavigateToDetail = { contextId ->
                    navController.navigate(Screen.ContextDetail.createRoute(contextId))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.ContextEditor.createRoute())
                },
            )
        }
        composable(Screen.Capsules.route) {
            CapsulesScreen(
                viewModel = capsuleViewModel,
                onNavigateToDetail = { capsuleId ->
                    navController.navigate(Screen.CapsuleDetail.createRoute(capsuleId))
                },
            )
        }
        composable(Screen.Actions.route) {
            QuickActionsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        // ── Context detail ───────────────────────────────────
        composable(
            route = Screen.ContextDetail.route,
            arguments = listOf(navArgument("contextId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val contextId = backStackEntry.arguments?.getString("contextId") ?: return@composable
            ContextDetailScreen(
                contextId = contextId,
                viewModel = contextViewModel,
                appViewModel = appViewModel,
                actionViewModel = actionViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.ContextEditor.createRoute(id))
                },
                onNavigateToAddApps = { id ->
                    navController.navigate(Screen.AppPicker.createRoute(id))
                },
                onNavigateToAddAction = { id ->
                    navController.navigate(Screen.ActionEditor.createRoute(id))
                },
                onNavigateToEditAction = { actionId ->
                    navController.navigate(Screen.ActionEditor.createRoute(contextId, actionId))
                },
                onNavigateToCaptureCapsule = { id ->
                    val ctx = contextViewModel.contexts.value.find { it.id == id }
                    navController.navigate(Screen.CapsuleEditor.createRoute(id, ctx?.name ?: ""))
                },
            )
        }

        // ── Context editor ───────────────────────────────────
        composable(
            route = Screen.ContextEditor.route,
            arguments = listOf(
                navArgument("contextId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val editingId = backStackEntry.arguments?.getString("contextId")
                ?.takeIf { it.isNotBlank() }
            ContextEditorScreen(
                viewModel = contextViewModel,
                editingContextId = editingId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── App picker ───────────────────────────────────────
        composable(
            route = Screen.AppPicker.route,
            arguments = listOf(navArgument("contextId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val contextId = backStackEntry.arguments?.getString("contextId") ?: return@composable
            AppPickerScreen(
                contextId = contextId,
                viewModel = appViewModel,
                onConfirm = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Action editor ────────────────────────────────────
        composable(
            route = Screen.ActionEditor.route,
            arguments = listOf(
                navArgument("contextId") { type = NavType.StringType },
                navArgument("actionId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val contextId = backStackEntry.arguments?.getString("contextId") ?: return@composable
            val actionId = backStackEntry.arguments?.getString("actionId")
                ?.takeIf { it.isNotBlank() }
            ActionEditorScreen(
                contextId = contextId,
                editingActionId = actionId,
                viewModel = actionViewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Capsule detail ───────────────────────────────────
        composable(
            route = Screen.CapsuleDetail.route,
            arguments = listOf(navArgument("capsuleId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val capsuleId = backStackEntry.arguments?.getString("capsuleId") ?: return@composable
            CapsuleDetailScreen(
                capsuleId = capsuleId,
                viewModel = capsuleViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRestore = { id ->
                    navController.navigate(Screen.CapsuleRestore.createRoute(id))
                },
            )
        }

        // ── Capsule editor (capture) ─────────────────────────
        composable(
            route = Screen.CapsuleEditor.route,
            arguments = listOf(
                navArgument("contextId") { type = NavType.StringType },
                navArgument("contextName") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val contextId = backStackEntry.arguments?.getString("contextId") ?: return@composable
            val contextName = backStackEntry.arguments?.getString("contextName") ?: ""
            CapsuleEditorScreen(
                contextId = contextId,
                contextName = contextName,
                viewModel = capsuleViewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Capsule restore flow ─────────────────────────────
        composable(
            route = Screen.CapsuleRestore.route,
            arguments = listOf(navArgument("capsuleId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val capsuleId = backStackEntry.arguments?.getString("capsuleId") ?: return@composable
            RestoreFlowScreen(
                capsuleId = capsuleId,
                viewModel = capsuleRestoreViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToContext = { contextId ->
                    navController.popBackStack()
                    navController.navigate(Screen.ContextDetail.createRoute(contextId))
                },
            )
        }
    }
}
