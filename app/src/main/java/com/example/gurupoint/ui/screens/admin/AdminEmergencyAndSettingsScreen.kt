package com.example.gurupoint.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurupoint.data.model.Announcement
import com.example.gurupoint.ui.theme.DangerRed
import com.example.gurupoint.ui.theme.GoldPrimary
import com.example.gurupoint.viewmodel.ContentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEmergencyAndSettingsScreen(
    contentViewModel: ContentViewModel,
    onNavigateBack: () -> Unit
) {
    val appConfig by contentViewModel.appConfig.collectAsState()
    val announcements by contentViewModel.announcements.collectAsState()

    var showAddAnnouncementDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency & Settings", fontWeight = FontWeight.Bold) },
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
            // Emergency Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Emergency", tint = DangerRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emergency Controls", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DangerRed)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Maintenance Mode Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Maintenance Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Show maintenance screen to all non-admin users", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Switch(
                                checked = appConfig.isMaintenanceMode,
                                onCheckedChange = { contentViewModel.toggleMaintenanceMode(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = DangerRed)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Emergency Stop All Games Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Emergency Stop Games", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Prevent any number selections on all games immediately", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Switch(
                                checked = appConfig.isEmergencyGamesStopped,
                                onCheckedChange = { contentViewModel.toggleEmergencyStopGames(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = DangerRed)
                            )
                        }
                    }
                }
            }

            // Announcements Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = GoldPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Announcements", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            IconButton(onClick = { showAddAnnouncementDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = GoldPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        announcements.forEach { ann ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(ann.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GoldPrimary)
                                    Text(ann.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddAnnouncementDialog) {
            AddAnnouncementDialog(
                onDismiss = { showAddAnnouncementDialog = false },
                onSave = { ann ->
                    contentViewModel.saveAnnouncement(ann)
                    showAddAnnouncementDialog = false
                }
            )
        }
    }
}

@Composable
fun AddAnnouncementDialog(
    onDismiss: () -> Unit,
    onSave: (Announcement) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Announcement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(Announcement("ann_${System.currentTimeMillis()}", title, message, isActive = true))
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Publish", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
