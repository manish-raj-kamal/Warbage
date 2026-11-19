package com.example.warbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.AccountCircle
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
import com.example.warbage.data.entity.LotEntity
import com.example.warbage.data.entity.MaterialEntity
import com.example.warbage.data.entity.RequestEntity
import com.example.warbage.ui.components.StatusBadge
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.LiquidSegmentedTabs
import com.example.warbage.ui.glass.LiquidTopAppBar
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.CyanBright
import com.example.warbage.ui.theme.EmeraldBright
import com.example.warbage.ui.theme.GoldBright
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecyclerDashboardScreen(
    database: WarbageDatabase,
    onRequestClick: (requestId: Int) -> Unit,
    onProfileClick: () -> Unit = {}
) {
    val currentUser by database.userDao().getLastActiveUser().collectAsState(initial = null)
    val recycler by database.recyclerDao().getRecyclerByName(currentUser?.displayName ?: "").collectAsState(initial = null)
    val recyclerId = recycler?.id ?: -1

    val requests by database.requestDao().getPendingRequestsForRecycler(recyclerId).collectAsState(initial = emptyList())
    val allRequests by database.requestDao().getAllRequestsForRecycler(recyclerId).collectAsState(initial = emptyList())
    val lots by database.lotDao().getAllLots().collectAsState(initial = emptyList())
    val materials by database.materialDao().getAllMaterials().collectAsState(initial = emptyList())

    val lotMap = remember(lots) { lots.associateBy { it.id } }
    val materialMap = remember(materials) { materials.associateBy { it.id } }

    var selectedTab by remember { mutableIntStateOf(0) }
    val displayRequests = if (selectedTab == 0) requests else allRequests

    // Calculate total pending weight & value
    val totalPendingWeight = remember(requests, lotMap) {
        requests.sumOf { req -> lotMap[req.lotId]?.weight ?: 0.0 }
    }
    val totalPendingValue = remember(requests, lotMap) {
        requests.sumOf { req -> lotMap[req.lotId]?.estimatedMaxValue ?: 0.0 }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 12.dp,
                                    tint = CyanBright.copy(alpha = 0.22f),
                                    borderAlpha = 0.5f,
                                    elevation = 2.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Factory,
                                contentDescription = null,
                                tint = CyanBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Recycler Terminal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Certified Processing Yard",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    GlassIconButton(
                        onClick = onProfileClick,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
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
            // Enhanced Hero Dashboard Visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .liquidGlass(
                        shape = RoundedCornerShape(26.dp),
                        blurRadius = 14.dp,
                        tint = CyanBright.copy(alpha = 0.12f),
                        borderAlpha = 0.5f,
                        elevation = 6.dp
                    )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = "Good to see you, ${currentUser?.displayName?.substringBefore(" ") ?: "Partner"}!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Here is your facility's live processing overview.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // 2x2 Grid for Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Metric 1: Pending Requests
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .liquidGlass(
                                    shape = RoundedCornerShape(16.dp),
                                    blurRadius = 8.dp,
                                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                    borderAlpha = 0.3f,
                                    elevation = 1.dp
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = GoldBright, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${requests.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Pending Intake", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Metric 2: Total Weight
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .liquidGlass(
                                    shape = RoundedCornerShape(16.dp),
                                    blurRadius = 8.dp,
                                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                    borderAlpha = 0.3f,
                                    elevation = 1.dp
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Icon(Icons.Default.Scale, contentDescription = null, tint = CyanBright, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${String.format("%.1f", totalPendingWeight)}kg", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CyanBright)
                                Text("Queue Weight", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Metric 3: Total Completed
                        val completedCount = allRequests.count { it.status == "completed" }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .liquidGlass(
                                    shape = RoundedCornerShape(16.dp),
                                    blurRadius = 8.dp,
                                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                    borderAlpha = 0.3f,
                                    elevation = 1.dp
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldBright, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("$completedCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Completed", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Metric 4: Estimated Value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .liquidGlass(
                                    shape = RoundedCornerShape(16.dp),
                                    blurRadius = 8.dp,
                                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                    borderAlpha = 0.3f,
                                    elevation = 1.dp
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = EmeraldBright, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("₹${String.format("%,.0f", totalPendingValue)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = EmeraldBright)
                                Text("Est. Queue Value", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Liquid Segmented Tabs
            LiquidSegmentedTabs(
                tabs = listOf("Pending Queue (${requests.size})", "All Logs (${allRequests.size})"),
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            )

            if (displayRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(26.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .liquidGlass(
                                        shape = CircleShape,
                                        blurRadius = 14.dp,
                                        tint = CyanBright.copy(alpha = 0.16f),
                                        borderAlpha = 0.4f,
                                        elevation = 2.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Default.Inbox else Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = CyanBright,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (selectedTab == 0) "Queue is Empty" else "No Handover Records",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "New pickup and delivery requests from collectors will stream here automatically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(displayRequests, key = { it.id }) { request ->
                        val lot = lotMap[request.lotId]
                        val material = lot?.let { materialMap[it.materialId] }

                        RequestGlassCard(
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
private fun RequestGlassCard(
    request: RequestEntity,
    lot: LotEntity?,
    material: MaterialEntity?,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .liquidGlass(
                                shape = CircleShape,
                                blurRadius = 10.dp,
                                tint = CyanBright.copy(alpha = 0.18f),
                                borderAlpha = 0.45f,
                                elevation = 1.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = CyanBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = request.requestId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusBadge(status = request.status)
            }

            if (lot != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${material?.icon ?: "📦"} ${material?.name ?: "E-Waste Item"}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Lot ID: ${lot.lotId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Weight & Value Pill
                    Box(
                        modifier = Modifier
                            .liquidGlass(
                                shape = RoundedCornerShape(12.dp),
                                blurRadius = 10.dp,
                                tint = EmeraldBright.copy(alpha = 0.16f),
                                borderAlpha = 0.4f,
                                elevation = 1.dp
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${lot.weight} kg • ₹${String.format("%,.0f", lot.estimatedMaxValue)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldBright
                        )
                    }
                }
            }

            // Collector Information visible to Recycler
            val collectorName = lot?.collectorName?.ifEmpty { request.collectorName }?.ifEmpty { "Eco Collector" } ?: "Eco Collector"
            val collectorContact = lot?.collectorPhone?.ifEmpty { lot.collectorEmail }?.ifEmpty { request.collectorPhone } ?: ""

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Collector",
                        tint = EmeraldBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Collector: $collectorName",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (collectorContact.isNotEmpty()) {
                    Text(
                        text = collectorContact,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(request.dateCreated)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Review & Act",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = CyanBright,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}
