package com.warbage.recycling.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warbage.recycling.data.WarbageDatabase
import com.warbage.recycling.ui.glass.GlassButton
import com.warbage.recycling.ui.glass.GlassCard
import com.warbage.recycling.ui.glass.GlassIconButton
import com.warbage.recycling.ui.glass.GlassTextField
import com.warbage.recycling.ui.glass.LiquidTopAppBar
import com.warbage.recycling.ui.glass.liquidGlass
import com.warbage.recycling.ui.theme.CyanBright
import com.warbage.recycling.ui.theme.EmeraldBright
import com.warbage.recycling.ui.theme.GoldBright

@Composable
fun MaterialDetailScreen(
    database: WarbageDatabase,
    materialId: Int,
    onCreateLot: (materialId: Int, weight: Double) -> Unit,
    onBack: () -> Unit
) {
    val material by database.materialDao().getMaterialById(materialId).collectAsState(initial = null)
    var weightText by remember { mutableStateOf("") }
    val weight = weightText.toDoubleOrNull()

    val quickWeights = listOf(1.0, 5.0, 10.0, 25.0)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = { Text("Material Details", fontWeight = FontWeight.Bold) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Material Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
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
                                .size(80.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 18.dp,
                                    tint = EmeraldBright.copy(alpha = 0.22f),
                                    borderAlpha = 0.55f,
                                    elevation = 4.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mat.icon,
                                fontSize = 42.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = mat.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = mat.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Market Price Glass Pill
                        Box(
                            modifier = Modifier
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 12.dp,
                                    tint = EmeraldBright.copy(alpha = 0.16f),
                                    borderAlpha = 0.45f,
                                    elevation = 2.dp
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Market Rate: ₹${mat.minPrice} – ₹${mat.maxPrice} / ${mat.unit}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldBright
                            )
                        }
                    }
                }

                // Weight Input Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Enter Scrap Weight",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GlassTextField(
                            value = weightText,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    weightText = newValue
                                }
                            },
                            label = "Estimated Weight (${mat.unit})",
                            placeholder = "e.g. 5.5",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Weight Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickWeights.forEach { qWeight ->
                                val label = "+${qWeight.toInt()} ${mat.unit}"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .liquidGlass(
                                            shape = CircleShape,
                                            blurRadius = 8.dp,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                            borderAlpha = 0.3f,
                                            elevation = 0.dp
                                        )
                                        .clickable {
                                            val current = weight ?: 0.0
                                            val updated = current + qWeight
                                            weightText = String.format("%.1f", updated)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Estimated Value & Impact Summary Card
                if (weight != null && weight > 0) {
                    val minValue = weight * mat.minPrice
                    val maxValue = weight * mat.maxPrice
                    val co2Saved = weight * 0.42

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        tint = CyanBright.copy(alpha = 0.12f),
                        elevation = 5.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ESTIMATED TOTAL RETURN",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "₹${String.format("%,.0f", minValue)} – ₹${String.format("%,.0f", maxValue)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldBright
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .liquidGlass(
                                            shape = CircleShape,
                                            blurRadius = 10.dp,
                                            tint = CyanBright.copy(alpha = 0.2f),
                                            borderAlpha = 0.4f,
                                            elevation = 1.dp
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Co2,
                                            contentDescription = null,
                                            tint = CyanBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "-${String.format("%.1f", co2Saved)}kg",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanBright
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Create Lot Glass Button
                GlassButton(
                    onClick = {
                        if (weight != null && weight > 0) {
                            onCreateLot(mat.id, weight)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = weight != null && weight > 0,
                    tint = EmeraldBright.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Create Recycling Lot",
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

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
