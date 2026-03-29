package de.stroebele.mindyourself.wear.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import de.stroebele.mindyourself.data.db.mapper.deserializeTypeConfig
import de.stroebele.mindyourself.domain.model.AppSettings
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.model.VacationPeriod
import de.stroebele.mindyourself.domain.model.VacationSettings
import de.stroebele.mindyourself.domain.repository.AppSettingsRepository
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import de.stroebele.mindyourself.domain.repository.VacationSettingsRepository
import de.stroebele.mindyourself.sync.model.AppSettingsDto
import de.stroebele.mindyourself.sync.model.ReminderConfigDto
import de.stroebele.mindyourself.sync.model.SyncPaths
import de.stroebele.mindyourself.sync.model.SyncSerializer.toAppSettings
import de.stroebele.mindyourself.sync.model.SyncSerializer.toReminderConfigList
import de.stroebele.mindyourself.sync.model.SyncSerializer.toVacationSettings
import de.stroebele.mindyourself.sync.model.VacationSettingsDto
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Runs once on process start. If the local reminder DB is empty (e.g. after a
 * fresh install or uninstall+reinstall), fetches any existing DataItems from the
 * Wear Data Layer and applies them — so the phone does not need to manually
 * re-sync after reinstalling the watch app.
 *
 * DataItems are persistent on the Data Layer and survive watch reinstalls as
 * long as the phone app is still installed.
 */
class WearStartupRestoreUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderConfigRepository: ReminderConfigRepository,
    private val vacationSettingsRepository: VacationSettingsRepository,
    private val appSettingsRepository: AppSettingsRepository,
) {

    suspend operator fun invoke() {
        val existing = reminderConfigRepository.getAll()
        if (existing.isNotEmpty()) {
            Log.d(TAG, "DB has ${existing.size} reminder configs — no restore needed")
            return
        }

        Log.i(TAG, "Reminder DB is empty — attempting DataLayer restore")
        try {
            val dataClient = Wearable.getDataClient(context)
            val items = dataClient.dataItems.await()

            var restored = false
            items.forEach { dataItem ->
                val path = dataItem.uri.path ?: return@forEach
                Log.d(TAG, "Found DataLayer item: $path")
                when (path) {
                    SyncPaths.REMINDER_CONFIGS -> {
                        val bytes = DataMapItem.fromDataItem(dataItem).dataMap
                            .getByteArray("configs") ?: return@forEach
                        val configs = bytes.toReminderConfigList()
                            .mapNotNull { runCatching { it.toDomain() }.getOrNull() }
                        if (configs.isNotEmpty()) {
                            reminderConfigRepository.replaceAll(configs)
                            Log.i(TAG, "Restored ${configs.size} reminder configs from DataLayer")
                            restored = true
                        }
                    }
                    SyncPaths.VACATION_SETTINGS -> {
                        val bytes = DataMapItem.fromDataItem(dataItem).dataMap
                            .getByteArray("vacation") ?: return@forEach
                        runCatching {
                            val dto = bytes.toVacationSettings()
                            val periods = dto.periods.map { p ->
                                VacationPeriod(
                                    from = LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(p.fromEpochMs), ZoneId.systemDefault()
                                    ),
                                    until = LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(p.untilEpochMs), ZoneId.systemDefault()
                                    ),
                                )
                            }
                            vacationSettingsRepository.save(VacationSettings(periods = periods))
                            Log.i(TAG, "Restored vacation settings from DataLayer")
                        }
                    }
                    SyncPaths.APP_SETTINGS -> {
                        val bytes = DataMapItem.fromDataItem(dataItem).dataMap
                            .getByteArray("settings") ?: return@forEach
                        runCatching {
                            val dto = bytes.toAppSettings()
                            appSettingsRepository.save(AppSettings(stepDailyGoal = dto.stepDailyGoal))
                            Log.i(TAG, "Restored app settings from DataLayer")
                        }
                    }
                }
            }
            items.release()

            if (!restored) {
                Log.i(TAG, "No reminder configs found in DataLayer — phone sync required")
            }
        } catch (e: Exception) {
            Log.e(TAG, "DataLayer restore failed", e)
        }
    }

    private fun ReminderConfigDto.toDomain(): ReminderConfig {
        val reminderType = ReminderType.valueOf(type)
        val typeConfig = deserializeTypeConfig(reminderType, typeConfigJson)
        val days = activeDays.split(",")
            .mapNotNull { runCatching { DayOfWeek.valueOf(it.trim()) }.getOrNull() }
            .toSet()
            .ifEmpty { DayOfWeek.entries.toSet() }
        return ReminderConfig(
            id = id,
            type = reminderType,
            enabled = enabled,
            label = label,
            activeDays = days,
            activeFrom = LocalTime.of(activeFromHour, activeFromMinute),
            activeUntil = LocalTime.of(activeUntilHour, activeUntilMinute),
            typeConfig = typeConfig,
            activeInVacation = activeInVacation,
        )
    }

    companion object {
        private const val TAG = "WearStartupRestore"
    }
}
