package com.example.gurupoint.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurupoint.data.model.Game
import com.example.gurupoint.ui.components.GameStatusBadge
import com.example.gurupoint.ui.theme.GoldPrimary
import com.example.gurupoint.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGamesScreen(
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val games by mainViewModel.games.collectAsState()
    var editingGame by remember { mutableStateOf<Game?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Timings & Control", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoldPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Game", tint = Color.Black)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(games) { game ->
                AdminGameCard(
                    game = game,
                    onEditClick = { editingGame = game },
                    onToggleOpen = { mainViewModel.toggleGameStatus(game.id, !game.isOpen) }
                )
            }
        }

        if (showAddDialog || editingGame != null) {
            GameEditDialog(
                initialGame = editingGame,
                onDismiss = {
                    showAddDialog = false
                    editingGame = null
                },
                onSave = { game ->
                    mainViewModel.saveGame(game)
                    showAddDialog = false
                    editingGame = null
                }
            )
        }
    }
}

@Composable
fun AdminGameCard(
    game: Game,
    onEditClick: () -> Unit,
    onToggleOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(game.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoldPrimary)
                GameStatusBadge(isOpen = game.isOpen)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Open: ${game.openTime} | Close: ${game.closeTime} | Result: ${game.resultTime}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Game")
                }

                Button(
                    onClick = onToggleOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = if (game.isOpen) MaterialTheme.colorScheme.error else GoldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (game.isOpen) "Force Close" else "Force Open", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GameEditDialog(
    initialGame: Game?,
    onDismiss: () -> Unit,
    onSave: (Game) -> Unit
) {
    var name by remember { mutableStateOf(initialGame?.name ?: "") }
    var desc by remember { mutableStateOf(initialGame?.description ?: "") }
    var openTime by remember { mutableStateOf(initialGame?.openTime ?: "10:00 AM") }
    var closeTime by remember { mutableStateOf(initialGame?.closeTime ?: "04:00 PM") }
    var resultTime by remember { mutableStateOf(initialGame?.resultTime ?: "05:00 PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGame != null) "Edit Game" else "Add New Game") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Game Name") }, singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, singleLine = true)
                OutlinedTextField(value = openTime, onValueChange = { openTime = it }, label = { Text("Open Time") }, singleLine = true)
                OutlinedTextField(value = closeTime, onValueChange = { closeTime = it }, label = { Text("Close Time") }, singleLine = true)
                OutlinedTextField(value = resultTime, onValueChange = { resultTime = it }, label = { Text("Result Time") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val gameToSave = initialGame?.copy(
                        name = name,
                        description = desc,
                        openTime = openTime,
                        closeTime = closeTime,
                        resultTime = resultTime
                    ) ?: Game(
                        id = "game_${System.currentTimeMillis()}",
                        name = name,
                        description = desc,
                        openTime = openTime,
                        closeTime = closeTime,
                        resultTime = resultTime
                    )
                    onSave(gameToSave)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
