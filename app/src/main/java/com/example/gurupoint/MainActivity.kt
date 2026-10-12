package com.example.gurupoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.gurupoint.data.local.SessionManager
import com.example.gurupoint.data.repository.AgentRepository
import com.example.gurupoint.data.repository.AuthRepository
import com.example.gurupoint.data.repository.ContentRepository
import com.example.gurupoint.data.repository.UserRepository
import com.example.gurupoint.ui.navigation.AppNavigation
import com.example.gurupoint.ui.navigation.Screen
import com.example.gurupoint.ui.theme.GuruPointTheme
import com.example.gurupoint.viewmodel.AgentViewModel
import com.example.gurupoint.viewmodel.AuthViewModel
import com.example.gurupoint.viewmodel.ContentViewModel
import com.example.gurupoint.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(applicationContext)
        val authRepository = AuthRepository(sessionManager)
        val userRepository = UserRepository(sessionManager)
        val contentRepository = ContentRepository(sessionManager)
        val agentRepository = AgentRepository(sessionManager)

        val authViewModel = AuthViewModel(authRepository)
        val mainViewModel = MainViewModel(userRepository)
        val contentViewModel = ContentViewModel(contentRepository)
        val agentViewModel = AgentViewModel(agentRepository)

        val startDestination = if (authRepository.isLoggedIn()) Screen.Home.route else Screen.Login.route

        setContent {
            GuruPointTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        mainViewModel = mainViewModel,
                        contentViewModel = contentViewModel,
                        agentViewModel = agentViewModel,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
