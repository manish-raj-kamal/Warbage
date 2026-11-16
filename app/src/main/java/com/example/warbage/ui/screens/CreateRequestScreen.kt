package com.example.warbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.RequestEntity
import com.example.warbage.data.repository.LotRepository
import com.example.warbage.data.repository.RequestRepository
import com.example.warbage.ui.glass.GlassButton
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.LiquidTopAppBar
import kotlinx.coroutines.launch

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
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = { Text("Submit Request", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    GlassIconButton(
                        onClick = onBack,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                    text = "Handover Summary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Lot info glass cards
                InfoGlassCard("Lot Identifier", currentLot.lotId)
                InfoGlassCard("Material", material?.let { "${it.icon} ${it.name}" } ?: "Loading...")
                InfoGlassCard("Weight", "${currentLot.weight} kg")
                InfoGlassCard("Estimated Value", "₹${String.format("%,.0f", currentLot.estimatedMinValue)} – ₹${String.format("%,.0f", currentLot.estimatedMaxValue)}")

                // Recycler Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Target Recycler",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentRecycler.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currentRecycler.location} • ₹${currentRecycler.pricePerKg}/kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                GlassButton(
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
                    enabled = !isSubmitting,
                    isLoading = isSubmitting,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Request",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun InfoGlassCard(title: String, content: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
