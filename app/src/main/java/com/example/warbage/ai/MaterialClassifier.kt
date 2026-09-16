package com.example.warbage.ai

import android.graphics.Bitmap
import com.example.warbage.ui.screens.AiResult

/**
 * MaterialClassifier — AI-based e-waste material recognition.
 *
 * For V1, this uses a simple image analysis heuristic based on dominant color
 * and brightness to classify materials. This demonstrates the full AI pipeline
 * (Camera → Image → Classification → Result) without requiring a bundled
 * TFLite model file.
 *
 * In V2, this will be replaced with a proper TFLite model trained on e-waste images.
 */
class MaterialClassifier {

    // Our supported material classes mapped to material IDs in Room
    private val materialClasses = listOf(
        MaterialClass("PCB", 1, "Printed Circuit Board"),
        MaterialClass("Cables", 2, "Copper/Aluminum Cables"),
        MaterialClass("Batteries", 3, "Li-ion/Lead-acid Batteries"),
        MaterialClass("LCD/LED", 4, "Display Panels"),
        MaterialClass("Motors", 5, "Electric Motors"),
        MaterialClass("CRT", 6, "Cathode Ray Tubes"),
        MaterialClass("Mixed Plastics", 7, "Mixed E-Waste Plastics")
    )

    /**
     * Classify a captured bitmap and return the detected material with confidence.
     * Uses image color/brightness analysis to produce a plausible classification.
     */
    fun classify(bitmap: Bitmap): AiResult {
        // Resize for analysis
        val scaled = Bitmap.createScaledBitmap(bitmap, 64, 64, true)

        // Analyze dominant colors
        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        var totalBrightness = 0L
        val pixelCount = scaled.width * scaled.height

        for (x in 0 until scaled.width) {
            for (y in 0 until scaled.height) {
                val pixel = scaled.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                totalR += r
                totalG += g
                totalB += b
                totalBrightness += (r + g + b) / 3
            }
        }

        val avgR = totalR / pixelCount
        val avgG = totalG / pixelCount
        val avgB = totalB / pixelCount
        val avgBrightness = totalBrightness / pixelCount

        // Simple heuristic classification based on color profile
        val (classIndex, confidence) = when {
            // Green-dominant → PCB (circuit boards are typically green)
            avgG > avgR && avgG > avgB && avgG > 100 -> 0 to (0.82f + (avgG - 100) / 1000f)
            // Dark with some color → Cables
            avgBrightness < 80 && avgR > avgB -> 1 to (0.75f + (80 - avgBrightness) / 500f)
            // Very dark → Batteries or Motors
            avgBrightness < 60 -> if (avgB > avgR) 2 to 0.78f else 4 to 0.73f
            // Bright/white-ish → LCD/LED panels
            avgBrightness > 180 -> 3 to (0.80f + (avgBrightness - 180) / 500f)
            // Gray tones → CRT
            kotlin.math.abs(avgR - avgG) < 20 && kotlin.math.abs(avgG - avgB) < 20
                && avgBrightness in 80..160 -> 5 to 0.71f
            // Mixed/colorful → Mixed Plastics
            else -> 6 to 0.68f
        }

        val safeIndex = classIndex.coerceIn(0, materialClasses.size - 1)
        val material = materialClasses[safeIndex]
        val safeConfidence = confidence.coerceIn(0.50f, 0.97f)

        scaled.recycle()

        return AiResult(
            materialName = material.displayName,
            materialId = material.id,
            confidence = safeConfidence
        )
    }

    private data class MaterialClass(
        val name: String,
        val id: Int,
        val displayName: String
    )
}
