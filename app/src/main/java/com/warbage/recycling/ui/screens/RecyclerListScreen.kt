package com.warbage.recycling.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warbage.recycling.data.WarbageDatabase
import com.warbage.recycling.ui.components.RecyclerCard
import com.warbage.recycling.ui.glass.GlassCard
import com.warbage.recycling.ui.glass.GlassIconButton
import com.warbage.recycling.ui.glass.LiquidTopAppBar
import com.warbage.recycling.ui.theme.EmeraldBright

@Composable
fun RecyclerListScreen(
    database: WarbageDatabase,
    lotId: Int,
    onRecyclerSelected: (recyclerId: Int) -> Unit,
    onBack: () -> Unit
) {
    val recyclers by database.recyclerDao().getAllRecyclers().collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidTopAppBar(
                title = { Text("Select Certified Recycler", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = EmeraldBright,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Verified Recycler Network",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "All listed facilities are ISO-14001 & R2 certified for ethical e-waste recycling.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(recyclers, key = { it.id }) { recycler ->
                RecyclerCard(
                    recycler = recycler,
                    onSelect = { onRecyclerSelected(recycler.id) }
                )
            }
        }
    }
}
