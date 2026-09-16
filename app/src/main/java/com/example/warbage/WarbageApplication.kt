package com.example.warbage

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.warbage.data.WarbageDatabase
import com.example.warbage.util.NotificationHelper
import com.example.warbage.worker.SyncWorker
import java.util.concurrent.TimeUnit

class WarbageApplication : Application() {

    lateinit var database: WarbageDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize database (triggers prepopulation on first launch)
        database = WarbageDatabase.getDatabase(this)

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Schedule periodic sync with WorkManager
        scheduleSyncWork()
    }

    private fun scheduleSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
