package com.example.gurupoint.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gurupoint.ui.screens.admin.*
import com.example.gurupoint.ui.screens.agent.AgentDashboardScreen
import com.example.gurupoint.ui.screens.agent.AgentLoginScreen
import com.example.gurupoint.ui.screens.auth.ForgotPasswordScreen
import com.example.gurupoint.ui.screens.auth.LoginScreen
import com.example.gurupoint.ui.screens.auth.RegisterScreen
import com.example.gurupoint.ui.screens.content.ArticlesScreen
import com.example.gurupoint.ui.screens.content.VideosScreen
import com.example.gurupoint.ui.screens.game.GameSelectionScreen
import com.example.gurupoint.ui.screens.home.HomeScreen
import com.example.gurupoint.ui.screens.profile.ProfileScreen
import com.example.gurupoint.ui.screens.results.ResultsScreen
import com.example.gurupoint.ui.screens.wallet.WalletScreen
import com.example.gurupoint.viewmodel.AgentViewModel
import com.example.gurupoint.viewmodel.AuthViewModel
import com.example.gurupoint.viewmodel.ContentViewModel
import com.example.gurupoint.viewmodel.MainViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    contentViewModel: ContentViewModel,
    agentViewModel: AgentViewModel,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                mainViewModel = mainViewModel,
                authViewModel = authViewModel,
                contentViewModel = contentViewModel,
                onNavigateToGameSelection = { gameId, gameName ->
                    navController.navigate(Screen.GameSelection.createRoute(gameId, gameName))
                },
                onNavigateToWallet = {
                    navController.navigate(Screen.Wallet.route)
                },
                onNavigateToResults = {
                    navController.navigate(Screen.Results.route)
                },
                onNavigateToArticles = {
                    navController.navigate(Screen.Articles.route)
                },
                onNavigateToVideos = {
                    navController.navigate(Screen.Videos.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Results.route) {
            ResultsScreen(
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Articles.route) {
            ArticlesScreen(
                contentViewModel = contentViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Videos.route) {
            VideosScreen(
                contentViewModel = contentViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GameSelection.route,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType },
                navArgument("gameName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val gameName = backStackEntry.arguments?.getString("gameName") ?: ""

            GameSelectionScreen(
                gameId = gameId,
                gameName = gameName,
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Wallet.route) {
            WalletScreen(
                mainViewModel = mainViewModel,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- AGENT PORTAL ROUTES ---
        composable(Screen.AgentLogin.route) {
            AgentLoginScreen(
                agentViewModel = agentViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.AgentDashboard.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AgentDashboard.route) {
            AgentDashboardScreen(
                agentViewModel = agentViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = { navController.popBackStack() }
            )
        }

        // --- ADMIN ROUTES ---
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAdminGames = { navController.navigate(Screen.AdminGames.route) },
                onNavigateToAdminResults = { navController.navigate(Screen.AdminResults.route) },
                onNavigateToAdminCoins = { navController.navigate(Screen.AdminCoins.route) },
                onNavigateToAdminAgents = { navController.navigate(Screen.AdminAgents.route) },
                onNavigateToAdminEmergency = { navController.navigate(Screen.AdminEmergency.route) },
                onNavigateToAdminLogs = { navController.navigate(Screen.AdminLogs.route) }
            )
        }

        composable(Screen.AdminAgents.route) {
            AdminAgentManagementScreen(
                agentViewModel = agentViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminGames.route) {
            AdminGamesScreen(
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminResults.route) {
            AdminResultDeclarationScreen(
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminCoins.route) {
            AdminCoinManagementScreen(
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminEmergency.route) {
            AdminEmergencyAndSettingsScreen(
                contentViewModel = contentViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminLogs.route) {
            AdminActivityLogsScreen(
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
