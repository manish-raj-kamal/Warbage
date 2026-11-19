package com.warbage.recycling.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warbage.recycling.data.WarbageDatabase
import com.warbage.recycling.ui.components.TransactionCard
import com.warbage.recycling.ui.glass.GlassCard
import com.warbage.recycling.ui.glass.GlassIconButton
import com.warbage.recycling.ui.glass.LiquidTopAppBar
import com.warbage.recycling.ui.glass.liquidGlass
import com.warbage.recycling.ui.theme.EmeraldBright

@Composable
fun TransactionListScreen(
    database: WarbageDatabase,
    onBack: () -> Unit
) {
    val currentUser by database.userDao().getLastActiveUser().collectAsState(initial = null)
    val lots by database.lotDao().getLotsByUser(currentUser?.uid ?: "").collectAsState(initial = emptyList())
    val requests by database.requestDao().getRequestsByUser(currentUser?.uid ?: "").collectAsState(initial = emptyList())
    val materials by database.materialDao().getAllMaterials().collectAsState(initial = emptyList())

    val materialMap = remember(materials) { materials.associateBy { it.id } }
    val requestByLot = remember(requests) {
        requests.associateBy { it.lotId }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = { Text("My Transactions & Lots", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    GlassIconButton(
                        onClick = onBack,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (lots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    shape = RoundedCornerShape(28.dp),
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
                                    tint = EmeraldBright.copy(alpha = 0.16f),
                                    borderAlpha = 0.45f,
                                    elevation = 2.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = EmeraldBright,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Transactions Recorded",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scan or select a material from the catalog to create your first recycling lot.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(lots, key = { it.id }) { lot ->
                    val material = materialMap[lot.materialId]
                    val request = requestByLot[lot.id]
                    TransactionCard(
                        lot = lot,
                        request = request,
                        materialName = material?.let { "${it.icon} ${it.name}" } ?: "Material"
                    )
                }
            }
        }
    }
}
