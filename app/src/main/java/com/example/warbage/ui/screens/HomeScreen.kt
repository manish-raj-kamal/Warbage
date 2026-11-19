package com.example.warbage.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warbage.ui.glass.GlassButton
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.LiquidTopAppBar
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.CyanBright
import com.example.warbage.ui.theme.EmeraldBright
import com.example.warbage.ui.theme.EmeraldNeon
import com.example.warbage.ui.theme.GoldBright
import com.example.warbage.ui.theme.VioletBright

@Composable
fun HomeScreen(
    onMaterialsClick: () -> Unit,
    onPriceBoardClick: () -> Unit,
    onFindRecyclerClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onAiScannerClick: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // High-tech circular brand icon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 14.dp,
                                    tint = EmeraldBright.copy(alpha = 0.22f),
                                    borderAlpha = 0.6f,
                                    elevation = 3.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AllInclusive,
                                contentDescription = "WARBAGE Logo",
                                tint = EmeraldBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "WARBAGE",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Circular E-Waste Exchange",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    // Profile button with active status ring
                    Box(contentAlignment = Alignment.BottomEnd) {
                        GlassIconButton(
                            onClick = onProfileClick,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        // Active green beacon
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .offset(x = (-2).dp, y = (-2).dp)
                                .shadow(4.dp, CircleShape, spotColor = EmeraldNeon)
                                .clip(CircleShape)
                                .background(EmeraldNeon)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // High-Tech Holographic AI Scanner Button
            GlassButton(
                onClick = onAiScannerClick,
                shape = CircleShape,
                tint = VioletBright.copy(alpha = 0.32f),
                elevation = 8.dp
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = VioletBright,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Material Scanner",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // HERO CARD: Value Portfolio & Carbon Offset
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ESTIMATED RECYCLE VALUE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹12,480",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Growth indicator pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 10.dp,
                                    tint = EmeraldBright.copy(alpha = 0.20f),
                                    borderAlpha = 0.45f,
                                    elevation = 1.dp
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = EmeraldBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+14.8%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldBright
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Dual Impact Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Stat 1: Weight Diverted
                        MiniStatPill(
                            icon = Icons.Default.Recycling,
                            value = "38.5 kg",
                            label = "E-Waste Diverted",
                            accent = EmeraldBright,
                            modifier = Modifier.weight(1f)
                        )

                        // Stat 2: Carbon Footprint Saved
                        MiniStatPill(
                            icon = Icons.Default.Co2,
                            value = "16.2 kg",
                            label = "CO₂ Emissions Saved",
                            accent = CyanBright,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // SECTION HEADER
            Text(
                text = "Marketplace Services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )

            // 2x2 Interactive Services Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DashboardServiceCard(
                    title = "E-Waste Catalog",
                    caption = "15+ Certified Materials",
                    icon = Icons.Default.Category,
                    accentColor = EmeraldBright,
                    onClick = onMaterialsClick,
                    modifier = Modifier.weight(1f)
                )
                DashboardServiceCard(
                    title = "Live Price Board",
                    caption = "Daily Commodity Ticker",
                    icon = Icons.Default.QueryStats,
                    accentColor = CyanBright,
                    onClick = onPriceBoardClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DashboardServiceCard(
                    title = "Find Recycler",
                    caption = "Nearby Verified Yards",
                    icon = Icons.Default.NearMe,
                    accentColor = VioletBright,
                    onClick = onFindRecyclerClick,
                    modifier = Modifier.weight(1f)
                )
                DashboardServiceCard(
                    title = "My Orders",
                    caption = "Lots & Handover Status",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    accentColor = GoldBright,
                    onClick = onTransactionsClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // LIVE COMMODITY SNAPSHOT BANNER
            GlassCard(
                onClick = onPriceBoardClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 10.dp,
                                    tint = GoldBright.copy(alpha = 0.18f),
                                    borderAlpha = 0.4f,
                                    elevation = 1.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = null,
                                tint = GoldBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Market Surge: Lithium Cells",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Demand up +8.4% • Up to ₹120/kg",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "View Prices",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp)) // Padding for FAB
        }
    }
}

@Composable
private fun MiniStatPill(
    icon: ImageVector,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .liquidGlass(
                shape = RoundedCornerShape(18.dp),
                blurRadius = 12.dp,
                tint = accent.copy(alpha = 0.12f),
                borderAlpha = 0.35f,
                elevation = 2.dp
            )
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardServiceCard(
    title: String,
    caption: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.92f),
        shape = RoundedCornerShape(26.dp),
        tint = accentColor.copy(alpha = 0.12f),
        elevation = 5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Luminous Icon Beacon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .liquidGlass(
                        shape = CircleShape,
                        blurRadius = 14.dp,
                        lensHeight = 10.dp,
                        lensAmount = 12.dp,
                        tint = accentColor.copy(alpha = 0.22f),
                        borderAlpha = 0.5f,
                        elevation = 3.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = accentColor
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
