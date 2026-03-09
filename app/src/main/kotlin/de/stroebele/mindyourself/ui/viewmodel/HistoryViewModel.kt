package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.model.HydrationLog
import de.stroebele.mindyourself.domain.model.SupplementLog
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import de.stroebele.mindyourself.domain.repository.SupplementRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HistoryUiState(
    val hydrationLogs: List<HydrationLog> = emptyList(),
    val supplementLogs: List<SupplementLog> = emptyList(),
    val todayHydrationMl: Int = 0,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    hydrationRepository: HydrationRepository,
    supplementRepository: SupplementRepository,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        hydrationRepository.observeToday(),
        supplementRepository.observeToday(),
    ) { hydrationLogs, supplementLogs ->
        HistoryUiState(
            hydrationLogs = hydrationLogs,
            supplementLogs = supplementLogs,
            todayHydrationMl = hydrationLogs.sumOf { it.amountMl },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )
}
