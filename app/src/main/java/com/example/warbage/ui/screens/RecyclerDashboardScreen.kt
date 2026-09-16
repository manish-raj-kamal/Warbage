package com.example.warbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.warbage.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecyclerDashboardScreen(
    database: WarbageDatabase,
    onRequestClick: (requestId: Int) -> Unit,
    onSwitchRole: () -> Unit
) {
    val requests by database.requestDao().getPendingRequests().collectAsState(initial = emptyList())
    val allRequests by database.requestDao().getAllRequests().collectAsState(initial = emptyList())
    val lots by database.lotDao().getAllLots().collectAsState(initial = emptyList())
    val materials by database.materialDao().getAllMaterials().collectAsState(initial = emptyList())

    val lotMap = remember(lots) { lots.associateBy { it.id } }
    val materialMap = remember(materials) { materials.associateBy { it.id } }

    var selectedTab by remember { mutableIntStateOf(0) }
    val displayRequests = if (selectedTab == 0) requests else allRequests

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recycler Dashboard",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSwitchRole) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Role"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text("Pending (${requests.size})")
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("All (${allRequests.size})") }
                )
            }

            if (displayRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedTab == 0) "📭" else "📋",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTab == 0) "No pending requests" else "No requests yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayRequests, key = { it.id }) { request ->
                        val lot = lotMap[request.lotId]
                        val material = lot?.let { materialMap[it.materialId] }

                        RequestListCard(
                            request = request,
                            lot = lot,
                            material = material,
                            onClick = { onRequestClick(request.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestListCard(
    request: RequestEntity,
    lot: LotEntity?,
    material: MaterialEntity?,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.requestId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = request.status)
            }
            if (lot != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${lot.lotId} • ${material?.name ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${lot.weight} kg • ₹${String.format("%,.0f", lot.estimatedMinValue)}–₹${String.format("%,.0f", lot.estimatedMaxValue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateFormat.format(Date(request.dateCreated)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
