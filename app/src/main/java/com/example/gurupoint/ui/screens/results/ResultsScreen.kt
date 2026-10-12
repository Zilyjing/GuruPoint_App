package com.example.gurupoint.ui.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gurupoint.data.model.ResultItem
import com.example.gurupoint.ui.theme.GoldPrimary
import com.example.gurupoint.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val results by mainViewModel.results.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredResults = remember(results, searchQuery) {
        if (searchQuery.isBlank()) results
        else results.filter { it.gameName.contains(searchQuery, ignoreCase = true) || (it.declaredNumber ?: "").contains(searchQuery) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Results History", fontWeight = FontWeight.Bold) },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Game or Result") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredResults) { item ->
                    ResultCard(item = item)
                }
            }
        }
    }
}

@Composable
fun ResultCard(item: ResultItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.gameName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoldPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Date: ${item.resultDate} | Time: ${item.resultTime}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                if (!item.sourceName.isNull_or_blank()) {
                    Text("Source: ${item.sourceName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            Text(
                text = item.declaredNumber ?: "Awaiting",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = if (item.declaredNumber != null) GoldPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun String?.isNull_or_blank(): Boolean {
    return this.isNullOrBlank()
}
