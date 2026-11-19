package com.warbage.recycling.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.warbage.recycling.data.WarbageDatabase
import com.warbage.recycling.data.repository.MaterialRepository
import com.warbage.recycling.network.BackendLot
import com.warbage.recycling.network.BackendRequest
import com.warbage.recycling.network.RetrofitClient
import kotlinx.coroutines.flow.first

/**
 * SyncWorker — Background sync using WorkManager.
 *
 * Ensures all lots, requests, materials, and transactions are synchronized with
 * the secured cloud backend database.
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
        Log.d(TAG, "SyncWorker started — synchronizing lots, requests, materials, and prices...")

        return try {
            val database = WarbageDatabase.getDatabase(applicationContext)

            // 1. Sync Materials & Prices with Backend
            val materialRepo = MaterialRepository(database.materialDao())
            materialRepo.syncWithBackend()

            try {
                val prices = RetrofitClient.api.getPrices()
                Log.d(TAG, "Synchronized ${prices.size} price records from backend")
            } catch (e: Exception) {
                Log.w(TAG, "Price board sync notice: ${e.message}")
            }

            // 2. Sync Local Lots to Backend
            val lots = database.lotDao().getAllLots().first()
            var lotsSynced = 0
            for (lot in lots) {
                try {
                    val backendLot = BackendLot(
                        lotId = lot.lotId,
                        collectorId = lot.userId,
                        collectorName = lot.collectorName,
                        collectorEmail = lot.collectorEmail,
                        collectorPhone = lot.collectorPhone,
                        recyclerId = lot.recyclerId,
                        recyclerName = lot.recyclerName,
                        recyclerLocation = lot.recyclerLocation,
                        recyclerPricePerKg = lot.recyclerPricePerKg,
                        materialId = lot.materialId,
                        weight = lot.weight,
                        estimatedMinValue = lot.estimatedMinValue,
                        estimatedMaxValue = lot.estimatedMaxValue,
                        description = lot.description,
                        status = lot.status.uppercase()
                    )
                    RetrofitClient.api.createLot(backendLot)
                    lotsSynced++
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync lot ${lot.lotId}: ${e.message}")
                }
            }
            Log.d(TAG, "Synced $lotsSynced / ${lots.size} lots to backend")

            // 3. Sync Local Requests to Backend
            val requests = database.requestDao().getAllRequests().first()
            var requestsSynced = 0
            for (req in requests) {
                try {
                    val backendReq = BackendRequest(
                        requestId = req.requestId,
                        lotId = req.lotId.toString(),
                        collectorId = req.userId,
                        collectorName = req.collectorName,
                        collectorPhone = req.collectorPhone,
                        recyclerId = req.recyclerId.toString(),
                        recyclerName = req.recyclerName,
                        recyclerLocation = req.recyclerLocation,
                        pricePerKg = req.pricePerKg,
                        status = req.status.uppercase()
                    )
                    RetrofitClient.api.createRequest(backendReq)
                    requestsSynced++
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync request ${req.requestId}: ${e.message}")
                }
            }
            Log.d(TAG, "Synced $requestsSynced / ${requests.size} requests to backend")

            // 4. Fetch User's Transactions from Backend
            try {
                val transactions = RetrofitClient.api.getTransactions()
                Log.d(TAG, "Fetched ${transactions.size} transactions from backend")
            } catch (e: Exception) {
                Log.w(TAG, "Transaction query notice: ${e.message}")
            }

            Log.d(TAG, "SyncWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker failed", e)
            Result.retry()
        }
    }
}
