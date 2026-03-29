package de.stroebele.mindyourself.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import de.stroebele.mindyourself.healthconnect.HealthConnectManager
import de.stroebele.mindyourself.sync.WearDataClient
import java.util.concurrent.TimeUnit

/**
 * Periodic background worker that keeps local data and Health Connect in sync.
 *
 * Responsibilities:
 *  1. Re-write recent local hydration logs to HC (idempotent via clientRecordId).
 *  2. Read external HC hydration logs (from other apps / phone-entered) and push
 *     them to the watch so the watch can factor them into reminder decisions.
 *
 * The manual sync in [de.stroebele.mindyourself.sync.PhoneSyncUseCase] still handles
 * this as part of the full sync, so both paths stay consistent.
 *
 * Scheduled every [INTERVAL_MINUTES] minutes with KEEP policy — re-enqueueing on
 * every process start is a safe no-op if the work is already scheduled.
 */
@HiltWorker
class HcSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val hydrationRepository: HydrationRepository,
    private val healthConnectManager: HealthConnectManager,
    private val wearDataClient: WearDataClient,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "HC background sync started")
        return try {
            // 1. Write local logs to HC (deduplication via clientRecordId)
            val recentLogs = hydrationRepository.getRecentLogs(days = 7)
            healthConnectManager.writeHydrationLogs(recentLogs)
            Log.d(TAG, "Re-wrote ${recentLogs.size} local hydration logs to HC")

            // 2. Pull external HC logs and push to watch
            val externalLogs = healthConnectManager.readExternalHydrationLogs()
            if (externalLogs.isNotEmpty()) {
                wearDataClient.pushHcHydrationLogs(externalLogs)
                Log.d(TAG, "Pushed ${externalLogs.size} external HC logs to watch")
            }

            Log.d(TAG, "HC background sync completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "HC background sync failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "HcSyncWorker"
        private const val WORK_NAME = "hc_sync_periodic"
        private const val INTERVAL_MINUTES = 30L

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<HcSyncWorker>(
                INTERVAL_MINUTES, TimeUnit.MINUTES,
            ).build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
