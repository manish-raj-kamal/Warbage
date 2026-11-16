package com.example.warbage.ui.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * An Apple & Kyant0-inspired Liquid Glass Card with physical refraction,
 * soft ambient blur, glowing rim, and subtle spring bounce on press.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    tint: Color? = null,
    blurRadius: Dp = 24.dp,
    lensHeight: Dp = 18.dp,
    lensAmount: Dp = 20.dp,
    elevation: Dp = 6.dp,
    borderAlpha: Float = 0.35f,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "glass_press_scale"
    )

    var cardModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .liquidGlass(
            shape = shape,
            blurRadius = blurRadius,
            lensHeight = lensHeight,
            lensAmount = lensAmount,
            tint = tint,
            borderAlpha = if (isPressed) borderAlpha * 1.5f else borderAlpha,
            elevation = if (isPressed) elevation * 0.5f else elevation
        )

    if (onClick != null) {
        cardModifier = cardModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }

    Box(
        modifier = cardModifier,
        content = content
    )
}

