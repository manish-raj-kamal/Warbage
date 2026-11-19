package com.warbage.recycling.ai

import android.content.Context
import android.graphics.Bitmap
import com.warbage.recycling.ui.screens.AiResult
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
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
        val scaled = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            .copy(Bitmap.Config.ARGB_8888, false)
        val pixels = IntArray(inputSize * inputSize)
        scaled.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        val floatInput = FloatArray(inputSize * inputSize * 3)
        pixels.forEachIndexed { i, p ->
            floatInput[i * 3] = (p shr 16 and 0xFF).toFloat()
            floatInput[i * 3 + 1] = (p shr 8 and 0xFF).toFloat()
            floatInput[i * 3 + 2] = (p and 0xFF).toFloat()
        }

        var isFloat = true
        try {
            inputs[0].writeFloat(floatInput)
        } catch (_: Exception) {
            isFloat = false
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
            try {
                outputs[0].readFloat()
            } catch (_: Exception) {
                FloatArray(labels.size)
            }
        } else {
            try {
                val byteOut = outputs[0].readInt8()
                val floats = FloatArray(byteOut.size)
                for (i in byteOut.indices) {
                    floats[i] = (byteOut[i].toInt() and 0xFF) / 255.0f
                }
                floats
            } catch (_: Exception) {
                FloatArray(labels.size)
            }
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

        // Strictly map the 10 HuggingFace classes directly to the matching Database IDs
        val mappedId = when (labelName) {
            "Battery" -> 1
            "Keyboard" -> 2
            "Microwave" -> 3
            "Mobile" -> 4
            "Mouse" -> 5
            "PCB" -> 6
            "Player" -> 7
            "Printer" -> 8
            "Television" -> 9
            "Washing Machine" -> 10
            else -> 1 // Default fallback
        }

        AiResult(
            materialName = labelName,
            materialId = mappedId,
            confidence = maxProb
        )
    }

    override fun close() {
        try {
            (inputs + outputs).forEach { it.close() }
            model.close()
        } catch (_: Exception) {
        }
    }
}
