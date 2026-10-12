package com.example.gurupoint.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Wallet : Screen("wallet")
    object Profile : Screen("profile")
    object Results : Screen("results")
    object Articles : Screen("articles")
    object Videos : Screen("videos")
    object AgentLogin : Screen("agent_login")
    object AgentDashboard : Screen("agent_dashboard")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminGames : Screen("admin_games")
    object AdminResults : Screen("admin_results")
    object AdminCoins : Screen("admin_coins")
    object AdminAgents : Screen("admin_agents")
    object AdminEmergency : Screen("admin_emergency")
    object AdminLogs : Screen("admin_logs")
    object GameSelection : Screen("game_selection/{gameId}/{gameName}") {
        fun createRoute(gameId: String, gameName: String) = "game_selection/$gameId/$gameName"
    }
}
