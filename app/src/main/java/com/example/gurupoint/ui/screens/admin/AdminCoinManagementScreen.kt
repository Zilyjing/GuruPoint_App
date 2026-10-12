package com.example.gurupoint.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurupoint.ui.theme.GoldPrimary
import com.example.gurupoint.viewmodel.AdminActionResult
import com.example.gurupoint.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCoinManagementScreen(
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val adminActionResult by mainViewModel.adminActionResult.collectAsState()

    var userMobile by remember { mutableStateOf("") }
    var coinAmountText by remember { mutableStateOf("") }
    var isAddOperation by remember { mutableStateOf(true) }
    var reason by remember { mutableStateOf("Admin Manual Adjustment") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Coin Management", fontWeight = FontWeight.Bold) },
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
                    Text("Manual Virtual Coin Adjustment", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldPrimary)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Operation Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isAddOperation = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isAddOperation) GoldPrimary else Color.Transparent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ADD COINS", color = if (isAddOperation) Color.Black else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { isAddOperation = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (!isAddOperation) MaterialTheme.colorScheme.error else Color.Transparent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("DEDUCT COINS", color = if (!isAddOperation) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = userMobile,
                        onValueChange = { if (it.length <= 10) userMobile = it },
                        label = { Text("User Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = coinAmountText,
                        onValueChange = { coinAmountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Coin Amount") },
                        leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Mandatory Reason / Note") },
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
                            val amt = coinAmountText.toLongOrNull() ?: 0L
                            mainViewModel.adminAdjustCoins("usr_$userMobile", userMobile, amt, isAddOperation, reason)
                        },
                        enabled = userMobile.length == 10 && (coinAmountText.toLongOrNull() ?: 0L) > 0L && reason.isNotBlank() && adminActionResult !is AdminActionResult.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isAddOperation) GoldPrimary else MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (adminActionResult is AdminActionResult.Loading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (isAddOperation) "Add Virtual Coins" else "Deduct Virtual Coins", color = if (isAddOperation) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
