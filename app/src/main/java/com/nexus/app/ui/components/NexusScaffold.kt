package com.nexus.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nexus.app.ui.navigation.NexusNavGraph
import com.nexus.app.ui.navigation.Screen

@Composable
fun NexusScaffold() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?: Screen.Home.route

    val topLevelRoutes = Screen.bottomNavItems.map { it.route }
    val isSubScreen = currentRoute.startsWith("context_detail") ||
        currentRoute.startsWith("context_editor") ||
        currentRoute.startsWith("app_picker") ||
        currentRoute.startsWith("action_editor") ||
        currentRoute.startsWith("capsule_detail") ||
        currentRoute.startsWith("capsule_editor") ||
        currentRoute.startsWith("capsule_restore") ||
        currentRoute.startsWith("automation_detail") ||
        currentRoute.startsWith("automation_editor") ||
        currentRoute == Screen.AutomationHistory.route ||
        currentRoute == Screen.EnvironmentSources.route

    val showBottomBar = currentRoute in topLevelRoutes && !isSubScreen

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
                exit = slideOutVertically(tween(300)) { it } + fadeOut(tween(300)),
            ) {
                NexusBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NexusNavGraph(navController = navController)
        }
    }
}
