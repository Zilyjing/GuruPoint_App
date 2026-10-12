package com.example.gurupoint.ui.screens.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
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
import com.example.gurupoint.data.model.RegisteredAccount
import com.example.gurupoint.ui.theme.DangerRed
import com.example.gurupoint.ui.theme.GoldPrimary
import com.example.gurupoint.viewmodel.AgentUiState
import com.example.gurupoint.viewmodel.AgentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDashboardScreen(
    agentViewModel: AgentViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val currentAgent by agentViewModel.currentAgent.collectAsState()
    val customers by agentViewModel.agentCustomers.collectAsState()
    val transactions by agentViewModel.agentTransactions.collectAsState()
    val uiState by agentViewModel.uiState.collectAsState()

    var targetCustomerMobile by remember { mutableStateOf("") }
    var coinAmountText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent Dashboard (${currentAgent?.agentCode ?: ""})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        agentViewModel.logoutAgent()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = DangerRed)
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
            // Agent Profile & Wallet Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(currentAgent?.name ?: "Agent", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        Text("Agent Code: ${currentAgent?.agentCode ?: ""}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Wallet Balance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text("${currentAgent?.coinBalance ?: 0} Coins", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Today Earnings", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text("₹${currentAgent?.todayEarnings ?: 0.0}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Distribute Coins to Customer Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Distribute Coins to Customer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = targetCustomerMobile,
                            onValueChange = { if (it.length <= 10) targetCustomerMobile = it },
                            label = { Text("Customer Mobile Number") },
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
                            label = { Text("Virtual Coins Amount") },
                            leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState is AgentUiState.Error) {
                            Text(
                                text = (uiState as AgentUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        if (uiState is AgentUiState.Success) {
                            Text(
                                text = (uiState as AgentUiState.Success).message,
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        Button(
                            onClick = {
                                val amt = coinAmountText.toLongOrNull() ?: 0L
                                agentViewModel.distributeCoinsToCustomer(targetCustomerMobile, amt)
                            },
                            enabled = targetCustomerMobile.length == 10 && (coinAmountText.toLongOrNull() ?: 0L) > 0L && uiState !is AgentUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState is AgentUiState.Loading) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Transfer Coins to Customer", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }

            // My Customers Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("My Assigned Customers (${customers.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            items(customers) { cust ->
                CustomerCard(customer = cust, onSelectClick = { targetCustomerMobile = cust.mobileNumber })
            }
        }
    }
}

@Composable
fun CustomerCard(customer: RegisteredAccount, onSelectClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("+91 ${customer.mobileNumber}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${customer.coinBalance} Coins", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldPrimary)
                TextButton(onClick = onSelectClick) {
                    Text("Select for Transfer", fontSize = 12.sp, color = GoldPrimary)
                }
            }
        }
    }
}
