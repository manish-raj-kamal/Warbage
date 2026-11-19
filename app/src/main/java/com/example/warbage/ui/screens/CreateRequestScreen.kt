package com.example.warbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.RequestEntity
import com.example.warbage.data.repository.LotRepository
import com.example.warbage.data.repository.RequestRepository
import com.example.warbage.ui.glass.GlassButton
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.LiquidTopAppBar
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.CyanBright
import com.example.warbage.ui.theme.EmeraldBright
import kotlinx.coroutines.launch

@Composable
fun CreateRequestScreen(
    database: WarbageDatabase,
    lotId: Int,
    recyclerId: Int,
    onRequestCreated: () -> Unit,
    onBack: () -> Unit
) {
    val currentUser by database.userDao().getLastActiveUser().collectAsState(initial = null)
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
                title = { Text("Handover Request", fontWeight = FontWeight.Bold) },
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Handover Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Lot details in clean cards
                InfoGlassCard("Lot Identifier", currentLot.lotId)
                InfoGlassCard("Material Type", material?.let { "${it.icon} ${it.name}" } ?: "Loading...")
                InfoGlassCard("Batch Weight", "${currentLot.weight} kg")
                InfoGlassCard("Valuation Range", "₹${String.format("%,.0f", currentLot.estimatedMinValue)} – ₹${String.format("%,.0f", currentLot.estimatedMaxValue)}")

                // Target Recycler Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    tint = CyanBright.copy(alpha = 0.14f),
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 10.dp,
                                    tint = CyanBright.copy(alpha = 0.22f),
                                    borderAlpha = 0.5f,
                                    elevation = 2.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = CyanBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentRecycler.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = EmeraldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${currentRecycler.location} • Rate: ₹${currentRecycler.pricePerKg}/kg",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Submit Action Button
                GlassButton(
                    onClick = {
                        if (!isSubmitting) {
                            isSubmitting = true
                            scope.launch {
                                val reqNum = requestRepository.getNextRequestNumber()
                                val request = RequestEntity(
                                    userId = currentUser?.uid ?: "",
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
                        .height(54.dp),
                    enabled = !isSubmitting,
                    isLoading = isSubmitting,
                    tint = EmeraldBright.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Pickup Request",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
                CircularProgressIndicator(color = EmeraldBright)
            }
        }
    }
}

@Composable
private fun InfoGlassCard(title: String, content: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = content,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
