package de.stroebele.mindyourself.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.model.AppSettings
import de.stroebele.mindyourself.data.repository.BackupRepository
import de.stroebele.mindyourself.domain.repository.AppSettingsRepository
import de.stroebele.mindyourself.healthconnect.HealthConnectManager
import de.stroebele.mindyourself.sync.PhoneSyncUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HcPermissionState { UNKNOWN, NOT_AVAILABLE, MISSING, GRANTED }

data class SettingsUiState(
    val syncState: SyncState = SyncState.IDLE,
    val stepDailyGoal: Int = AppSettings().stepDailyGoal,
    val hcPermissionState: HcPermissionState = HcPermissionState.UNKNOWN,
    /** Non-null while the "CreateDocument" file picker should be triggered. Cleared after use. */
    val pendingBackupJson: String? = null,
    val backupMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val phoneSyncUseCase: PhoneSyncUseCase,
    private val appSettingsRepository: AppSettingsRepository,
    private val healthConnectManager: HealthConnectManager,
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val requiredHcPermissions: Set<String> = healthConnectManager.requiredPermissions

    init {
        appSettingsRepository.observe()
            .onEach { settings -> _uiState.update { it.copy(stepDailyGoal = settings.stepDailyGoal) } }
            .launchIn(viewModelScope)
        checkHcPermissions()
    }

    fun checkHcPermissions() {
        viewModelScope.launch {
            val state = when {
                !healthConnectManager.isAvailable() -> HcPermissionState.NOT_AVAILABLE
                healthConnectManager.hasPermissions() -> HcPermissionState.GRANTED
                else -> HcPermissionState.MISSING
            }
            _uiState.update { it.copy(hcPermissionState = state) }
        }
    }

    fun saveStepGoal(goal: Int) {
        viewModelScope.launch {
            appSettingsRepository.save(AppSettings(stepDailyGoal = goal))
        }
    }

    fun sync() {
        if (_uiState.value.syncState == SyncState.SYNCING) return
        Log.i(TAG, "Sync triggered by user")
        viewModelScope.launch {
            _uiState.update { it.copy(syncState = SyncState.SYNCING) }
            runCatching { phoneSyncUseCase() }
                .onSuccess {
                    Log.i(TAG, "Sync completed successfully")
                    _uiState.update { it.copy(syncState = SyncState.SUCCESS) }
                }
                .onFailure { e ->
                    Log.e(TAG, "Sync failed", e)
                    _uiState.update { it.copy(syncState = SyncState.ERROR) }
                }
        }
    }

    fun resetSyncState() {
        _uiState.update { it.copy(syncState = SyncState.IDLE) }
    }

    fun prepareBackupExport() {
        viewModelScope.launch {
            runCatching { backupRepository.exportAsJson() }
                .onSuccess { json -> _uiState.update { it.copy(pendingBackupJson = json) } }
                .onFailure { _uiState.update { it.copy(backupMessage = "Export fehlgeschlagen") } }
        }
    }

    fun clearPendingBackupExport() {
        _uiState.update { it.copy(pendingBackupJson = null) }
    }

    fun importBackupFromJson(json: String) {
        viewModelScope.launch {
            runCatching { backupRepository.importFromJson(json) }
                .onSuccess { result ->
                    val msg = buildString {
                        append("${result.remindersImported} Erinnerungen")
                        append(", ${result.locationsImported} Orte")
                        append(", ${result.vacationPeriodsImported} Urlaubszeiträume")
                        append(", ${result.portionSizesImported} Trinkmengen")
                        append(" wiederhergestellt")
                    }
                    _uiState.update { it.copy(backupMessage = msg) }
                }
                .onFailure {
                    _uiState.update { it.copy(backupMessage = "Import fehlgeschlagen – Datei ungültig?") }
                }
        }
    }

    fun clearBackupMessage() {
        _uiState.update { it.copy(backupMessage = null) }
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}

