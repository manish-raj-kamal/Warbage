package com.warbage.recycling.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * High-performance Liquid Glass Host that creates the vibrant ambient 
 * cyber-aurora background lighting (emerald, cyan, iris, solar gold).
 * Completely eliminates third-party RenderNode artifacts.
 */
@Composable
fun LiquidBackdropHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Box(modifier = modifier.fillMaxSize()) {
        // Sampling canvas layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Base canvas gradient
                    val baseColors = if (isDark) {
                        listOf(
                            Color(0xFF080D1A),
                            Color(0xFF0B1324),
                            Color(0xFF050811)
                        )
                    } else {
                        listOf(
                            Color(0xFFF0F4FA),
                            Color(0xFFE8EEF6),
                            Color(0xFFF5F7FB)
                        )
                    }
                    drawRect(brush = Brush.verticalGradient(baseColors))

                    // Luminous ambient aurora spheres
                    // 1. Top-Right: Bioluminescent Neo-Mint & Cyan Orb
                    val orbRadius1 = size.width * 0.75f
                    val orbCenter1 = Offset(size.width * 0.92f, size.height * 0.10f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0x4000F59B),
                                    Color(0x2200E5FF),
                                    Color(0x0806B6D4),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Color(0x5599F6E4),
                                    Color(0x38A5F3FC),
                                    Color(0x1006B6D4),
                                    Color.Transparent
                                )
                            },
                            center = orbCenter1,
                            radius = orbRadius1
                        ),
                        radius = orbRadius1,
                        center = orbCenter1
                    )

                    // 2. Mid-Left: Quantum Iris & Electric Violet Glow
                    val orbRadius2 = size.width * 0.65f
                    val orbCenter2 = Offset(size.width * 0.05f, size.height * 0.42f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0x358B5CF6),
                                    Color(0x166366F1),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Color(0x40DDD6FE),
                                    Color(0x28C7D2FE),
                                    Color.Transparent
                                )
                            },
                            center = orbCenter2,
                            radius = orbRadius2
                        ),
                        radius = orbRadius2,
                        center = orbCenter2
                    )

                    // 3. Bottom-Right: Solar Gold & Cyber Copper Glow
                    val orbRadius3 = size.width * 0.60f
                    val orbCenter3 = Offset(size.width * 0.88f, size.height * 0.82f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0x30F59E0B),
                                    Color(0x1200F59B),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Color(0x40FDE68A),
                                    Color(0x20FBBF24),
                                    Color.Transparent
                                )
                            },
                            center = orbCenter3,
                            radius = orbRadius3
                        ),
                        radius = orbRadius3,
                        center = orbCenter3
                    )

                    // 4. Center-Bottom: Deep Azure Focus
                    val orbRadius4 = size.width * 0.50f
                    val orbCenter4 = Offset(size.width * 0.25f, size.height * 0.95f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0x2000E5FF),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Color(0x30BAE6FD),
                                    Color(0x1567E8F9),
                                    Color.Transparent
                                )
                            },
                            center = orbCenter4,
                            radius = orbRadius4
                        ),
                        radius = orbRadius4,
                        center = orbCenter4
                    )

                    // 5. Light mode only: subtle top-left mint wash for richness
                    if (!isDark) {
                        val orbRadius5 = size.width * 0.55f
                        val orbCenter5 = Offset(size.width * 0.15f, size.height * 0.08f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x30A7F3D0),
                                    Color(0x1510B981),
                                    Color.Transparent
                                ),
                                center = orbCenter5,
                                radius = orbRadius5
                            ),
                            radius = orbRadius5,
                            center = orbCenter5
                        )
                    }
                }
        )

        content()
    }
}

