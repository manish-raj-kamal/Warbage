package com.example.warbage.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// WARBAGE - CYBER-TITANIUM & BIOLUMINESCENT EMERALD DESIGN SYSTEM
// =========================================================================

// --- Dark Mode Base (Deep Obsidian & Cosmic Slate - Zero Murky Green) ---
val DarkBackground = Color(0xFF090D16)
val DarkSurface = Color(0xFF0F172A)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkSurfaceBorder = Color(0xFF334155)

// --- Light Mode Base (Crystalline Platinum & Frosted Pearl) ---
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightSurfaceBorder = Color(0xFFE2E8F0)

// --- Primary: Bioluminescent Mint & Hyper-Emerald ---
// Conveys modern high-value circular tech, clean energy, and e-waste transformation
val EmeraldNeon = Color(0xFF00F59B)      // Vivid glowing mint
val EmeraldBright = Color(0xFF10B981)    // Electric emerald
val EmeraldDeep = Color(0xFF059669)      // Rich forest emerald
val EmeraldMuted = Color(0xFF047857)     // Dark emerald
val EmeraldLight = Color(0xFFD1FAE5)     // Frosted mint tint
val EmeraldGlow = Color(0x3300F59B)      // Luminous shadow/orb

// --- Secondary: Cyber Cyan & Precision Azure ---
// Conveys microchip circuitry, digital trading, and high-tech diagnostics
val CyanNeon = Color(0xFF00E5FF)         // Holographic cyan
val CyanBright = Color(0xFF06B6D4)       // Precision azure
val CyanDeep = Color(0xFF0891B2)         // Deep oceanic cyan
val CyanLight = Color(0xFFCFFAFE)        // Translucent ice
val CyanGlow = Color(0x3000E5FF)         // Cyan glow

// --- Tertiary: Quantum Iris & Neon Violet ---
// Conveys AI optical scanning, computer vision, and smart sorting
val VioletNeon = Color(0xFFA78BFA)       // Electric lilac
val VioletBright = Color(0xFF8B5CF6)     // Quantum violet
val VioletDeep = Color(0xFF6D28D9)       // Deep amethyst
val VioletLight = Color(0xFFEDE9FE)      // Soft lavender tint
val VioletGlow = Color(0x308B5CF6)       // AI scanner glow

// --- Accent: Precious Metals Gold & Solar Flare ---
// Conveys recovered gold, copper scrap value, rupees, and wealth generation
val GoldNeon = Color(0xFFFFC107)         // Recovered gold
val GoldBright = Color(0xFFF59E0B)       // Solar amber
val GoldDeep = Color(0xFFD97706)         // Burnished bronze
val GoldLight = Color(0xFFFEF3C7)        // Champagne shimmer
val GoldGlow = Color(0x30F59E0B)         // Gold shimmer

// --- Text & Neutral Tones ---
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextTertiaryDark = Color(0xFF64748B)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextTertiaryLight = Color(0xFF94A3B8)

// --- Crisp Semantic Status Colors ---
val StatusPending = Color(0xFFF59E0B)    // Amber
val StatusAccepted = Color(0xFF10B981)   // Bright Emerald
val StatusCompleted = Color(0xFF6366F1)  // Digital Indigo
val StatusRejected = Color(0xFFF43F5E)   // Electric Rose
val StatusInTransit = Color(0xFF06B6D4)  // Cyan

// --- Error Colors ---
val ErrorNeon = Color(0xFFF43F5E)
val ErrorContainerDark = Color(0xFF4C0519)
val ErrorContainerLight = Color(0xFFFFE4E6)

// --- Backward compatibility aliases (to prevent breaking any legacy references) ---
val Green80 = EmeraldNeon
val Green40 = EmeraldBright
val Green30 = EmeraldDeep
val Green90 = EmeraldLight

val Teal80 = CyanNeon
val Teal40 = CyanBright
val Teal30 = CyanDeep

val Amber80 = GoldNeon
val Amber40 = GoldBright
val Amber30 = GoldDeep

val Neutral10 = DarkBackground
val Neutral20 = DarkSurfaceVariant
val Neutral90 = TextPrimaryDark
val Neutral95 = LightSurfaceVariant
val Neutral99 = LightBackground

val Error80 = ErrorNeon
val Error40 = ErrorNeon
