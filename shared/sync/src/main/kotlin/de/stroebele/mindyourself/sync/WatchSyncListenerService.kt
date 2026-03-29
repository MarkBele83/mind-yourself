package de.stroebele.mindyourself.sync

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import de.stroebele.mindyourself.sync.model.AppSettingsDto
import de.stroebele.mindyourself.sync.model.HydrationExternalLogDto
import de.stroebele.mindyourself.sync.model.ReminderConfigDto
import de.stroebele.mindyourself.sync.model.SyncPaths
import de.stroebele.mindyourself.sync.model.SyncSerializer.toAppSettings
import de.stroebele.mindyourself.sync.model.SyncSerializer.toHcHydrationLogList
import de.stroebele.mindyourself.sync.model.SyncSerializer.toReminderConfigList
import de.stroebele.mindyourself.sync.model.SyncSerializer.toVacationSettings
import de.stroebele.mindyourself.sync.model.VacationSettingsDto

/**
 * Runs on the Watch. Receives DataItems pushed from the Phone (reminder configs).
 * Must be subclassed in :wear to inject repositories via Hilt.
 */
abstract class WatchSyncListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        Log.d(TAG, "onDataChanged: ${events.count} event(s)")
        events.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "  Skipping event type=${event.type} (not TYPE_CHANGED)")
                return@forEach
            }
            val path = event.dataItem.uri.path
            Log.d(TAG, "  Event: path=$path uri=${event.dataItem.uri}")

            if (path == null) {
                Log.w(TAG, "  Skipping event with null path")
                return@forEach
            }

            when (path) {
                SyncPaths.REMINDER_CONFIGS -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val bytes = dataMap.getByteArray("configs")
                    if (bytes == null) {
                        Log.e(TAG, "  REMINDER_CONFIGS: 'configs' byte array is null")
                        return@forEach
                    }
                    Log.d(TAG, "  REMINDER_CONFIGS: received ${bytes.size} bytes")
                    try {
                        val configs = bytes.toReminderConfigList()
                        Log.i(TAG, "  REMINDER_CONFIGS: deserialized ${configs.size} config(s)")
                        onReminderConfigsReceived(configs)
                    } catch (e: Exception) {
                        Log.e(TAG, "  REMINDER_CONFIGS: deserialization failed", e)
                    }
                }
                SyncPaths.VACATION_SETTINGS -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val bytes = dataMap.getByteArray("vacation")
                    if (bytes == null) {
                        Log.e(TAG, "  VACATION_SETTINGS: 'vacation' byte array is null")
                        return@forEach
                    }
                    Log.d(TAG, "  VACATION_SETTINGS: received ${bytes.size} bytes")
                    try {
                        val dto = bytes.toVacationSettings()
                        Log.i(TAG, "  VACATION_SETTINGS: deserialized ${dto.periods.size} period(s)")
                        onVacationSettingsReceived(dto)
                    } catch (e: Exception) {
                        Log.e(TAG, "  VACATION_SETTINGS: deserialization failed", e)
                    }
                }
                SyncPaths.APP_SETTINGS -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val bytes = dataMap.getByteArray("settings")
                    if (bytes == null) {
                        Log.e(TAG, "  APP_SETTINGS: 'settings' byte array is null")
                        return@forEach
                    }
                    Log.d(TAG, "  APP_SETTINGS: received ${bytes.size} bytes")
                    try {
                        val dto = bytes.toAppSettings()
                        Log.i(TAG, "  APP_SETTINGS: stepDailyGoal=${dto.stepDailyGoal}")
                        onAppSettingsReceived(dto)
                    } catch (e: Exception) {
                        Log.e(TAG, "  APP_SETTINGS: deserialization failed", e)
                    }
                }
                SyncPaths.HC_HYDRATION_LOGS -> {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val bytes = dataMap.getByteArray("logs")
                    if (bytes == null) {
                        Log.e(TAG, "  HC_HYDRATION_LOGS: 'logs' byte array is null")
                        return@forEach
                    }
                    Log.d(TAG, "  HC_HYDRATION_LOGS: received ${bytes.size} bytes")
                    try {
                        val logs = bytes.toHcHydrationLogList()
                        Log.i(TAG, "  HC_HYDRATION_LOGS: deserialized ${logs.size} log(s)")
                        onHcHydrationLogsReceived(logs)
                    } catch (e: Exception) {
                        Log.e(TAG, "  HC_HYDRATION_LOGS: deserialization failed", e)
                    }
                }
                else -> {
                    Log.d(TAG, "  Ignoring unknown path: $path")
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

    /**
     * Called when app settings arrive from the Phone (e.g. step daily goal).
     * Implement in :wear to persist settings via AppSettingsRepository.
     */
    abstract fun onAppSettingsReceived(dto: AppSettingsDto)

    /**
     * Called when Health Connect hydration logs arrive from the Phone.
     * Implement in :wear to persist logs via HydrationRepository (with deduplication).
     */
    abstract fun onHcHydrationLogsReceived(logs: List<HydrationExternalLogDto>)

    companion object {
        private const val TAG = "WatchSyncListener"
    }
}
