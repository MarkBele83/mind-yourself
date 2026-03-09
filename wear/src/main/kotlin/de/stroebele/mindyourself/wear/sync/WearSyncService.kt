package de.stroebele.mindyourself.wear.sync

import dagger.hilt.android.AndroidEntryPoint
import de.stroebele.mindyourself.data.db.mapper.deserializeTypeConfig
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.model.VacationPeriod
import de.stroebele.mindyourself.domain.model.VacationSettings
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import de.stroebele.mindyourself.domain.repository.VacationSettingsRepository
import de.stroebele.mindyourself.sync.WatchSyncListenerService
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReminderConfigsReceived(configs: List<ReminderConfigDto>) {
        scope.launch {
            val domainConfigs = configs.mapNotNull { dto ->
                runCatching { dto.toDomain() }.getOrNull()
            }
            reminderConfigRepository.replaceAll(domainConfigs)
        }
    }

    override fun onVacationSettingsReceived(dto: VacationSettingsDto) {
        scope.launch {
            val periods = dto.periods.map { period ->
                VacationPeriod(
                    from = LocalDateTime.ofInstant(Instant.ofEpochMilli(period.fromEpochMs), ZoneId.systemDefault()),
                    until = LocalDateTime.ofInstant(Instant.ofEpochMilli(period.untilEpochMs), ZoneId.systemDefault()),
                )
            }
            vacationSettingsRepository.save(VacationSettings(periods = periods))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
}
