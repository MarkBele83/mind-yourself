package de.stroebele.mindyourself.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import de.stroebele.mindyourself.sync.model.HeartRateDto
import de.stroebele.mindyourself.sync.model.HydrationLogDto
import de.stroebele.mindyourself.sync.model.ReminderConfigDto
import de.stroebele.mindyourself.sync.model.SupplementLogDto
import de.stroebele.mindyourself.sync.model.SyncPaths
import de.stroebele.mindyourself.sync.model.SyncSerializer.heartRateToJsonBytes
import de.stroebele.mindyourself.sync.model.SyncSerializer.hydrationToJsonBytes
import de.stroebele.mindyourself.sync.model.SyncSerializer.supplementToJsonBytes
import de.stroebele.mindyourself.sync.model.SyncSerializer.toJsonBytes
import de.stroebele.mindyourself.sync.model.SyncSerializer.toVacationJsonBytes
import de.stroebele.mindyourself.sync.model.VacationSettingsDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the Wear Data Layer API (DataClient) for Watch ↔ Phone sync.
 *
 * Design decisions (Nutzerkontrolle + Akku-Effizienz):
 * - All sync is triggered explicitly by the user, never automatically.
 * - DataItems are used for configuration (persistent, survives reconnect).
 * - Messages (fire-and-forget) are used only for handshake signals.
 */
@Singleton
class WearDataClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }

    // ── Phone → Watch: push reminder configs ─────────────────────────────────

    /** Called from Phone App when user saves config and taps "Sync". */
    suspend fun pushReminderConfigs(configs: List<ReminderConfigDto>) {
        val request = PutDataMapRequest.create(SyncPaths.REMINDER_CONFIGS).apply {
            dataMap.putByteArray("configs", configs.toJsonBytes())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()

        try {
            dataClient.putDataItem(request).await()
            Log.d(TAG, "Pushed ${configs.size} reminder configs to watch")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push reminder configs", e)
            throw e
        }
    }

    // ── Phone → Watch: push vacation settings ────────────────────────────────

    suspend fun pushVacationSettings(dto: VacationSettingsDto) {
        val request = PutDataMapRequest.create(SyncPaths.VACATION_SETTINGS).apply {
            dataMap.putByteArray("vacation", dto.toVacationJsonBytes())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        try {
            dataClient.putDataItem(request).await()
            Log.d(TAG, "Pushed vacation settings to watch")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push vacation settings", e)
            throw e
        }
    }

    // ── Watch → Phone: push log data ─────────────────────────────────────────

    /** Called from Watch App when user triggers sync. */
    suspend fun pushHydrationLogs(logs: List<HydrationLogDto>) {
        val request = PutDataMapRequest.create(SyncPaths.HYDRATION_LOGS).apply {
            dataMap.putByteArray("logs", logs.hydrationToJsonBytes())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()

        try {
            dataClient.putDataItem(request).await()
            Log.d(TAG, "Pushed ${logs.size} hydration logs to phone")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push hydration logs", e)
            throw e
        }
    }

    suspend fun pushSupplementLogs(logs: List<SupplementLogDto>) {
        val request = PutDataMapRequest.create(SyncPaths.SUPPLEMENT_LOGS).apply {
            dataMap.putByteArray("logs", logs.supplementToJsonBytes())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()

        try {
            dataClient.putDataItem(request).await()
            Log.d(TAG, "Pushed ${logs.size} supplement logs to phone")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push supplement logs", e)
            throw e
        }
    }

    suspend fun pushHeartRateLogs(logs: List<HeartRateDto>) {
        val request = PutDataMapRequest.create(SyncPaths.HEART_RATE_LOGS).apply {
            dataMap.putByteArray("logs", logs.heartRateToJsonBytes())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()

        try {
            dataClient.putDataItem(request).await()
            Log.d(TAG, "Pushed ${logs.size} heart rate entries to phone")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push heart rate logs", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "WearDataClient"
    }
}
