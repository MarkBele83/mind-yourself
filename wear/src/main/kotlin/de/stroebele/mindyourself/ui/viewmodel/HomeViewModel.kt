package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.model.SupplementConfig
import de.stroebele.mindyourself.domain.repository.HealthCacheRepository
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val todayHydrationMl: Int = 0,
    val todaySteps: Long = 0L,
    val supplementNames: List<String> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val hydrationRepository: HydrationRepository,
    private val healthCacheRepository: HealthCacheRepository,
    private val reminderConfigRepository: ReminderConfigRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        hydrationRepository.observeToday(),
        healthCacheRepository.observeTodaySteps(),
        reminderConfigRepository.observeAll(),
    ) { hydrationLogs, steps, configs ->
        HomeUiState(
            todayHydrationMl = hydrationLogs.sumOf { it.amountMl },
            todaySteps = steps,
            supplementNames = configs
                .filter { it.type == ReminderType.SUPPLEMENT && it.enabled }
                .map { (it.typeConfig as SupplementConfig).supplementName },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState(),
    )
}
