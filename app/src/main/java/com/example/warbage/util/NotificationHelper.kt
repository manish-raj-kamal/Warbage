package com.example.warbage.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.warbage.MainActivity

/**
 * NotificationHelper — Manages local notifications for status changes.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "warbage_requests"
    private const val CHANNEL_NAME = "Request Updates"
    private const val CHANNEL_DESCRIPTION = "Notifications for e-waste request status changes"

    /**
     * Create the notification channel (required for Android 8.0+).
     * Should be called once at app startup.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Show a notification for request status change.
     */
    fun showRequestStatusNotification(
        context: Context,
        requestId: String,
        newStatus: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val statusText = when (newStatus.lowercase()) {
            "accepted" -> "Your request $requestId has been accepted! ✅"
            "rejected" -> "Your request $requestId was rejected. ❌"
            "completed" -> "Transaction $requestId is now complete! 🎉"
            else -> "Request $requestId status updated to $newStatus"
        }

        val targetIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            targetIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("WARbage")
            .setContentText(statusText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted on Android 13+
        }
    }
}
