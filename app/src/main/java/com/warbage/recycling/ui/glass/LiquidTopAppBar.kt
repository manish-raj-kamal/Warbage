package com.warbage.recycling.ui.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Liquid Glass Top App Bar that smoothly blurs and refracts background content
 * scrolling beneath it, featuring rim highlights and optional navigation/action slots.
 */
@Composable
fun LiquidTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    floating: Boolean = false,
    tint: Color? = null
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val shape = if (floating) {
        RoundedCornerShape(22.dp)
    } else {
        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    }

    val barModifier = if (floating) {
        modifier
            .padding(top = statusBarPadding + 8.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth()
    } else {
        modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    }

    Box(
        modifier = barModifier.liquidGlass(
            shape = shape,
            blurRadius = 24.dp,
            lensHeight = 16.dp,
            lensAmount = 18.dp,
            tint = tint,
            elevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (floating) 0.dp else statusBarPadding)
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (navigationIcon != null) {
                    navigationIcon()
                    Spacer(modifier = Modifier.width(12.dp))
                }
                ProvideTextStyle(
                    value = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    title()
                }
            }

            if (actions != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }
    }
}

