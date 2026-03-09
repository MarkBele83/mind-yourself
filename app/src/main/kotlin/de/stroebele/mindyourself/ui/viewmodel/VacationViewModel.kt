package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.model.VacationPeriod
import de.stroebele.mindyourself.domain.model.VacationSettings
import de.stroebele.mindyourself.domain.repository.VacationSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class VacationUiState(
    val settings: VacationSettings = VacationSettings(),
    val isCurrentlyInVacation: Boolean = false,
    val overlapError: Boolean = false,
)

@HiltViewModel
class VacationViewModel @Inject constructor(
    private val vacationSettingsRepository: VacationSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VacationUiState())
    val uiState: StateFlow<VacationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            vacationSettingsRepository.observe().collect { settings ->
                val now = LocalDateTime.now()
                val inVacation = settings.periods.any { now >= it.from && now <= it.until }
                _uiState.update { it.copy(settings = settings, isCurrentlyInVacation = inVacation) }
            }
        }
    }

    fun addPeriod(from: LocalDateTime, until: LocalDateTime) {
        val settings = _uiState.value.settings
        val hasOverlap = settings.periods.any { existing ->
            from < existing.until && existing.from < until
        }
        if (hasOverlap) {
            _uiState.update { it.copy(overlapError = true) }
            return
        }
        val sorted = (settings.periods + VacationPeriod(from, until)).sortedBy { it.from }
        viewModelScope.launch {
            vacationSettingsRepository.save(settings.copy(periods = sorted))
        }
    }

    fun removePeriod(index: Int) {
        val settings = _uiState.value.settings
        if (index < 0 || index >= settings.periods.size) return
        val newPeriods = settings.periods.toMutableList().also { it.removeAt(index) }
        viewModelScope.launch {
            vacationSettingsRepository.save(settings.copy(periods = newPeriods))
        }
    }

    fun clearOverlapError() {
        _uiState.update { it.copy(overlapError = false) }
    }
}
