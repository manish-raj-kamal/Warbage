package com.warbage.recycling.ui.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * High-performance Liquid Glass Segmented Tab Bar inspired by Kyant0's LiquidBottomTabs
 * and Apple's Liquid Glass navigation design.
 */
@Composable
fun LiquidSegmentedTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    icons: List<ImageVector>? = null
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Box(
        modifier = modifier
            .height(52.dp)
            .liquidGlass(
                shape = CircleShape,
                blurRadius = 20.dp,
                lensHeight = 14.dp,
                lensAmount = 16.dp,
                tint = if (isDark) Color(0x380F172A) else Color(0x55FFFFFF),
                elevation = 4.dp
            )
            .padding(4.dp)
    ) {
        // Tab row
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                val interactionSource = remember { MutableInteractionSource() }

                val tabScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.0f else 0.95f,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                    label = "tab_scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(
                            if (isSelected) {
                                Modifier.liquidGlass(
                                    shape = CircleShape,
                                    blurRadius = 12.dp,
                                    lensHeight = 10.dp,
                                    lensAmount = 12.dp,
                                    tint = if (isDark) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                    },
                                    borderAlpha = 0.6f,
                                    elevation = 2.dp
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onTabSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (icons != null && index < icons.size) {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                },
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            }
                        )
                    }
                }
            }
        }
    }
}

