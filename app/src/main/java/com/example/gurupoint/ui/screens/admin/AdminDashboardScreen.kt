package com.example.gurupoint.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurupoint.ui.theme.GoldPrimary
import com.example.gurupoint.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAdminGames: () -> Unit,
    onNavigateToAdminResults: () -> Unit,
    onNavigateToAdminCoins: () -> Unit,
    onNavigateToAdminAgents: () -> Unit,
    onNavigateToAdminEmergency: () -> Unit,
    onNavigateToAdminLogs: () -> Unit
) {
    val games by mainViewModel.games.collectAsState()
    val selections by mainViewModel.selections.collectAsState()
    val results by mainViewModel.results.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Master Admin Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stat Overview Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Active Games", "${games.count { it.isOpen }}", Modifier.weight(1f))
                    StatCard("Selections Today", "${selections.size}", Modifier.weight(1f))
                    StatCard("Results Declared", "${results.count { it.isDeclared }}", Modifier.weight(1f))
                }
            }

            item {
                Text("Master Admin Controls", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            // Quick Actions
            item {
                AdminActionTile(
                    title = "Agent Management System",
                    description = "Create Agents, Assign Unique Codes, Allocate Unlimited Coins, Block & Customer Transfer",
                    icon = Icons.Default.Badge,
                    onClick = onNavigateToAdminAgents
                )
            }

            item {
                AdminActionTile(
                    title = "Games & Timings Control",
                    description = "Add/Edit games, Opening/Closing times, Active/Inactive state",
                    icon = Icons.Default.Casino,
                    onClick = onNavigateToAdminGames
                )
            }

            item {
                AdminActionTile(
                    title = "Declare Results & Rewards",
                    description = "Manual or Auto-Fetch result declaration, winning reward calculator",
                    icon = Icons.Default.RateReview,
                    onClick = onNavigateToAdminResults
                )
            }

            item {
                AdminActionTile(
                    title = "Admin Coin Management",
                    description = "Manual Coin Add/Deduct for any user with mandatory reason & audit ledger",
                    icon = Icons.Default.MonetizationOn,
                    onClick = onNavigateToAdminCoins
                )
            }

            item {
                AdminActionTile(
                    title = "Emergency & App Settings",
                    description = "1-Click Emergency Stop, Maintenance Mode toggle, Announcements & Ads",
                    icon = Icons.Default.Warning,
                    onClick = onNavigateToAdminEmergency
                )
            }

            item {
                AdminActionTile(
                    title = "Admin Audit Activity Logs",
                    description = "Track all admin actions, changes, timestamps, and target details",
                    icon = Icons.Default.History,
                    onClick = onNavigateToAdminLogs
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun AdminActionTile(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = GoldPrimary, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}
