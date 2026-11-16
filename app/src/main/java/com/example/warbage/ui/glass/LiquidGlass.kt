package com.example.warbage.ui.glass

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow

val LocalBackdrop = compositionLocalOf<LayerBackdrop?> { null }

/**
 * High-performance Liquid Glass Host that creates the backdrop sampling layer
 * and draws vibrant ambient background lighting (aurora orbs, organic gradients)
 * which liquid glass surfaces refract and distort with optical realism.
 */
@Composable
fun LiquidBackdropHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val backdrop = rememberLayerBackdrop()
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Box(modifier = modifier.fillMaxSize()) {
        // Background layer: registered as sampling source for backdrop effects
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Base background gradient
                    val baseColors = if (isDark) {
                        listOf(
                            Color(0xFF0D1812),
                            Color(0xFF0F1E19),
                            Color(0xFF08120E)
                        )
                    } else {
                        listOf(
                            Color(0xFFF3F8F4),
                            Color(0xFFE8F3EB),
                            Color(0xFFF7FAF8)
                        )
                    }
                    drawRect(
                        brush = Brush.verticalGradient(baseColors)
                    )

                    // Luminous ambient mesh spheres (emerald, teal, mint, amber)
                    // Top-right glowing emerald orb
                    val orbRadius1 = size.width * 0.65f
                    val orbCenter1 = Offset(size.width * 0.9f, size.height * 0.12f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (isDark) {
                                listOf(Color(0x332E7D32), Color(0x1800897B), Color.Transparent)
                            } else {
                                listOf(Color(0x35A5D6A7), Color(0x1580CBC4), Color.Transparent)
                            },
                            center = orbCenter1,
                            radius = orbRadius1
                        ),
                        radius = orbRadius1,
                        center = orbCenter1
                    )

                    // Mid-left glowing teal/cyan orb
                    val orbRadius2 = size.width * 0.55f
                    val orbCenter2 = Offset(size.width * 0.05f, size.height * 0.45f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (isDark) {
                                listOf(Color(0x2800897B), Color(0x101B5E20), Color.Transparent)
                            } else {
                                listOf(Color(0x2580CBC4), Color(0x10C8E6C9), Color.Transparent)
                            },
                            center = orbCenter2,
                            radius = orbRadius2
                        ),
                        radius = orbRadius2,
                        center = orbCenter2
                    )

                    // Bottom-right energetic amber accent orb
                    val orbRadius3 = size.width * 0.5f
                    val orbCenter3 = Offset(size.width * 0.85f, size.height * 0.85f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (isDark) {
                                listOf(Color(0x22FF8F00), Color(0x0CFFB300), Color.Transparent)
                            } else {
                                listOf(Color(0x20FFE082), Color(0x08FFCA28), Color.Transparent)
                            },
                            center = orbCenter3,
                            radius = orbRadius3
                        ),
                        radius = orbRadius3,
                        center = orbCenter3
                    )
                }
                .layerBackdrop(backdrop)
        )

        // Provide the backdrop to any child composables
        CompositionLocalProvider(LocalBackdrop provides backdrop) {
            content()
        }
    }
}

/**
 * Modifier that applies iOS-style Liquid Glass optics (lens refraction, gaussian blur,
 * vibrancy, specular rim highlight, and soft elevation) to any composable.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    blurRadius: Dp = 20.dp,
    lensHeight: Dp = 16.dp,
    lensAmount: Dp = 18.dp,
    tint: Color? = null,
    borderWidth: Dp = 1.dp,
    borderAlpha: Float = 0.35f,
    elevation: Dp = 4.dp
): Modifier = composed {
    val backdrop = LocalBackdrop.current
    val density = LocalDensity.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val defaultTint = tint ?: if (isDark) {
        Color(0xFF1B2C24).copy(alpha = 0.45f)
    } else {
        Color.White.copy(alpha = 0.65f)
    }

    val rimGradient = remember(isDark, borderAlpha) {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = borderAlpha),
                    Color(0xFF80CBC4).copy(alpha = borderAlpha * 0.6f),
                    Color.White.copy(alpha = borderAlpha * 0.1f)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = borderAlpha * 1.5f),
                    Color(0xFF2E7D32).copy(alpha = borderAlpha * 0.25f),
                    Color.White.copy(alpha = borderAlpha * 0.6f)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        }
    }

    val blurRadiusPx = with(density) { blurRadius.toPx() }
    val lensHeightPx = with(density) { lensHeight.toPx() }
    val lensAmountPx = with(density) { lensAmount.toPx() }

    var modifier: Modifier = Modifier

    if (elevation > 0.dp) {
        modifier = modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0x1F2E7D32),
            spotColor = if (isDark) Color(0x3300897B) else Color(0x292E7D32)
        )
    }

    if (backdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        modifier = modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(blurRadiusPx)
                lens(lensHeightPx, lensAmountPx)
            },
            highlight = { Highlight.Ambient },
            shadow = { Shadow.Default },
            onDrawSurface = {
                drawRect(defaultTint)
            }
        )
    } else {
        // Fallback for devices without backdrop or older API
        modifier = modifier
            .clip(shape)
            .background(defaultTint)
    }

    // Specular perimeter highlight border for physical glass feel
    modifier.border(
        width = borderWidth,
        brush = rimGradient,
        shape = shape
    )
}

