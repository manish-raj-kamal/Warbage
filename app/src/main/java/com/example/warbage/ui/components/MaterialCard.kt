package com.example.warbage.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warbage.data.entity.MaterialEntity
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.liquidGlass

@Composable
fun MaterialCard(
    material: MaterialEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    GlassCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Luminous Icon Orb with dual-gradient refraction
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .liquidGlass(
                        shape = CircleShape,
                        blurRadius = 14.dp,
                        lensHeight = 10.dp,
                        lensAmount = 12.dp,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        borderAlpha = 0.5f,
                        elevation = 3.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = material.icon,
                    fontSize = 28.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = material.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = material.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // High-precision price pill
                Box(
                    modifier = Modifier
                        .liquidGlass(
                            shape = RoundedCornerShape(10.dp),
                            blurRadius = 10.dp,
                            lensHeight = 6.dp,
                            lensAmount = 8.dp,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            borderAlpha = 0.4f,
                            elevation = 1.dp
                        )
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "₹${material.minPrice} – ₹${material.maxPrice} / ${material.unit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Right chevron indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .liquidGlass(
                        shape = CircleShape,
                        blurRadius = 8.dp,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        borderAlpha = 0.2f,
                        elevation = 0.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Details",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
