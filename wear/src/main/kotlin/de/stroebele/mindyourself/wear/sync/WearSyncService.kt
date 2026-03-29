package de.stroebele.mindyourself.wear.sync

import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import de.stroebele.mindyourself.data.db.mapper.deserializeTypeConfig
import de.stroebele.mindyourself.domain.model.AppSettings
import de.stroebele.mindyourself.domain.model.HydrationLog
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.model.VacationPeriod
import de.stroebele.mindyourself.domain.model.VacationSettings
import de.stroebele.mindyourself.domain.repository.AppSettingsRepository
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import de.stroebele.mindyourself.domain.repository.VacationSettingsRepository
import de.stroebele.mindyourself.sync.WatchSyncListenerService
import de.stroebele.mindyourself.sync.model.AppSettingsDto
import de.stroebele.mindyourself.sync.model.HydrationExternalLogDto
import de.stroebele.mindyourself.sync.model.ReminderConfigDto
import de.stroebele.mindyourself.sync.model.VacationSettingsDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Concrete Watch-side Wearable Listener.
 * Receives reminder configs pushed from the Phone and persists them to Room DB.
 */
@AndroidEntryPoint
class WearSyncService : WatchSyncListenerService() {

    @Inject
    lateinit var reminderConfigRepository: ReminderConfigRepository

    @Inject
    lateinit var vacationSettingsRepository: VacationSettingsRepository

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    @Inject
    lateinit var hydrationRepository: HydrationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReminderConfigsReceived(configs: List<ReminderConfigDto>) {
        Log.i(TAG, "Processing ${configs.size} reminder config DTOs")
        scope.launch {
            val domainConfigs = configs.mapNotNull { dto ->
                runCatching { dto.toDomain() }
                    .onSuccess { config ->
                        Log.d(TAG, "  Converted Config[${config.id}] type=${config.type} " +
                                "enabled=${config.enabled} label=\"${config.label}\"")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "  Failed to convert Config[${dto.id}] type=${dto.type} " +
                                "label=\"${dto.label}\": ${e.message}", e)
                    }
                    .getOrNull()
            }
            Log.i(TAG, "Converted ${domainConfigs.size}/${configs.size} configs successfully")

            try {
                reminderConfigRepository.replaceAll(domainConfigs)
                Log.i(TAG, "Saved ${domainConfigs.size} configs to DB (replaceAll)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save configs to DB", e)
            }
        }
    }

    override fun onVacationSettingsReceived(dto: VacationSettingsDto) {
        Log.i(TAG, "Processing vacation settings: ${dto.periods.size} period(s)")
        scope.launch {
            try {
                val periods = dto.periods.mapIndexed { i, period ->
                    val from = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(period.fromEpochMs), ZoneId.systemDefault()
                    )
                    val until = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(period.untilEpochMs), ZoneId.systemDefault()
                    )
                    Log.d(TAG, "  Period[$i] $from - $until")
                    VacationPeriod(from = from, until = until)
                }
                vacationSettingsRepository.save(VacationSettings(periods = periods))
                Log.i(TAG, "Saved ${periods.size} vacation period(s) to DB")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save vacation settings to DB", e)
            }
        }
    }

    override fun onAppSettingsReceived(dto: AppSettingsDto) {
        Log.i(TAG, "Processing app settings: stepDailyGoal=${dto.stepDailyGoal} hydrationDailyGoalMl=${dto.hydrationDailyGoalMl}")
        scope.launch {
            try {
                appSettingsRepository.save(AppSettings(
                    stepDailyGoal = dto.stepDailyGoal,
                    hydrationDailyGoalMl = dto.hydrationDailyGoalMl,
                ))
                Log.i(TAG, "Saved app settings to DataStore")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save app settings", e)
            }
        }
    }

    override fun onHcHydrationLogsReceived(logs: List<HydrationExternalLogDto>) {
        Log.i(TAG, "Processing ${logs.size} HC hydration log(s)")
        scope.launch {
            try {
                val domainLogs = logs.map { dto ->
                    HydrationLog(
                        amountMl = dto.amountMl,
                        timestamp = Instant.ofEpochMilli(dto.timestampEpochMs),
                        synced = true,
                        healthConnectId = dto.healthConnectId,
                    )
                }
                hydrationRepository.saveAll(domainLogs)
                Log.i(TAG, "Saved ${domainLogs.size} HC hydration log(s) to DB (INSERT OR IGNORE)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save HC hydration logs", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        scope.cancel()
    }

    private fun ReminderConfigDto.toDomain(): ReminderConfig {
        val type = ReminderType.valueOf(type)
        return ReminderConfig(
            id = id,
            type = type,
            enabled = enabled,
            label = label,
            activeDays = activeDays.split(",").map { DayOfWeek.valueOf(it.trim()) }.toSet(),
            activeFrom = LocalTime.of(activeFromHour, activeFromMinute),
            activeUntil = LocalTime.of(activeUntilHour, activeUntilMinute),
            typeConfig = deserializeTypeConfig(type, typeConfigJson),
            activeInVacation = activeInVacation,
        )
    }

    companion object {
        private const val TAG = "WearSyncService"
    }
}
