package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.sync.PhoneSyncUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val syncState: SyncState = SyncState.IDLE,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val phoneSyncUseCase: PhoneSyncUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun sync() {
        if (_uiState.value.syncState == SyncState.SYNCING) return
        viewModelScope.launch {
            _uiState.update { it.copy(syncState = SyncState.SYNCING) }
            runCatching { phoneSyncUseCase() }
                .onSuccess { _uiState.update { it.copy(syncState = SyncState.SUCCESS) } }
                .onFailure { _uiState.update { it.copy(syncState = SyncState.ERROR) } }
        }
    }

    fun resetSyncState() {
        _uiState.update { it.copy(syncState = SyncState.IDLE) }
    }
}
