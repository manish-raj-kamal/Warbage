package com.example.warbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.RecyclerEntity
import com.example.warbage.ui.glass.GlassCard
import com.example.warbage.ui.glass.GlassIconButton
import com.example.warbage.ui.glass.LiquidTopAppBar
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.CyanBright
import com.example.warbage.ui.theme.EmeraldBright
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun RecyclerMapScreen(
    database: WarbageDatabase,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val recyclers by database.recyclerDao().getAllRecyclers().collectAsState(initial = emptyList())
    var selectedRecycler by remember { mutableStateOf<RecyclerEntity?>(null) }

    val defaultPosition = LatLng(20.5937, 78.9629) // Center of India overview
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 5f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            recyclers.forEach { recycler ->
                val position = LatLng(recycler.latitude, recycler.longitude)
                Marker(
                    state = MarkerState(position = position),
                    title = recycler.name,
                    snippet = "${recycler.materials} • ₹${recycler.pricePerKg}/kg",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    onClick = {
                        selectedRecycler = recycler
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(position, 12f)
                            )
                        }
                        true
                    }
                )
            }
        }

        // Top App Bar
        LiquidTopAppBar(
            title = { Text("Recycler Map", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                GlassIconButton(
                    onClick = onBack,
                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Selected Recycler Bottom Card
        if (selectedRecycler != null) {
            val recycler = selectedRecycler!!
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = recycler.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (recycler.authorized) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Authorized Recycler",
                                            tint = CyanBright,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = recycler.materials,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { selectedRecycler = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = recycler.location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "₹${recycler.pricePerKg}/kg",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldBright
                            )
                        }
                    }
                }
            }
        }

        // Custom Glass "Reset View" Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 20.dp,
                    bottom = if (selectedRecycler != null) 230.dp else 40.dp
                )
        ) {
            GlassIconButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(defaultPosition, 5f)
                        )
                    }
                },
                size = 50.dp,
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Overview",
                    tint = EmeraldBright,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
