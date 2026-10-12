package com.example.gurupoint.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurupoint.data.model.Agent
import com.example.gurupoint.data.model.AgentStatus
import com.example.gurupoint.ui.theme.DangerRed
import com.example.gurupoint.ui.theme.GoldPrimary
import com.example.gurupoint.viewmodel.AgentUiState
import com.example.gurupoint.viewmodel.AgentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAgentManagementScreen(
    agentViewModel: AgentViewModel,
    onNavigateBack: () -> Unit
) {
    val agents by agentViewModel.agentList.collectAsState()
    val uiState by agentViewModel.uiState.collectAsState()

    var showCreateAgentDialog by remember { mutableStateOf(false) }
    var coinTransferAgent by remember { mutableStateOf<Agent?>(null) }
    var coinTransferAmountText by remember { mutableStateOf("10000") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent Management (Master Admin)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateAgentDialog = true },
                containerColor = GoldPrimary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Create Agent", tint = Color.Black)
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
            item {
                if (uiState is AgentUiState.Success) {
                    Text(
                        text = (uiState as AgentUiState.Success).message,
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (uiState is AgentUiState.Error) {
                    Text(
                        text = (uiState as AgentUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            items(agents) { agent ->
                AgentCard(
                    agent = agent,
                    onTransferCoinsClick = { coinTransferAgent = agent },
                    onBlockAgentClick = { agentViewModel.blockAgentAndTransferCustomers(agent.id) }
                )
            }
        }

        if (showCreateAgentDialog) {
            CreateAgentDialog(
                onDismiss = { showCreateAgentDialog = false },
                onSave = { name, mobile, pass ->
                    agentViewModel.createAgent(name, mobile, pass)
                    showCreateAgentDialog = false
                }
            )
        }

        if (coinTransferAgent != null) {
            AlertDialog(
                onDismissRequest = { coinTransferAgent = null },
                title = { Text("Allocate Coins to Agent ${coinTransferAgent!!.name}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current Agent Wallet: ${coinTransferAgent!!.coinBalance} Coins", fontSize = 13.sp)
                        OutlinedTextField(
                            value = coinTransferAmountText,
                            onValueChange = { coinTransferAmountText = it.filter { c -> c.isDigit() } },
                            label = { Text("Coins Amount (Unlimited Master Source)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = coinTransferAmountText.toLongOrNull() ?: 0L
                            agentViewModel.masterAdminTransferCoins(coinTransferAgent!!.id, amt)
                            coinTransferAgent = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Transfer Coins", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { coinTransferAgent = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun AgentCard(
    agent: Agent,
    onTransferCoinsClick: () -> Unit,
    onBlockAgentClick: () -> Unit
) {
    val isBlocked = agent.status == AgentStatus.BLOCKED

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
                Column {
                    Text(agent.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoldPrimary)
                    Text("Code: ${agent.agentCode} | Mobile: +91 ${agent.mobileNumber}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }

                Badge(containerColor = if (isBlocked) DangerRed else GoldPrimary) {
                    Text(agent.status.name, color = Color.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Wallet Balance: ${agent.coinBalance} Coins | Customers: ${agent.totalCustomers}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTransferCoinsClick,
                    enabled = !isBlocked,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Allocate Coins", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onBlockAgentClick,
                    enabled = !isBlocked,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Block & Transfer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CreateAgentDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Agent Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Agent Name") }, singleLine = true)
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { if (it.length <= 10) mobile = it },
                    label = { Text("Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Agent Password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, mobile, pass) },
                enabled = name.isNotBlank() && mobile.length == 10 && pass.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Create Agent", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
