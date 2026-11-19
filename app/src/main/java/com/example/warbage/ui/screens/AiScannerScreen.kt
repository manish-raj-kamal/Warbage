package com.example.warbage.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.warbage.ai.MaterialClassifier
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.MaterialEntity
import com.example.warbage.data.repository.MaterialRepository
import com.example.warbage.ui.glass.GlassButton
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.LiquidTopAppBar
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.CyanBright
import com.example.warbage.ui.theme.EmeraldBright
import com.example.warbage.ui.theme.VioletBright
import kotlinx.coroutines.launch

data class AiResult(
    val materialName: String,
    val materialId: Int,
    val confidence: Float
)

@Composable
fun AiScannerScreen(
    database: WarbageDatabase? = null,
    onMaterialDetected: (materialId: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val materialRepo = remember(database) { database?.let { MaterialRepository(it.materialDao()) } }
    val materials by (database?.materialDao()?.getAllMaterials() ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    // Manual override dialog state
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMaterialForEdit by remember { mutableStateOf<MaterialEntity?>(null) }
    var editMinPrice by remember { mutableStateOf("") }
    var editMaxPrice by remember { mutableStateOf("") }
    var materialDropdownExpanded by remember { mutableStateOf(false) }
    
    val classifier = remember {
        try {
            MaterialClassifier(context)
        } catch (_: Exception) {
            null
        }
    }

    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var aiResult by remember { mutableStateOf<AiResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    // Take picture launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val softwareBitmap = try {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } catch (_: Exception) {
                bitmap
            }
            currentBitmap = softwareBitmap
            isProcessing = true
            errorMessage = null
            coroutineScope.launch {
                try {
                    if (classifier != null) {
                        val result = classifier.classify(softwareBitmap)
                        aiResult = result
                    } else {
                        errorMessage = "AI Classifier model could not be initialized."
                    }
                } catch (e: Exception) {
                    errorMessage = "Recognition error: ${e.localizedMessage ?: "Model evaluation error"}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    // Camera permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                takePictureLauncher.launch(null)
            } catch (e: Exception) {
                errorMessage = "Unable to open camera: ${e.localizedMessage}"
            }
        } else {
            showPermissionDeniedDialog = true
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isProcessing = true
            errorMessage = null
            coroutineScope.launch {
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    }
                    val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                    currentBitmap = softwareBitmap

                    if (classifier != null) {
                        val result = classifier.classify(softwareBitmap)
                        aiResult = result
                    } else {
                        errorMessage = "AI Classifier model could not be initialized."
                    }
                } catch (e: Exception) {
                    errorMessage = "Could not process image: ${e.localizedMessage ?: "Invalid file"}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = { Text("AI Vision Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    GlassIconButton(
                        onClick = onBack,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
            // Error Feedback Banner
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .liquidGlass(
                                shape = RoundedCornerShape(14.dp),
                                blurRadius = 10.dp,
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                borderAlpha = 0.5f
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (currentBitmap == null) {
                // Empty Viewfinder State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        elevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .liquidGlass(
                                        shape = CircleShape,
                                        blurRadius = 22.dp,
                                        lensHeight = 16.dp,
                                        lensAmount = 20.dp,
                                        tint = VioletBright.copy(alpha = 0.28f),
                                        borderAlpha = 0.65f,
                                        elevation = 6.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = VioletBright
                                )
                            }

                            Text(
                                text = "Scan E-Waste Item",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Point your camera or upload a clear photo of motherboards, lithium batteries, cables, or electronic appliances for instant LiteRT recognition.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Camera Button with runtime permission check
                                GlassButton(
                                    onClick = {
                                        errorMessage = null
                                        val hasPerm = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (hasPerm) {
                                            try {
                                                takePictureLauncher.launch(null)
                                            } catch (e: Exception) {
                                                errorMessage = "Unable to open camera: ${e.localizedMessage}"
                                            }
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    tint = EmeraldBright.copy(alpha = 0.32f),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = EmeraldBright)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Camera", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }

                                // Gallery Button
                                GlassButton(
                                    onClick = {
                                        errorMessage = null
                                        try {
                                            galleryLauncher.launch("image/*")
                                        } catch (e: Exception) {
                                            errorMessage = "Unable to open gallery: ${e.localizedMessage}"
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    tint = CyanBright.copy(alpha = 0.28f),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = CyanBright)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gallery", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            } else {
                // Image Preview Glass State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(28.dp),
                        elevation = 6.dp
                    ) {
                        Image(
                            bitmap = currentBitmap!!.asImageBitmap(),
                            contentDescription = "Captured Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(28.dp)),
                            contentScale = ContentScale.Fit
                        )

                        if (isProcessing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .liquidGlass(
                                        shape = RoundedCornerShape(28.dp),
                                        blurRadius = 18.dp,
                                        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = VioletBright,
                                        modifier = Modifier.size(36.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        "Classifying with LiteRT AI...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Result Floating Glass Sheet
                AnimatedVisibility(visible = aiResult != null && !isProcessing) {
                    aiResult?.let { result ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(26.dp),
                            tint = EmeraldBright.copy(alpha = 0.16f),
                            elevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = EmeraldBright
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = result.materialName,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .liquidGlass(
                                                shape = CircleShape,
                                                blurRadius = 8.dp,
                                                tint = EmeraldBright.copy(alpha = 0.22f),
                                                borderAlpha = 0.5f,
                                                elevation = 1.dp
                                            )
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "${String.format("%.0f", result.confidence * 100)}% match",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldBright
                                        )
                                    }
                                }

                                // Show current market rate from database if available
                                val matchedDbMaterial = materials.find { it.id == result.materialId }
                                if (matchedDbMaterial != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Current Market Rate: ₹${matchedDbMaterial.minPrice} – ₹${matchedDbMaterial.maxPrice} / ${matchedDbMaterial.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Collector / User Manual Edit Button
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .liquidGlass(
                                            shape = RoundedCornerShape(12.dp),
                                            tint = CyanBright.copy(alpha = 0.15f),
                                            borderAlpha = 0.4f,
                                            elevation = 1.dp
                                        )
                                        .clickable {
                                            val currentMat = materials.find { it.id == result.materialId }
                                                ?: materials.firstOrNull()
                                            selectedMaterialForEdit = currentMat
                                            editMinPrice = currentMat?.minPrice?.toString() ?: ""
                                            editMaxPrice = currentMat?.maxPrice?.toString() ?: ""
                                            showEditDialog = true
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = CyanBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Change Material Type or Market Rate",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanBright
                                        )
                                    }
                                }

                                if (result.confidence < 0.6f) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Low confidence detection. You may also select your material manually.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    GlassButton(
                                        onClick = {
                                            currentBitmap = null
                                            aiResult = null
                                            errorMessage = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Retake", fontWeight = FontWeight.SemiBold)
                                    }

                                    GlassButton(
                                        onClick = { onMaterialDetected(result.materialId) },
                                        modifier = Modifier.weight(1.4f),
                                        tint = EmeraldBright.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            "Proceed to Lot",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Collector Material & Market Rate Override Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text("Override Material & Rate", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select corrected material type and adjust current market rate if needed. Changes will synchronize with the database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Material Selector Dropdown Box
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedMaterialForEdit?.let { "${it.icon} ${it.name}" } ?: "Select Material",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Material Type") },
                            trailingIcon = {
                                TextButton(onClick = { materialDropdownExpanded = true }) {
                                    Text("Change", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { materialDropdownExpanded = true }
                        )

                        DropdownMenu(
                            expanded = materialDropdownExpanded,
                            onDismissRequest = { materialDropdownExpanded = false }
                        ) {
                            materials.forEach { mat ->
                                DropdownMenuItem(
                                    text = { Text("${mat.icon} ${mat.name} (₹${mat.minPrice} - ₹${mat.maxPrice} / ${mat.unit})") },
                                    onClick = {
                                        selectedMaterialForEdit = mat
                                        editMinPrice = mat.minPrice.toString()
                                        editMaxPrice = mat.maxPrice.toString()
                                        materialDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Price inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editMinPrice,
                            onValueChange = { editMinPrice = it },
                            label = { Text("Min Rate (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editMaxPrice,
                            onValueChange = { editMaxPrice = it },
                            label = { Text("Max Rate (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    selectedMaterialForEdit?.let { mat ->
                        Text(
                            text = "Unit: ${mat.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val mat = selectedMaterialForEdit
                        val min = editMinPrice.toIntOrNull()
                        val max = editMaxPrice.toIntOrNull()

                        if (mat == null) {
                            Toast.makeText(context, "Please select a material", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        if (min == null || max == null || min <= 0 || max < min) {
                            Toast.makeText(context, "Please enter valid market rates (min ≤ max)", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        coroutineScope.launch {
                            // Update material in database with new rates if changed
                            val updatedMat = mat.copy(minPrice = min, maxPrice = max)
                            materialRepo?.updateMaterial(updatedMat)

                            // Update active AI result to use the overridden material
                            aiResult = AiResult(
                                materialName = mat.name,
                                materialId = mat.id,
                                confidence = 1.0f // Explicit user override has 100% confidence
                            )
                            Toast.makeText(context, "Updated to ${mat.name} (₹$min - ₹$max) & synchronized to database!", Toast.LENGTH_SHORT).show()
                            showEditDialog = false
                        }
                    }
                ) {
                    Text("Apply & Sync", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Permission Denied Dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = {
                Text("Camera Permission Required", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Warbage requires Camera access to scan and identify e-waste components with AI. Please allow camera permission in App Settings, or select an image from your gallery.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDeniedDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Please open Settings and grant Camera permission", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("Open Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
