package com.example.warbage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.MaterialEntity
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.LiquidTopAppBar
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.CyanBright
import com.example.warbage.ui.theme.EmeraldBright
import com.example.warbage.ui.theme.EmeraldNeon
import com.example.warbage.ui.theme.GoldBright
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.warbage.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun PriceBoardScreen(
    database: WarbageDatabase,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val materials by database.materialDao().getAllMaterials().collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val materialRepo = com.example.warbage.data.repository.MaterialRepository(database.materialDao())
                materialRepo.syncWithBackend()
            } catch (e: Exception) {
                android.util.Log.e("PriceBoard", "Failed to fetch live prices", e)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = { Text("Commodity Rates", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // High-Tech Exchange Banner Card
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .liquidGlass(
                                            shape = CircleShape,
                                            blurRadius = 10.dp,
                                            tint = CyanBright.copy(alpha = 0.2f),
                                            borderAlpha = 0.45f,
                                            elevation = 2.dp
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueryStats,
                                        contentDescription = null,
                                        tint = CyanBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "E-Waste Spot Market",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Official Yard Valuation Indices",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Live pulse beacon pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .liquidGlass(
                                        shape = CircleShape,
                                        blurRadius = 8.dp,
                                        tint = EmeraldBright.copy(alpha = 0.16f),
                                        borderAlpha = 0.4f,
                                        elevation = 1.dp
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .shadow(4.dp, CircleShape, spotColor = EmeraldNeon)
                                        .clip(CircleShape)
                                        .background(EmeraldNeon)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE FEED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = EmeraldBright
                                )
                            }
                        }
                    }
                }
            }

            items(materials, key = { it.id }) { material ->
                PriceGlassRow(material)
            }
        }
    }
}

@Composable
private fun PriceGlassRow(material: MaterialEntity) {
    // Generate deterministic trend for each material ID
    val trendDelta = when (material.id % 4) {
        0 -> "+5.4%"
        1 -> "+2.8%"
        2 -> "-0.9%"
        else -> "+4.1%"
    }
    val isPositive = !trendDelta.startsWith("-")

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Material Icon & Title
            Row(
                modifier = Modifier.weight(1.1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .liquidGlass(
                            shape = CircleShape,
                            blurRadius = 12.dp,
                            tint = EmeraldBright.copy(alpha = 0.16f),
                            borderAlpha = 0.45f,
                            elevation = 1.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = material.icon,
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = material.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "per ${material.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Market Range & Trend Delta
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${material.minPrice} – ₹${material.maxPrice}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldBright
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (isPositive) EmeraldBright else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = trendDelta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) EmeraldBright else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
