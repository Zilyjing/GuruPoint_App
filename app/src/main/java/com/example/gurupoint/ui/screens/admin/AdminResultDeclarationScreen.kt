package com.example.gurupoint.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurupoint.data.model.Game
import com.example.gurupoint.ui.theme.GoldPrimary
import com.example.gurupoint.viewmodel.AdminActionResult
import com.example.gurupoint.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminResultDeclarationScreen(
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val games by mainViewModel.games.collectAsState()
    val adminActionResult by mainViewModel.adminActionResult.collectAsState()

    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var declaredNumber by remember { mutableStateOf("") }
    var sourceName by remember { mutableStateOf("Public Web Result Source") }
    var expandedDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(games) {
        if (selectedGame == null && games.isNotEmpty()) {
            selectedGame = games.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Declare Result & Rewards", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Select Game for Result Declaration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Game Dropdown Box
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedGame?.name ?: "Select Game",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Game") },
                            leadingIcon = { Icon(Icons.Default.Casino, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            games.forEach { game ->
                                DropdownMenuItem(
                                    text = { Text(game.name) },
                                    onClick = {
                                        selectedGame = game
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Result Number Input
                    OutlinedTextField(
                        value = declaredNumber,
                        onValueChange = { if (it.length <= 2) declaredNumber = it.filter { c -> c.isDigit() } },
                        label = { Text("Result Number (00-99)") },
                        leadingIcon = { Icon(Icons.Default.RateReview, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = sourceName,
                        onValueChange = { sourceName = it },
                        label = { Text("Source Name / Fetcher") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (adminActionResult is AdminActionResult.Error) {
                        Text(
                            text = (adminActionResult as AdminActionResult.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    if (adminActionResult is AdminActionResult.Success) {
                        Text(
                            text = (adminActionResult as AdminActionResult.Success).message,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            selectedGame?.let { game ->
                                mainViewModel.declareResult(game.id, declaredNumber, sourceName)
                            }
                        },
                        enabled = selectedGame != null && declaredNumber.isNotEmpty() && adminActionResult !is AdminActionResult.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (adminActionResult is AdminActionResult.Loading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Declare Result & Calculate Rewards", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
