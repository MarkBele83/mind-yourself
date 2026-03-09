package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderListUiState(
    val configs: List<ReminderConfig> = emptyList(),
)

@HiltViewModel
class ReminderListViewModel @Inject constructor(
    private val reminderConfigRepository: ReminderConfigRepository,
) : ViewModel() {

    val uiState: StateFlow<ReminderListUiState> =
        reminderConfigRepository.observeAll()
            .map { ReminderListUiState(configs = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ReminderListUiState(),
            )

    fun delete(id: Long) {
        viewModelScope.launch { reminderConfigRepository.delete(id) }
    }

    fun toggleEnabled(config: ReminderConfig) {
        viewModelScope.launch {
            reminderConfigRepository.save(config.copy(enabled = !config.enabled))
        }
    }
}
