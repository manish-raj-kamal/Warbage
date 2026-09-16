package com.example.warbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.ui.components.RecyclerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecyclerListScreen(
    database: WarbageDatabase,
    lotId: Int,
    onRecyclerSelected: (recyclerId: Int) -> Unit,
    onBack: () -> Unit
) {
    val recyclers by database.recyclerDao().getAllRecyclers().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Recycler", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Choose a recycler for your lot",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
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
