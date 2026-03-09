package de.stroebele.mindyourself.sync

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import de.stroebele.mindyourself.sync.model.HeartRateDto
import de.stroebele.mindyourself.sync.model.HydrationLogDto
import de.stroebele.mindyourself.sync.model.SupplementLogDto
import de.stroebele.mindyourself.sync.model.SyncPaths
import de.stroebele.mindyourself.sync.model.SyncSerializer.toHeartRateList
import de.stroebele.mindyourself.sync.model.SyncSerializer.toHydrationLogList
import de.stroebele.mindyourself.sync.model.SyncSerializer.toSupplementLogList

/**
 * Runs on the Phone. Receives DataItems pushed from the Watch (log data).
 * Must be subclassed in :app to inject repositories via Hilt.
 */
abstract class PhoneSyncListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        events.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) return@forEach
            val path = event.dataItem.uri.path ?: return@forEach
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

            when (path) {
                SyncPaths.HYDRATION_LOGS -> {
                    val bytes = dataMap.getByteArray("logs") ?: return@forEach
                    val logs = bytes.toHydrationLogList()
                    Log.d(TAG, "Received ${logs.size} hydration logs from watch")
                    onHydrationLogsReceived(logs)
                }
                SyncPaths.SUPPLEMENT_LOGS -> {
                    val bytes = dataMap.getByteArray("logs") ?: return@forEach
                    val logs = bytes.toSupplementLogList()
                    Log.d(TAG, "Received ${logs.size} supplement logs from watch")
                    onSupplementLogsReceived(logs)
                }
                SyncPaths.HEART_RATE_LOGS -> {
                    val bytes = dataMap.getByteArray("logs") ?: return@forEach
                    val logs = bytes.toHeartRateList()
                    Log.d(TAG, "Received ${logs.size} heart rate entries from watch")
                    onHeartRateLogsReceived(logs)
                }
            }
        }
    }

    abstract fun onHydrationLogsReceived(logs: List<HydrationLogDto>)
    abstract fun onSupplementLogsReceived(logs: List<SupplementLogDto>)
    abstract fun onHeartRateLogsReceived(logs: List<HeartRateDto>)

    companion object {
        private const val TAG = "PhoneSyncListenerService"
    }
}
