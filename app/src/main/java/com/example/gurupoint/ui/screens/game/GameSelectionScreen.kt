package com.example.gurupoint.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurupoint.data.model.SelectionMode
import com.example.gurupoint.ui.theme.GoldPrimary
import com.example.gurupoint.viewmodel.MainViewModel
import com.example.gurupoint.viewmodel.SelectionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSelectionScreen(
    gameId: String,
    gameName: String,
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val selectionState by mainViewModel.selectionUiState.collectAsState()

    var selectedMode by remember { mutableStateOf(SelectionMode.MODE_A) }
    var selectedNumbers by remember { mutableStateOf(setOf<String>()) }
    var coinAmountText by remember { mutableStateOf("50") }

    val maxRange = if (selectedMode == SelectionMode.MODE_A) 99 else 9
    val rewardMultiplier = if (selectedMode == SelectionMode.MODE_A) 90 else 9

    LaunchedEffect(selectionState) {
        if (selectionState is SelectionUiState.Success) {
            mainViewModel.resetSelectionState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gameName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
            ) {
                Button(
                    onClick = {
                        selectedMode = SelectionMode.MODE_A
                        selectedNumbers = emptySet()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMode == SelectionMode.MODE_A) GoldPrimary else Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Mode A (00–99)",
                        color = if (selectedMode == SelectionMode.MODE_A) Color.Black else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        selectedMode = SelectionMode.MODE_B
                        selectedNumbers = emptySet()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMode == SelectionMode.MODE_B) GoldPrimary else Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Mode B (0–9)",
                        color = if (selectedMode == SelectionMode.MODE_B) Color.Black else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Selection Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedMode == SelectionMode.MODE_A) "Select Numbers (00 - 99):" else "Select Digits (0 - 9):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Select All
                    OutlinedButton(
                        onClick = {
                            val allSet = (0..maxRange).map { idx ->
                                if (selectedMode == SelectionMode.MODE_A) String.format("%02d", idx) else idx.toString()
                            }.toSet()
                            selectedNumbers = allSet
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Select All", fontSize = 12.sp)
                    }

                    // Clear All
                    OutlinedButton(
                        onClick = { selectedNumbers = emptySet() },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Number Grid
            val gridColumns = if (selectedMode == SelectionMode.MODE_A) 5 else 5

            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(maxRange + 1) { index ->
                    val numStr = if (selectedMode == SelectionMode.MODE_A) String.format("%02d", index) else index.toString()
                    val isSelected = selectedNumbers.contains(numStr)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                selectedNumbers = if (isSelected) {
                                    selectedNumbers - numStr
                                } else {
                                    selectedNumbers + numStr
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                            Text(
                                text = numStr,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi Selection Summary & Order Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val count = selectedNumbers.size
                    val coinsPerNum = coinAmountText.toLongOrNull() ?: 0L
                    val totalCoinsRequired = count * coinsPerNum

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected (${count}): ${if (count > 0) selectedNumbers.sorted().joinToString(", ") else "None"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GoldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = coinAmountText,
                        onValueChange = { coinAmountText = it.filter { char -> char.isDigit() } },
                        label = { Text("Coins per Number") },
                        leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Deducted: $totalCoinsRequired Coins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Potential Reward: ${coinsPerNum * rewardMultiplier} Coins/Num (${rewardMultiplier}x)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }

                    if (selectionState is SelectionUiState.Error) {
                        Text(
                            text = (selectionState as SelectionUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            mainViewModel.submitMultipleSelections(
                                gameId = gameId,
                                gameName = gameName,
                                mode = selectedMode,
                                numbers = selectedNumbers.sorted(),
                                coinsPerNumber = coinsPerNum
                            )
                        },
                        enabled = count > 0 && coinsPerNum > 0L && selectionState !is SelectionUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (selectionState is SelectionUiState.Loading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (count > 0) "Confirm $count Selection(s) ($totalCoinsRequired Coins)" else "Select at least 1 Number",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
