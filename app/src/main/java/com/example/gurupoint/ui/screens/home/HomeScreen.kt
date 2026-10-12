package com.example.gurupoint.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurupoint.data.model.Game
import com.example.gurupoint.ui.components.AppTopBar
import com.example.gurupoint.ui.components.GameStatusBadge
import com.example.gurupoint.ui.screens.maintenance.MaintenanceScreen
import com.example.gurupoint.ui.theme.GoldPrimary
import com.example.gurupoint.ui.theme.SuccessGreen
import com.example.gurupoint.viewmodel.AuthViewModel
import com.example.gurupoint.viewmodel.ContentViewModel
import com.example.gurupoint.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    contentViewModel: ContentViewModel,
    onNavigateToGameSelection: (String, String) -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToResults: () -> Unit,
    onNavigateToArticles: () -> Unit,
    onNavigateToVideos: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val games by mainViewModel.games.collectAsState()
    val user by authViewModel.currentUser.collectAsState()
    val appConfig by contentViewModel.appConfig.collectAsState()
    val announcements by contentViewModel.announcements.collectAsState()
    val context = LocalContext.current

    // If Maintenance Mode is active, show Maintenance Screen
    if (appConfig.isMaintenanceMode) {
        MaintenanceScreen(message = appConfig.maintenanceMessage)
        return
    }

    Scaffold(
        topBar = {
            AppTopBar(
                coinBalance = user?.coinBalance ?: 0L,
                onWalletClick = onNavigateToWallet,
                onResultsClick = onNavigateToResults,
                onProfileClick = onNavigateToProfile
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
            // System Announcements
            if (announcements.isNotEmpty()) {
                item {
                    val ann = announcements.first()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Announcement",
                                tint = GoldPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = ann.title,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = ann.message,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Quick Content Nav Cards (Articles & Videos)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = onNavigateToArticles,
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Articles", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Card(
                        onClick = onNavigateToVideos,
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Videos", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Agent Support Contact Quick Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val waNumber = user?.agentWhatsApp?.takeIf { it.isNotBlank() } ?: "919027750140"
                    Card(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/$waNumber"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("💬 WhatsApp Support", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SuccessGreen)
                        }
                    }

                    val tgUser = user?.agentTelegram?.takeIf { it.isNotBlank() } ?: "vinodagent"
                    Card(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/$tgUser"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0088cc).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("✈️ Telegram Support", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0088cc))
                        }
                    }
                }
            }

            // Games Header
            item {
                Text(
                    text = "Live Entertainment Games",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Game Cards
            items(games) { game ->
                GameCard(
                    game = game,
                    isEmergencyStopped = appConfig.isEmergencyGamesStopped,
                    onPlayClick = { onNavigateToGameSelection(game.id, game.name) }
                )
            }
        }
    }
}

@Composable
fun GameCard(
    game: Game,
    isEmergencyStopped: Boolean,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                GameStatusBadge(isOpen = game.isOpen && !isEmergencyStopped)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Time",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Close: ${game.closeTime} | Result: ${game.resultTime}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Latest Result",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = game.latestResult ?: "Awaiting",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }

                Button(
                    onClick = onPlayClick,
                    enabled = game.isOpen && !isEmergencyStopped,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Select Numbers",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
