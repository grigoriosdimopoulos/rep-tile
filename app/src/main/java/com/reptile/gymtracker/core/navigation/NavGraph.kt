package com.reptile.gymtracker.core.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reptile.gymtracker.ui.activesession.ActiveSessionScreen
import com.reptile.gymtracker.ui.history.HistoryScreen
import com.reptile.gymtracker.ui.mainmenu.MainMenuScreen
import com.reptile.gymtracker.ui.sessionsummary.SessionSummaryScreen
import com.reptile.gymtracker.ui.settings.SettingsScreen

@Composable
fun RepTileNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainMenu.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onNavigateToSession = { sessionId ->
                    navController.navigate(Screen.ActiveSession.createRoute(sessionId))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToSessionSummary = { sessionId ->
                    navController.navigate(Screen.SessionSummary.createRoute(sessionId))
                }
            )
        }

        composable(
            route = Screen.ActiveSession.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { it } }
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments!!.getLong("sessionId")
            ActiveSessionScreen(
                sessionId = sessionId,
                onSessionEnded = { finishedSessionId ->
                    navController.navigate(Screen.SessionSummary.createRoute(finishedSessionId)) {
                        popUpTo(Screen.MainMenu.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.SessionSummary.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { it } }
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments!!.getLong("sessionId")
            SessionSummaryScreen(
                sessionId = sessionId,
                onDone = {
                    navController.navigate(Screen.MainMenu.route) {
                        popUpTo(Screen.MainMenu.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.SessionSummary.createRoute(sessionId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
