package com.example.warbage.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.data.entity.LotEntity
import com.example.warbage.data.entity.RequestEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionCard(
    lot: LotEntity,
    request: RequestEntity?,
    materialName: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val status = request?.status ?: lot.status
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { onClick?.invoke() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Text(
                    text = lot.lotId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$materialName • ${lot.weight} ${if (lot.weight == 1.0) "kg" else "kg"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${String.format("%.0f", lot.estimatedMinValue)}–₹${String.format("%.0f", lot.estimatedMaxValue)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dateFormat.format(Date(lot.dateCreated)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (request != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Request: ${request.requestId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
