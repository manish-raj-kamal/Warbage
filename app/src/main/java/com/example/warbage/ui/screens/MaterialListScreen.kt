package com.example.warbage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.MaterialEntity
import com.example.warbage.data.repository.MaterialRepository
import com.example.warbage.ui.components.MaterialCard
import com.example.warbage.ui.glass.GlassButton
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.GlassTextField
import com.example.warbage.ui.glass.LiquidTopAppBar
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.EmeraldBright
import kotlinx.coroutines.launch

@Composable
fun MaterialListScreen(
    database: WarbageDatabase,
    onMaterialClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val materialRepo = remember { MaterialRepository(database.materialDao()) }

    val materials by database.materialDao().getAllMaterials().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Dialog state for adding new material / category
    var showAddDialog by remember { mutableStateOf(false) }
    var newMaterialName by remember { mutableStateOf("") }
    var newMaterialDescription by remember { mutableStateOf("") }
    var newMinPrice by remember { mutableStateOf("") }
    var newMaxPrice by remember { mutableStateOf("") }
    var newUnit by remember { mutableStateOf("kg") }
    var newIcon by remember { mutableStateOf("♻️") }

    val baseCategories = listOf("All", "Batteries", "Boards & CPUs", "Appliances", "Screens", "Cables", "Custom")

    val filteredMaterials = remember(materials, searchQuery, selectedCategory) {
        materials.filter { mat ->
            val matchesQuery = searchQuery.isBlank() ||
                    mat.name.contains(searchQuery, ignoreCase = true) ||
                    mat.description.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                "Batteries" -> mat.name.contains("Battery", ignoreCase = true) || mat.name.contains("Cell", ignoreCase = true)
                "Boards & CPUs" -> mat.name.contains("Board", ignoreCase = true) || mat.name.contains("CPU", ignoreCase = true) || mat.name.contains("RAM", ignoreCase = true) || mat.name.contains("PCB", ignoreCase = true)
                "Appliances" -> mat.name.contains("Microwave", ignoreCase = true) || mat.name.contains("Appliance", ignoreCase = true) || mat.name.contains("Fan", ignoreCase = true) || mat.name.contains("Washing", ignoreCase = true)
                "Screens" -> mat.name.contains("Display", ignoreCase = true) || mat.name.contains("Monitor", ignoreCase = true) || mat.name.contains("TV", ignoreCase = true) || mat.name.contains("Television", ignoreCase = true)
                "Cables" -> mat.name.contains("Cable", ignoreCase = true) || mat.name.contains("Wire", ignoreCase = true)
                "Custom" -> mat.id > 10
                else -> true
            }

            matchesQuery && matchesCategory
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = { Text("E-Waste Catalog", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    GlassIconButton(
                        onClick = onBack,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    GlassIconButton(
                        onClick = { showAddDialog = true },
                        tint = EmeraldBright.copy(alpha = 0.25f)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Category / Material",
                            tint = EmeraldBright
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            GlassButton(
                onClick = { showAddDialog = true },
                shape = CircleShape,
                tint = EmeraldBright.copy(alpha = 0.32f),
                elevation = 6.dp
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = EmeraldBright,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Material",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                GlassTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search materials...",
                    placeholder = "e.g. Lithium, Motherboard, Cable",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else null
                )
            }

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                baseCategories.forEach { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .liquidGlass(
                                shape = CircleShape,
                                blurRadius = 12.dp,
                                lensHeight = 8.dp,
                                lensAmount = 10.dp,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                },
                                borderAlpha = if (isSelected) 0.6f else 0.25f,
                                elevation = if (isSelected) 3.dp else 0.dp
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Materials List
            if (filteredMaterials.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        shape = RoundedCornerShape(26.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔍",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Materials Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search query, selecting another category, or add a custom material.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredMaterials, key = { it.id }) { material ->
                        MaterialCard(
                            material = material,
                            onClick = { onMaterialClick(material.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Material / Category Dialog
    if (showAddDialog) {
        val iconOptions = listOf("♻️", "⚡", "🖥️", "🔌", "⚙️", "📦", "🔋", "💡")
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Add New Material", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newMaterialName,
                        onValueChange = { newMaterialName = it },
                        label = { Text("Material / Category Name") },
                        placeholder = { Text("e.g. Copper Wire, Solar Panel") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMaterialDescription,
                        onValueChange = { newMaterialDescription = it },
                        label = { Text("Description") },
                        placeholder = { Text("e.g. High-grade recycled copper") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newMinPrice,
                            onValueChange = { newMinPrice = it },
                            label = { Text("Min Price (₹)") },
                            placeholder = { Text("100") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newMaxPrice,
                            onValueChange = { newMaxPrice = it },
                            label = { Text("Max Price (₹)") },
                            placeholder = { Text("250") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newUnit,
                            onValueChange = { newUnit = it },
                            label = { Text("Unit") },
                            placeholder = { Text("kg / piece") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Select Icon:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconOptions.forEach { icon ->
                            val isSelected = icon == newIcon
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .liquidGlass(
                                        shape = CircleShape,
                                        tint = if (isSelected) EmeraldBright.copy(alpha = 0.35f) else Color.Transparent,
                                        borderAlpha = if (isSelected) 0.6f else 0.2f
                                    )
                                    .clickable { newIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = icon, fontSize = 20.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newMaterialName.trim()
                        val desc = if (newMaterialDescription.isBlank()) "$name e-waste material" else newMaterialDescription.trim()
                        val min = newMinPrice.toIntOrNull()
                        val max = newMaxPrice.toIntOrNull()
                        val unit = if (newUnit.isBlank()) "kg" else newUnit.trim()

                        if (name.isBlank() || min == null || max == null || min <= 0 || max < min) {
                            Toast.makeText(context, "Please enter a valid name and price range (min ≤ max)", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        scope.launch {
                            val nextId = materialRepo.getNextId()
                            val newMaterial = MaterialEntity(
                                id = nextId,
                                name = name,
                                description = desc,
                                minPrice = min,
                                maxPrice = max,
                                unit = unit,
                                icon = newIcon
                            )
                            materialRepo.insertMaterial(newMaterial)
                            Toast.makeText(context, "Added $name to catalog!", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                            // Reset inputs
                            newMaterialName = ""
                            newMaterialDescription = ""
                            newMinPrice = ""
                            newMaxPrice = ""
                            newUnit = "kg"
                            newIcon = "♻️"
                        }
                    }
                ) {
                    Text("Add Material", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
