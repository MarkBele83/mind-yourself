package de.stroebele.mindyourself.sync

import android.util.Log
import de.stroebele.mindyourself.data.db.mapper.serializeTypeConfig
import de.stroebele.mindyourself.domain.model.HydrationConfig
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.repository.AppSettingsRepository
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import de.stroebele.mindyourself.domain.repository.VacationSettingsRepository
import de.stroebele.mindyourself.healthconnect.HealthConnectManager
import de.stroebele.mindyourself.sync.model.AppSettingsDto
import de.stroebele.mindyourself.sync.model.ReminderConfigDto
import de.stroebele.mindyourself.sync.model.VacationPeriodDto
import de.stroebele.mindyourself.sync.model.VacationSettingsDto
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import javax.inject.Inject

/**
 * Orchestrates the manual Phone → Watch sync triggered by the user.
 * Pushes all reminder configs and vacation settings to the Watch via Data Layer API.
 */
class PhoneSyncUseCase @Inject constructor(
    private val reminderConfigRepository: ReminderConfigRepository,
    private val vacationSettingsRepository: VacationSettingsRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val hydrationRepository: HydrationRepository,
    private val healthConnectManager: HealthConnectManager,
    private val wearDataClient: WearDataClient,
) {
    suspend operator fun invoke() {
        Log.i(TAG, "Sync started")

        // 1. Reminder configs
        val configs = reminderConfigRepository.getAll()
        Log.d(TAG, "Fetched ${configs.size} reminder configs from DB")
        configs.forEach { c ->
            Log.d(TAG, "  Config[${c.id}] type=${c.type} enabled=${c.enabled} label=\"${c.label}\" " +
                    "days=${c.activeDays.joinToString(",") { it.name }} " +
                    "window=${c.activeFrom}-${c.activeUntil} vacation=${c.activeInVacation}")
        }

        val dtos = configs.map { it.toDto() }
        wearDataClient.pushReminderConfigs(dtos)
        Log.i(TAG, "Reminder configs pushed (${dtos.size})")

        // 2. Vacation settings
        val vacation = vacationSettingsRepository.observe().first()
        Log.d(TAG, "Fetched vacation settings: ${vacation.periods.size} period(s)")
        vacation.periods.forEachIndexed { i, p ->
            Log.d(TAG, "  Period[$i] ${p.from} - ${p.until}")
        }

        wearDataClient.pushVacationSettings(VacationSettingsDto(
            periods = vacation.periods.map { period ->
                VacationPeriodDto(
                    fromEpochMs = period.from.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    untilEpochMs = period.until.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                )
            },
        ))
        Log.i(TAG, "Vacation settings pushed (${vacation.periods.size} periods)")
        // 3. App settings
        val appSettings = appSettingsRepository.observe().first()
        val hydrationDailyGoalMl = configs
            .filter { it.enabled && it.type == ReminderType.HYDRATION }
            .sumOf { (it.typeConfig as HydrationConfig).reminderGoalMl }
        Log.d(TAG, "Fetched app settings: stepDailyGoal=${appSettings.stepDailyGoal} hydrationDailyGoalMl=$hydrationDailyGoalMl")
        wearDataClient.pushAppSettings(AppSettingsDto(
            stepDailyGoal = appSettings.stepDailyGoal,
            hydrationDailyGoalMl = hydrationDailyGoalMl,
        ))
        Log.i(TAG, "App settings pushed")

        // 4. Re-write recent local hydration logs to Health Connect (sync resilience)
        //    HC deduplicates via clientRecordId — re-writing is a no-op for existing records
        val recentLogs = hydrationRepository.getRecentLogs(days = 7)
        Log.d(TAG, "Re-writing ${recentLogs.size} recent hydration logs to HC")
        healthConnectManager.writeHydrationLogs(recentLogs)

        // 5. Pull external HC hydration logs and push to watch
        val externalHcLogs = healthConnectManager.readExternalHydrationLogs()
        Log.d(TAG, "Received ${externalHcLogs.size} external HC hydration logs")
        if (externalHcLogs.isNotEmpty()) {
            wearDataClient.pushHcHydrationLogs(externalHcLogs)
            Log.i(TAG, "Pushed ${externalHcLogs.size} external HC hydration logs to watch")
        }

        Log.i(TAG, "Sync completed successfully")
    }

    private fun ReminderConfig.toDto() = ReminderConfigDto(
        id = id,
        type = type.name,
        enabled = enabled,
        label = label,
        activeDays = activeDays.joinToString(",") { it.name },
        activeFromHour = activeFrom.hour,
        activeFromMinute = activeFrom.minute,
        activeUntilHour = activeUntil.hour,
        activeUntilMinute = activeUntil.minute,
        typeConfigJson = serializeTypeConfig(typeConfig),
        activeInVacation = activeInVacation,
    )

    companion object {
        private const val TAG = "PhoneSyncUseCase"
    }
}
