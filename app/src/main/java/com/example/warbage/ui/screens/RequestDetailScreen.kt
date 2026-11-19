package com.example.warbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.repository.LotRepository
import com.example.warbage.data.repository.RequestRepository
import com.example.warbage.ui.components.StatusBadge
import com.example.warbage.ui.glass.GlassButton
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.LiquidTopAppBar
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.CyanBright
import com.example.warbage.ui.theme.EmeraldBright
import android.util.Log
import com.example.warbage.network.BackendTransaction
import com.example.warbage.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RequestDetailScreen(
    database: WarbageDatabase,
    requestId: Int,
    onBack: () -> Unit
) {
    val request by database.requestDao().getRequestById(requestId).collectAsState(initial = null)
    val lots by database.lotDao().getAllLots().collectAsState(initial = emptyList())
    val materials by database.materialDao().getAllMaterials().collectAsState(initial = emptyList())
    val recyclers by database.recyclerDao().getAllRecyclers().collectAsState(initial = emptyList())

    val requestRepository = remember { RequestRepository(database.requestDao()) }
    val lotRepository = remember { LotRepository(database.lotDao()) }
    val scope = rememberCoroutineScope()

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = { Text("Request Inspection", fontWeight = FontWeight.Bold) },
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
        request?.let { req ->
            val lot = lots.find { it.id == req.lotId }
            val material = lot?.let { l -> materials.find { it.id == l.materialId } }
            val recycler = recyclers.find { it.id == req.recyclerId }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Request Hero Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 14.dp,
                                    tint = CyanBright.copy(alpha = 0.2f),
                                    borderAlpha = 0.5f,
                                    elevation = 2.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = CyanBright,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = req.requestId,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusBadge(status = req.status)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dateFormat.format(Date(req.dateCreated)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Lot details glass cards
                if (lot != null) {
                    DetailGlassInfoCard("Lot Number", lot.lotId)
                    DetailGlassInfoCard("Classified Material", material?.let { "${it.icon} ${it.name}" } ?: "Scrap")
                    DetailGlassInfoCard("Verified Weight", "${lot.weight} kg")
                    DetailGlassInfoCard(
                        "Estimated Payout",
                        "₹${String.format("%,.0f", lot.estimatedMinValue)} – ₹${String.format("%,.0f", lot.estimatedMaxValue)}"
                    )
                }

                val collectorName = lot?.collectorName?.ifEmpty { req.collectorName }?.ifEmpty { "Eco Collector" } ?: "Eco Collector"
                val collectorContact = lot?.collectorPhone?.ifEmpty { lot?.collectorEmail }?.ifEmpty { req.collectorPhone } ?: ""
                val facilityName = lot?.recyclerName?.ifEmpty { recycler?.name } ?: "Certified Recycler"
                val facilityLocation = lot?.recyclerLocation?.ifEmpty { recycler?.location } ?: "Facility"
                val facilityRate = if ((lot?.recyclerPricePerKg ?: 0.0) > 0) lot?.recyclerPricePerKg?.toInt() else recycler?.pricePerKg ?: 0

                DetailGlassInfoCard("Collector Name", collectorName)
                if (collectorContact.isNotEmpty()) {
                    DetailGlassInfoCard("Collector Contact", collectorContact)
                }
                DetailGlassInfoCard("Processing Facility", "$facilityName • $facilityLocation")
                if (facilityRate != null && facilityRate > 0) {
                    DetailGlassInfoCard("Agreed Recycler Rate", "₹$facilityRate/kg")
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action buttons based on status
                when (req.status) {
                    "pending" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassButton(
                                onClick = {
                                    scope.launch {
                                        requestRepository.updateStatus(req.id, "rejected")
                                        lot?.let { lotRepository.updateStatus(it.id, "rejected") }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reject", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }

                            GlassButton(
                                onClick = {
                                    scope.launch {
                                        requestRepository.updateStatus(req.id, "accepted")
                                        lot?.let { lotRepository.updateStatus(it.id, "accepted") }
                                    }
                                },
                                modifier = Modifier.weight(1.2f),
                                tint = EmeraldBright.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Accept Lot", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    "accepted" -> {
                        GlassButton(
                            onClick = {
                                scope.launch {
                                    requestRepository.updateStatus(req.id, "completed")
                                    lot?.let { lotRepository.updateStatus(it.id, "completed") }

                                    // Record transaction in MongoDB backend
                                    try {
                                        val weight = lot?.weight ?: 1.0
                                        val price = recycler?.pricePerKg?.toDouble() ?: 100.0
                                        val total = weight * price
                                        val tx = BackendTransaction(
                                            transactionId = "TX-${req.requestId}",
                                            lotId = lot?.lotId,
                                            requestId = req.requestId,
                                            collectorId = req.userId.ifEmpty { lot?.userId ?: "" },
                                            recyclerId = req.recyclerId.toString(),
                                            materialId = lot?.materialId ?: 1,
                                            weightKg = weight,
                                            totalPrice = total,
                                            status = "COMPLETED",
                                            notes = "Handover completed for request ${req.requestId}",
                                            transactionDate = System.currentTimeMillis()
                                        )
                                        RetrofitClient.api.createTransaction(tx)
                                        Log.d("RequestDetail", "Successfully created transaction for ${req.requestId}")
                                    } catch (e: Exception) {
                                        Log.e("RequestDetail", "Failed to create transaction on backend", e)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            tint = EmeraldBright.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mark Handover as Completed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    "completed" -> {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            tint = EmeraldBright.copy(alpha = 0.18f),
                            elevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldBright
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Handover & Settlement Completed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldBright
                                )
                            }
                        }
                    }
                    "rejected" -> {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            elevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "This request was rejected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = EmeraldBright)
        }
    }
}

@Composable
private fun DetailGlassInfoCard(label: String, value: String) {
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
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
