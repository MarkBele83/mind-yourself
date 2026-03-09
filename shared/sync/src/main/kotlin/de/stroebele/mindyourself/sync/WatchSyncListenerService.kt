package de.stroebele.mindyourself.sync

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import de.stroebele.mindyourself.sync.model.ReminderConfigDto
import de.stroebele.mindyourself.sync.model.SyncPaths
import de.stroebele.mindyourself.sync.model.SyncSerializer.toReminderConfigList
import de.stroebele.mindyourself.sync.model.SyncSerializer.toVacationSettings
import de.stroebele.mindyourself.sync.model.VacationSettingsDto

/**
 * Runs on the Watch. Receives DataItems pushed from the Phone (reminder configs).
 * Must be subclassed in :wear to inject repositories via Hilt.
 */
abstract class WatchSyncListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        events.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) return@forEach
            val path = event.dataItem.uri.path ?: return@forEach

            when (path) {
                SyncPaths.REMINDER_CONFIGS -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val bytes = dataMap.getByteArray("configs") ?: return@forEach
                    val configs = bytes.toReminderConfigList()
                    Log.d(TAG, "Received ${configs.size} reminder configs from phone")
                    onReminderConfigsReceived(configs)
                }
                SyncPaths.VACATION_SETTINGS -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val bytes = dataMap.getByteArray("vacation") ?: return@forEach
                    val dto = bytes.toVacationSettings()
                    Log.d(TAG, "Received vacation settings from phone: enabled=${dto.enabled}")
                    onVacationSettingsReceived(dto)
                }
            }
        }
    }

    /**
     * Called when new reminder configs arrive from the Phone.
     * Implement in :wear to persist configs to Room DB.
     */
    abstract fun onReminderConfigsReceived(configs: List<ReminderConfigDto>)

    /**
     * Called when vacation settings arrive from the Phone.
     * Implement in :wear to persist settings via VacationSettingsRepository.
     */
    abstract fun onVacationSettingsReceived(dto: VacationSettingsDto)

    companion object {
        private const val TAG = "WatchSyncListenerService"
    }
}
