package com.example.warbage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.warbage.ui.theme.*

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (status.lowercase()) {
        "pending" -> StatusPending to "🟡 Pending"
        "accepted" -> StatusAccepted to "🟢 Accepted"
        "completed" -> StatusCompleted to "✅ Completed"
        "rejected" -> StatusRejected to "❌ Rejected"
        "created" -> StatusPending to "📦 Created"
        "submitted" -> StatusPending to "📤 Submitted"
        else -> StatusPending to status
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
