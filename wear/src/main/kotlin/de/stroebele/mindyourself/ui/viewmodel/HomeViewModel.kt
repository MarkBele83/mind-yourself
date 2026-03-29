package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.model.AppSettings
import de.stroebele.mindyourself.domain.model.HydrationConfig
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.model.SupplementConfig
import de.stroebele.mindyourself.domain.model.VacationSettings
import de.stroebele.mindyourself.domain.repository.AppSettingsRepository
import de.stroebele.mindyourself.domain.repository.HealthCacheRepository
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import de.stroebele.mindyourself.domain.repository.SupplementRepository
import de.stroebele.mindyourself.domain.repository.VacationSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val todayHydrationMl: Int = 0,
    val hydrationGoalMl: Int = 0,
    val hydrationPercent: Int = 0,
    val todaySteps: Long = 0L,
    val stepDailyGoal: Int = AppSettings().stepDailyGoal,
    val stepsPercent: Int = 0,
    val supplementNames: List<String> = emptyList(),
    val supplementsTakenToday: Int = 0,
    val supplementsTotalToday: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val hydrationRepository: HydrationRepository,
    private val healthCacheRepository: HealthCacheRepository,
    private val reminderConfigRepository: ReminderConfigRepository,
    private val vacationSettingsRepository: VacationSettingsRepository,
    private val supplementRepository: SupplementRepository,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    // Combine reminder configs + vacation into one flow to reduce nesting depth
    private val configsAndVacation = combine(
        reminderConfigRepository.observeAll(),
        vacationSettingsRepository.observe(),
    ) { configs, vacation -> configs to vacation }

    val uiState: StateFlow<HomeUiState> = combine(
        hydrationRepository.observeToday(),
        healthCacheRepository.observeTodaySteps(),
        configsAndVacation,
        supplementRepository.observeToday(),
        appSettingsRepository.observe(),
    ) { hydrationLogs, steps, (configs, vacation), supplementLogs, appSettings ->
        val today = LocalDate.now()

        val hydrationGoalMl = if (appSettings.hydrationDailyGoalMl > 0)
            appSettings.hydrationDailyGoalMl
        else
            configs
                .filter { it.type == ReminderType.HYDRATION && isActiveToday(it, today, vacation) }
                .sumOf { (it.typeConfig as HydrationConfig).reminderGoalMl }

        val todayHydrationMl = hydrationLogs.sumOf { it.amountMl }
        val hydrationPercent = if (hydrationGoalMl > 0)
            (todayHydrationMl * 100 / hydrationGoalMl).coerceAtMost(100)
        else 0

        val stepsPercent = if (appSettings.stepDailyGoal > 0)
            (steps * 100 / appSettings.stepDailyGoal).toInt().coerceAtMost(100)
        else 0

        val supplementNames = configs
            .filter { it.type == ReminderType.SUPPLEMENT && it.enabled }
            .flatMap { (it.typeConfig as SupplementConfig).items.map { item -> item.name } }

        val supplementsTotalToday = configs
            .filter { it.type == ReminderType.SUPPLEMENT && isActiveToday(it, today, vacation) }
            .sumOf { (it.typeConfig as SupplementConfig).items.sumOf { item -> item.amount } }

        HomeUiState(
            todayHydrationMl = todayHydrationMl,
            hydrationGoalMl = hydrationGoalMl,
            hydrationPercent = hydrationPercent,
            todaySteps = steps,
            stepDailyGoal = appSettings.stepDailyGoal,
            stepsPercent = stepsPercent,
            supplementNames = supplementNames,
            supplementsTakenToday = supplementLogs.size,
            supplementsTotalToday = supplementsTotalToday,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState(),
    )

    private fun isActiveToday(config: ReminderConfig, today: LocalDate, vacation: VacationSettings): Boolean {
        val isVacation = vacation.periods.any { p ->
            !today.isBefore(p.from.toLocalDate()) && !today.isAfter(p.until.toLocalDate())
        }
        return config.enabled &&
                today.dayOfWeek in config.activeDays &&
                (config.activeInVacation || !isVacation)
    }
}
