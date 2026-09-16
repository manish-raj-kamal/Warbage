package com.example.warbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.LotEntity
import com.example.warbage.data.entity.MaterialEntity
import com.example.warbage.data.entity.RequestEntity
import com.example.warbage.ui.components.TransactionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    database: WarbageDatabase,
    onBack: () -> Unit
) {
    val lots by database.lotDao().getAllLots().collectAsState(initial = emptyList())
    val requests by database.requestDao().getAllRequests().collectAsState(initial = emptyList())
    val materials by database.materialDao().getAllMaterials().collectAsState(initial = emptyList())

    val materialMap = remember(materials) { materials.associateBy { it.id } }
    val requestByLot = remember(requests) {
        requests.associateBy { it.lotId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Transactions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (lots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📋",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No transactions yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Create a lot from Materials to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lots, key = { it.id }) { lot ->
                    val materialName = materialMap[lot.materialId]?.name ?: "Unknown"
                    val request = requestByLot[lot.id]
                    TransactionCard(
                        lot = lot,
                        request = request,
                        materialName = materialName
                    )
                }
            }
        }
    }
}