/**
 * Native Jetpack Compose implementation of frosted liquid glass.
 * Uses custom drawBehind for artifact-free glow and border rendering.
 * Avoids Modifier.shadow() and Modifier.border() which cause inner-rectangle
 * and octagon artifacts on semi-transparent surfaces and small circles.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(22.dp),
    blurRadius: Dp = 20.dp,
    lensHeight: Dp = 16.dp,
    lensAmount: Dp = 18.dp,
    tint: Color? = null,
    borderWidth: Dp = 1.dp,
    borderAlpha: Float = 0.35f,
    elevation: Dp = 4.dp
): Modifier = composed {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    // In light mode, glass requires a translucent white base to appear frosted
    // rather than muddy/gray when an accent tint is applied over the backdrop
    val resolvedTint = if (tint != null) {
        if (isDark) {
            tint
        } else {
            // Composite tint with white so colored cards look like tinted milky crystal
            Color(
                red = (tint.red * 0.35f + 1f * 0.65f),
                green = (tint.green * 0.35f + 1f * 0.65f),
                blue = (tint.blue * 0.35f + 1f * 0.65f),
                alpha = (tint.alpha * 0.8f + 0.55f).coerceAtMost(0.78f)
            )
        }
    } else {
        if (isDark) {
            Color(0xFF0F172A).copy(alpha = 0.52f)
        } else {
            Color.White.copy(alpha = 0.72f)
        }
    }

    // Specular refraction rim colors
    val rimColors = remember(isDark, borderAlpha) {
        if (isDark) {
            listOf(
                Color.White.copy(alpha = (borderAlpha * 1.3f).coerceAtMost(1f)),
                Color(0xFF38BDF8).copy(alpha = borderAlpha * 0.6f),
                Color(0xFF00F59B).copy(alpha = borderAlpha * 0.3f),
                Color.White.copy(alpha = borderAlpha * 0.15f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = (borderAlpha * 2.2f).coerceAtMost(1f)),
                Color(0xFF38BDF8).copy(alpha = borderAlpha * 0.5f),
                Color(0xFF10B981).copy(alpha = borderAlpha * 0.35f),
                Color.White.copy(alpha = (borderAlpha * 1.6f).coerceAtMost(1f))
            )
        }
    }

    val glowColor = remember(isDark, elevation) {
        if (isDark) Color.Black.copy(alpha = (0.12f * elevation.value).coerceAtMost(0.6f))
        else Color(0xFF64748B).copy(alpha = (0.06f * elevation.value).coerceAtMost(0.35f))
    }

    this
        .drawBehind {
            // Soft glow shadow — drawn as multiple expanded, blurred outlines
            // behind the shape, completely replacing Modifier.shadow()
            if (elevation > 0.dp) {
                val outline = shape.createOutline(size, layoutDirection, this)
                val paint = Paint().apply {
                    color = glowColor
                    isAntiAlias = true
                }
                drawIntoCanvas { canvas ->
                    // Draw soft shadow layers
                    val shadowPx = elevation.toPx()
                    val layers = 3
                    for (i in layers downTo 1) {
                        val spread = shadowPx * i / layers
                        val layerAlpha = glowColor.alpha / (i * 1.5f)
                        paint.color = glowColor.copy(alpha = layerAlpha)

                        canvas.save()
                        canvas.translate(0f, spread * 0.5f)

                        when (outline) {
                            is Outline.Rectangle -> {
                                val inflated = outline.rect.inflate(spread)
                                canvas.drawRect(inflated, paint)
                            }
                            is Outline.Rounded -> {
                                val rr = outline.roundRect
                                val inflated = RoundRect(
                                    left = rr.left - spread,
                                    top = rr.top - spread,
                                    right = rr.right + spread,
                                    bottom = rr.bottom + spread,
                                    topLeftCornerRadius = CornerRadius(
                                        rr.topLeftCornerRadius.x + spread,
                                        rr.topLeftCornerRadius.y + spread
                                    ),
                                    topRightCornerRadius = CornerRadius(
                                        rr.topRightCornerRadius.x + spread,
                                        rr.topRightCornerRadius.y + spread
                                    ),
                                    bottomLeftCornerRadius = CornerRadius(
                                        rr.bottomLeftCornerRadius.x + spread,
                                        rr.bottomLeftCornerRadius.y + spread
                                    ),
                                    bottomRightCornerRadius = CornerRadius(
                                        rr.bottomRightCornerRadius.x + spread,
                                        rr.bottomRightCornerRadius.y + spread
                                    )
                                )
                                val path = Path().apply { addRoundRect(inflated) }
                                canvas.drawPath(path, paint)
                            }
                            is Outline.Generic -> {
                                canvas.drawPath(outline.path, paint)
                            }
                        }
                        canvas.restore()
                    }
                }
            }
        }
        .clip(shape)
        .background(resolvedTint)
        .drawWithContent {
            drawContent()

            // Light mode: subtle specular top-edge highlight for glass depth
            if (!isDark) {
                val highlightBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.35f
                )
                drawRect(brush = highlightBrush)
            }

            // Draw the border on top using drawRoundRect
            // to avoid Modifier.border() shape-path octagon artifacts
            val strokePx = borderWidth.toPx()
            if (strokePx > 0f && borderAlpha > 0f) {
                val rimBrush = Brush.linearGradient(
                    colors = rimColors,
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
                when (val outline = shape.createOutline(size, layoutDirection, this)) {
                    is Outline.Rounded -> {
                        val rr = outline.roundRect
                        val halfStroke = strokePx / 2f
                        drawRoundRect(
                            brush = rimBrush,
                            topLeft = Offset(halfStroke, halfStroke),
                            size = Size(
                                size.width - strokePx,
                                size.height - strokePx
                            ),
                            cornerRadius = CornerRadius(
                                (rr.topLeftCornerRadius.x - halfStroke).coerceAtLeast(0f),
                                (rr.topLeftCornerRadius.y - halfStroke).coerceAtLeast(0f)
                            ),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokePx
                            )
                        )
                    }
                    is Outline.Rectangle -> {
                        val halfStroke = strokePx / 2f
                        drawRect(
                            brush = rimBrush,
                            topLeft = Offset(halfStroke, halfStroke),
                            size = Size(
                                size.width - strokePx,
                                size.height - strokePx
                            ),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokePx
                            )
                        )
                    }
                    is Outline.Generic -> {
                        drawPath(
                            path = outline.path,
                            brush = rimBrush,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokePx
                            )
                        )
                    }
                }
            }
        }
}
