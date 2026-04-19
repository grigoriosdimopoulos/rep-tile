package com.reptile.gymtracker.core.navigation

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object ActiveSession : Screen("active_session/{sessionId}") {
        fun createRoute(sessionId: Long) = "active_session/$sessionId"
    }
    object SessionSummary : Screen("session_summary/{sessionId}") {
        fun createRoute(sessionId: Long) = "session_summary/$sessionId"
    }
    object History : Screen("history")
    object Settings : Screen("settings")
}
