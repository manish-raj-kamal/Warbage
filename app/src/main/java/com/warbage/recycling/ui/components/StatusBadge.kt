package com.warbage.recycling.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warbage.recycling.ui.glass.liquidGlass
import com.warbage.recycling.ui.theme.StatusAccepted
import com.warbage.recycling.ui.theme.StatusCompleted
import com.warbage.recycling.ui.theme.StatusInTransit
import com.warbage.recycling.ui.theme.StatusPending
import com.warbage.recycling.ui.theme.StatusRejected

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (status.lowercase()) {
        "pending" -> StatusPending to "PENDING"
        "accepted" -> StatusAccepted to "ACCEPTED"
        "completed" -> StatusCompleted to "COMPLETED"
        "rejected" -> StatusRejected to "REJECTED"
        "in_transit", "in transit" -> StatusInTransit to "IN TRANSIT"
        "created" -> StatusPending to "CREATED"
        "submitted" -> StatusInTransit to "SUBMITTED"
        else -> StatusPending to status.uppercase()
    }

    Box(
        modifier = modifier
            .liquidGlass(
                shape = CircleShape,
                blurRadius = 14.dp,
                lensHeight = 8.dp,
                lensAmount = 10.dp,
                tint = color.copy(alpha = 0.16f),
                borderAlpha = 0.45f,
                elevation = 2.dp
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glowing status beacon dot
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        spotColor = color,
                        ambientColor = color
                    )
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = color
            )
        }
    }
}
