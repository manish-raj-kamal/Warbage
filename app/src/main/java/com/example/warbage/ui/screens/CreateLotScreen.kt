package com.example.warbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.LotEntity
import com.example.warbage.data.repository.LotRepository
import com.example.warbage.ui.glass.GlassButton
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.LiquidTopAppBar
import com.example.warbage.ui.glass.liquidGlass
import kotlinx.coroutines.launch

@Composable
fun CreateLotScreen(
    database: WarbageDatabase,
    materialId: Int,
    weight: Double,
    onLotCreated: (lotId: Int) -> Unit,
    onBack: () -> Unit
) {
    val material by database.materialDao().getMaterialById(materialId).collectAsState(initial = null)
    val lotRepository = remember { LotRepository(database.lotDao()) }
    val scope = rememberCoroutineScope()
    var isCreating by remember { mutableStateOf(false) }
    var lotNumber by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        lotNumber = lotRepository.getNextLotNumber()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = { Text("Create Lot", fontWeight = FontWeight.Bold) },
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
        material?.let { mat ->
            val minValue = weight * mat.minPrice
            val maxValue = weight * mat.maxPrice
            val lotIdStr = lotNumber?.let { "LOT-${String.format("%04d", it)}" } ?: "..."

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Lot Preview Glass Card
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
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                    borderAlpha = 0.5f,
                                    elevation = 2.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = lotIdStr,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "New E-Waste Lot",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Details Glass Rows
                DetailGlassRow("Material", "${mat.icon} ${mat.name}")
                DetailGlassRow("Weight", "$weight ${mat.unit}")
                DetailGlassRow("Estimated Value", "₹${String.format("%,.0f", minValue)} – ₹${String.format("%,.0f", maxValue)}")
                DetailGlassRow("Status", "📦 Created")

                Spacer(modifier = Modifier.weight(1f))

                GlassButton(
                    onClick = {
                        if (!isCreating && lotNumber != null) {
                            isCreating = true
                            scope.launch {
                                val lot = LotEntity(
                                    lotId = lotIdStr,
                                    materialId = materialId,
                                    weight = weight,
                                    estimatedMinValue = minValue,
                                    estimatedMaxValue = maxValue,
                                    description = "",
                                    dateCreated = System.currentTimeMillis(),
                                    status = "created"
                                )
                                val id = lotRepository.insert(lot)
                                onLotCreated(id.toInt())
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isCreating && lotNumber != null,
                    isLoading = isCreating,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Confirm & Find Recycler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailGlassRow(label: String, value: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
