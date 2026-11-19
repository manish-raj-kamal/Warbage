package com.example.warbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
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
import com.example.warbage.data.repository.LotRepository
import com.example.warbage.ui.components.StatusBadge
import com.example.warbage.ui.glass.GlassButton
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.LiquidTopAppBar
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.CyanBright
import com.example.warbage.ui.theme.EmeraldBright
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
    val currentUser by database.userDao().getLastActiveUser().collectAsState(initial = null)
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
                title = { Text("Create Recycling Lot", fontWeight = FontWeight.Bold) },
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                .size(72.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 16.dp,
                                    tint = EmeraldBright.copy(alpha = 0.22f),
                                    borderAlpha = 0.55f,
                                    elevation = 3.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = EmeraldBright
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = lotIdStr,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Verified E-Waste Batch",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Details Glass Rows
                DetailGlassRow("Target Material", "${mat.icon} ${mat.name}")
                DetailGlassRow("Total Weight", "$weight ${mat.unit}")
                DetailGlassRow("Estimated Value Range", "₹${String.format("%,.0f", minValue)} – ₹${String.format("%,.0f", maxValue)}")

                // Status Row with modern status badge
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
                            text = "Batch Status",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatusBadge(status = "created")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                GlassButton(
                    onClick = {
                        if (!isCreating && lotNumber != null) {
                            isCreating = true
                            scope.launch {
                                val lot = LotEntity(
                                    userId = currentUser?.uid ?: "",
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
                        .height(54.dp),
                    enabled = !isCreating && lotNumber != null,
                    isLoading = isCreating,
                    tint = EmeraldBright.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Confirm & Select Recycler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
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
