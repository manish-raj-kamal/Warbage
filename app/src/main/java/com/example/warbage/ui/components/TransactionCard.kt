package com.example.warbage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warbage.data.entity.LotEntity
import com.example.warbage.data.entity.RequestEntity
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.CyanBright
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionCard(
    lot: LotEntity,
    request: RequestEntity?,
    materialName: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val status = request?.status ?: lot.status
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    val recyclerName = lot.recyclerName.ifEmpty { request?.recyclerName ?: "" }
    val recyclerLocation = lot.recyclerLocation.ifEmpty { request?.recyclerLocation ?: "" }
    val recyclerPrice = if (lot.recyclerPricePerKg > 0) lot.recyclerPricePerKg else (request?.pricePerKg ?: 0.0)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
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
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                borderAlpha = 0.4f,
                                elevation = 1.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = lot.lotId,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateFormat.format(Date(lot.dateCreated)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Material and weight details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = materialName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Total Weight: ${lot.weight} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Estimated payout capsule
                Box(
                    modifier = Modifier
                        .liquidGlass(
                            shape = RoundedCornerShape(12.dp),
                            blurRadius = 10.dp,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            borderAlpha = 0.45f,
                            elevation = 1.dp
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "₹${String.format("%,.0f", lot.estimatedMinValue)} – ₹${String.format("%,.0f", lot.estimatedMaxValue)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Recycler Information visible to Collector
            if (recyclerName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = "Recycler Facility",
                            tint = CyanBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = recyclerName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (recyclerLocation.isNotEmpty() || recyclerPrice > 0) {
                        Text(
                            text = buildString {
                                if (recyclerLocation.isNotEmpty()) append(recyclerLocation)
                                if (recyclerPrice > 0) {
                                    if (isNotEmpty()) append(" • ")
                                    append("₹${recyclerPrice.toInt()}/kg")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
