package com.example.warbage.ai

import android.content.Context
import android.graphics.Bitmap
import com.example.warbage.ui.screens.AiResult
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import com.google.ai.edge.litert.LiteRtException
import com.google.ai.edge.litert.TensorBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * MaterialClassifier — AI-based e-waste material recognition using LiteRT.
 */
class MaterialClassifier(context: Context) : AutoCloseable {

    private val labels = mutableListOf<String>()

    private val environment by lazy { Environment.create() }
    private val model: CompiledModel
    private val inputs = mutableListOf<TensorBuffer>()
    private val outputs = mutableListOf<TensorBuffer>()

    // Use default CPU accelerator for simplicity, can be updated later if needed.
    // The placeholder model takes 224x224x3 float or uint8 input.
    // Assuming our train script generates a float32 model by default (no quantization).
    // But the mobilenet quant model we downloaded uses uint8. Let's handle it gracefully.

    init {
        // Load labels
        context.assets.open("e_waste_labels.txt").bufferedReader().useLines { lines ->
            labels.addAll(lines)
        }

        // Initialize LiteRT CompiledModel
        model = CompiledModel.create(
            context.assets,
            "e_waste_classifier.tflite",
            CompiledModel.Options(Accelerator.CPU).apply {
                cpuOptions = CompiledModel.CpuOptions(
                    numThreads = 4,
                    xnnPackFlags = 0x23
                )
            },
            environment
        )

        try {
            inputs += model.createInputBuffers()
            outputs += model.createOutputBuffers()
            // We'll skip warmup inference here since we might have uint8 or float32 model and we are doing a quick init.
            // A real app should do a warmup inference.
        } catch (e: Exception) {
            close()
            throw e
        }
    }

    /**
     * Classify a captured bitmap and return the detected material with confidence.
     * Uses the loaded LiteRT model on a background thread.
     */
    suspend fun classify(bitmap: Bitmap): AiResult = withContext(Dispatchers.Default) {
        val inputSize = 224
        val scaled = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true).copy(Bitmap.Config.ARGB_8888, false)
        val pixels = IntArray(inputSize * inputSize)
        scaled.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        // Assuming our placeholder mobilenet quant model expects UINT8 [1, 224, 224, 3]
        // If our real model is Float, this code will need to writeFloat instead of writeInt8.
        // We will try writing Float array if it's float, else Int8.

        // Let's check the buffer type
        val isFloat = try {
            inputs[0].writeFloat(FloatArray(1))
            true
        } catch (e: Exception) {
            false
        }

        if (isFloat) {
            val floatInput = FloatArray(inputSize * inputSize * 3)
            pixels.forEachIndexed { i, p ->
                // Do NOT normalize! Pass raw 0-255 values, identical to app.py logic
                floatInput[i * 3] = (p shr 16 and 0xFF).toFloat()
                floatInput[i * 3 + 1] = (p shr 8 and 0xFF).toFloat()
                floatInput[i * 3 + 2] = (p and 0xFF).toFloat()
            }
            inputs[0].writeFloat(floatInput)
        } else {
            // Uint8 expects ByteArray
            val byteInput = ByteArray(inputSize * inputSize * 3)
            pixels.forEachIndexed { i, p ->
                byteInput[i * 3] = (p shr 16 and 0xFF).toByte()
                byteInput[i * 3 + 1] = (p shr 8 and 0xFF).toByte()
                byteInput[i * 3 + 2] = (p and 0xFF).toByte()
            }
            inputs[0].writeInt8(byteInput)
        }

        model.run(inputs, outputs)

        val floatOutput = if (isFloat) {
            outputs[0].readFloat()
        } else {
            val byteOut = outputs[0].readInt8()
            val floats = FloatArray(byteOut.size)
            for (i in byteOut.indices) {
                floats[i] = (byteOut[i].toInt() and 0xFF) / 255.0f
            }
            floats
        }

        var maxIdx = 0
        var maxProb = 0f

        for (i in floatOutput.indices) {
            val prob = floatOutput[i]
            if (prob > maxProb) {
                maxProb = prob
                maxIdx = i
            }
        }

        val labelName = if (maxIdx < labels.size) labels[maxIdx] else "Other"

        // Map the new 10 HuggingFace classes to our 5 database categories
        val mappedId = when (labelName) {
            "PCB" -> 1 
            "Battery" -> 3 
            "Mobile", "Television" -> 4 // LCD_Panels
            else -> 7 // Other (Keyboard, Microwave, Mouse, Player, Printer, Washing Machine)
        }

        // Return the user-friendly category name rather than the granular raw label
        val displayName = when (mappedId) {
            1 -> "Printed Circuit Board"
            3 -> "Batteries"
            4 -> "Display Panels"
            else -> "Other E-Waste"
        }

        AiResult(
            materialName = displayName,
            materialId = mappedId,
            confidence = maxProb
        )
    }

    override fun close() {
        (inputs + outputs).forEach { it.close() }
        model.close()
    }
}
