package com.example.warbage.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.YuvImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.warbage.ai.MaterialClassifier
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class AiResult(
    val materialName: String,
    val materialId: Int,
    val confidence: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScannerScreen(
    onMaterialDetected: (materialId: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var aiResult by remember { mutableStateOf<AiResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val classifier = remember { MaterialClassifier() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasCameraPermission) {
                // Camera preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageCapture
                                    )
                                } catch (_: Exception) {
                                    // Camera binding failed
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Capture overlay text
                    if (!isProcessing && aiResult == null) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            )
                        ) {
                            Text(
                                text = "Point camera at e-waste material",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Result section
                AnimatedVisibility(visible = aiResult != null) {
                    aiResult?.let { result ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Detected: ${result.materialName}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Confidence: ${String.format("%.0f", result.confidence * 100)}%",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            aiResult = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Retry")
                                    }
                                    Button(
                                        onClick = { onMaterialDetected(result.materialId) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Use This Material")
                                    }
                                }
                            }
                        }
                    }
                }

                // Capture button
                if (aiResult == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = {
                                if (!isProcessing) {
                                    isProcessing = true
                                    imageCapture.takePicture(
                                        cameraExecutor,
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(image: ImageProxy) {
                                                val bitmap = image.toBitmapCompat()
                                                val result = classifier.classify(bitmap)
                                                aiResult = result
                                                isProcessing = false
                                                image.close()
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                isProcessing = false
                                            }
                                        }
                                    )
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Capture",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            } else {
                // No permission
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📷",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera permission required",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Grant camera access to use AI Scanner",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convert ImageProxy to Bitmap.
 * Works across different image formats.
 */
private fun ImageProxy.toBitmapCompat(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    return if (format == ImageFormat.JPEG) {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } else {
        // YUV to JPEG conversion
        val yuvImage = YuvImage(
            bytes, ImageFormat.NV21, width, height, null
        )
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
        val jpegBytes = out.toByteArray()
        BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }
}
