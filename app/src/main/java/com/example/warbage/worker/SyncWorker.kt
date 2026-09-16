package com.example.warbage.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.warbage.data.WarbageDatabase
import kotlinx.coroutines.flow.first

/**
 * SyncWorker — Background sync using WorkManager.
 *
 * For V1, this demonstrates the WorkManager/CoroutineWorker pattern:
 * - Checks for pending lots that haven't been synced
 * - Logs sync activity
 * - In V2, this would upload to a cloud backend
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "SyncWorker"
        const val WORK_NAME = "warbage_sync"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "SyncWorker started — checking for pending data...")

        return try {
            val database = WarbageDatabase.getDatabase(applicationContext)

            // Get all lots
            val lots = database.lotDao().getAllLots().first()
            val pendingLots = lots.filter { it.status == "created" || it.status == "submitted" }

            Log.d(TAG, "Total lots: ${lots.size}, Pending sync: ${pendingLots.size}")

            // Get all requests
            val requests = database.requestDao().getAllRequests().first()
            val pendingRequests = requests.filter { it.status == "pending" }

            Log.d(TAG, "Total requests: ${requests.size}, Pending: ${pendingRequests.size}")

            // In V2: Upload pending items to cloud backend
            // For V1: Just log that we'd sync
            pendingLots.forEach { lot ->
                Log.d(TAG, "Would sync lot: ${lot.lotId} (${lot.status})")
            }

            Log.d(TAG, "SyncWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker failed", e)
            Result.retry()
        }
    }
}
