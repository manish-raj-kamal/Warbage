package com.example.warbage.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.warbage.ui.glass.liquidGlass
import com.example.warbage.ui.theme.StatusAccepted
import com.example.warbage.ui.theme.StatusCompleted
import com.example.warbage.ui.theme.StatusPending
import com.example.warbage.ui.theme.StatusRejected

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
            .liquidGlass(
                shape = CircleShape,
                blurRadius = 12.dp,
                lensHeight = 8.dp,
                lensAmount = 10.dp,
                tint = color.copy(alpha = 0.22f),
                borderAlpha = 0.5f,
                elevation = 2.dp
            )
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
