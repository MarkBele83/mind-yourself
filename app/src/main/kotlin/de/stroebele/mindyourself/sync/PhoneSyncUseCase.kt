package de.stroebele.mindyourself.sync

import de.stroebele.mindyourself.data.db.mapper.serializeTypeConfig
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import de.stroebele.mindyourself.domain.repository.VacationSettingsRepository
import de.stroebele.mindyourself.sync.WearDataClient
import de.stroebele.mindyourself.sync.model.ReminderConfigDto
import de.stroebele.mindyourself.sync.model.VacationPeriodDto
import de.stroebele.mindyourself.sync.model.VacationSettingsDto
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import javax.inject.Inject

/**
 * Orchestrates the manual Phone → Watch sync triggered by the user.
 * Pushes all reminder configs to the Watch via Data Layer API.
 */
class PhoneSyncUseCase @Inject constructor(
    private val reminderConfigRepository: ReminderConfigRepository,
    private val vacationSettingsRepository: VacationSettingsRepository,
    private val wearDataClient: WearDataClient,
) {
    suspend operator fun invoke() {
        val configs = reminderConfigRepository.getAll()
        wearDataClient.pushReminderConfigs(configs.map { it.toDto() })

        val vacation = vacationSettingsRepository.observe().first()
        wearDataClient.pushVacationSettings(VacationSettingsDto(
            periods = vacation.periods.map { period ->
                VacationPeriodDto(
                    fromEpochMs = period.from.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    untilEpochMs = period.until.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                )
            },
        ))
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
}
