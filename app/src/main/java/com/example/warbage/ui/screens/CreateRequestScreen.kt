package com.example.warbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.RequestEntity
import com.example.warbage.data.repository.LotRepository
import com.example.warbage.data.repository.RequestRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    database: WarbageDatabase,
    lotId: Int,
    recyclerId: Int,
    onRequestCreated: () -> Unit,
    onBack: () -> Unit
) {
    val lot by database.lotDao().getLotById(lotId).collectAsState(initial = null)
    val recycler by database.recyclerDao().getRecyclerById(recyclerId).collectAsState(initial = null)
    val material by remember(lot) {
        val matId = lot?.materialId ?: 0
        database.materialDao().getMaterialById(matId)
    }.collectAsState(initial = null)

    val requestRepository = remember { RequestRepository(database.requestDao()) }
    val lotRepository = remember { LotRepository(database.lotDao()) }
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Request", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (lot != null && recycler != null) {
            val currentLot = lot!!
            val currentRecycler = recycler!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Handover Request",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // Lot info
                InfoCard(
                    title = "Lot",
                    content = currentLot.lotId
                )

                InfoCard(
                    title = "Material",
                    content = material?.let { "${it.icon} ${it.name}" } ?: "Loading..."
                )

                InfoCard(
                    title = "Weight",
                    content = "${currentLot.weight} kg"
                )

                InfoCard(
                    title = "Estimated Value",
                    content = "₹${String.format("%,.0f", currentLot.estimatedMinValue)} – ₹${String.format("%,.0f", currentLot.estimatedMaxValue)}"
                )

                // Recycler info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Recycler",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = currentRecycler.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${currentRecycler.location} • ₹${currentRecycler.pricePerKg}/kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (!isSubmitting) {
                            isSubmitting = true
                            scope.launch {
                                val reqNum = requestRepository.getNextRequestNumber()
                                val request = RequestEntity(
                                    requestId = "REQ-${String.format("%03d", reqNum)}",
                                    lotId = lotId,
                                    recyclerId = recyclerId,
                                    dateCreated = System.currentTimeMillis(),
                                    status = "pending"
                                )
                                requestRepository.insert(request)
                                lotRepository.updateStatus(lotId, "submitted")
                                onRequestCreated()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit Request",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
