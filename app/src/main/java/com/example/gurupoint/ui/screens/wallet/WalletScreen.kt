package com.example.gurupoint.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurupoint.data.model.CoinTransaction
import com.example.gurupoint.data.model.TransactionType
import com.example.gurupoint.ui.theme.DangerRed
import com.example.gurupoint.ui.theme.GoldPrimary
import com.example.gurupoint.ui.theme.SuccessGreen
import com.example.gurupoint.viewmodel.AuthViewModel
import com.example.gurupoint.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val user by authViewModel.currentUser.collectAsState()
    val transactions by mainViewModel.transactions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Virtual Coin Wallet", fontWeight = FontWeight.Bold) },
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
            // Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = GoldPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Current Virtual Coins", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(
                            text = "${user?.coinBalance ?: 0} Coins",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Legal Virtual Coins Note
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Virtual Entertainment Coins only. No real money deposit, withdrawal, cash conversion, or prizes.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Ledger Title
            item {
                Text(
                    text = "Immutable Transaction History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Transactions List
            items(transactions) { tx ->
                TransactionCard(tx = tx)
            }
        }
    }
}

@Composable
fun TransactionCard(tx: CoinTransaction) {
    val isCredit = tx.type == TransactionType.ADD || tx.type == TransactionType.REWARD || tx.type == TransactionType.REFERRAL || tx.type == TransactionType.BONUS
    val amountColor = if (isCredit) SuccessGreen else DangerRed
    val prefix = if (isCredit) "+" else "-"

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
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.reason, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Type: ${tx.type} | ${tx.dateTime}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("Before: ${tx.balanceBefore} → After: ${tx.balanceAfter}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Text(
                text = "$prefix${tx.amount} Coins",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = amountColor
            )
        }
    }
}
