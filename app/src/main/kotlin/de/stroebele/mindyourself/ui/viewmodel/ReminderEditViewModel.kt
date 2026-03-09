package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.model.HydrationConfig
import de.stroebele.mindyourself.domain.model.LocationFilter
import de.stroebele.mindyourself.domain.model.MovementConfig
import de.stroebele.mindyourself.domain.model.NamedLocation
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.model.ReminderType
import de.stroebele.mindyourself.domain.model.ScreenBreakConfig
import de.stroebele.mindyourself.domain.model.SedentaryConfig
import de.stroebele.mindyourself.domain.model.SupplementConfig
import de.stroebele.mindyourself.domain.repository.NamedLocationRepository
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

data class ReminderEditUiState(
    val config: ReminderConfig? = null,
    val namedLocations: List<NamedLocation> = emptyList(),
    val labelError: String? = null,
    val isSaved: Boolean = false,
)

@HiltViewModel
class ReminderEditViewModel @Inject constructor(
    private val reminderConfigRepository: ReminderConfigRepository,
    private val namedLocationRepository: NamedLocationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val reminderId: Long = savedStateHandle["reminderId"] ?: 0L

    private val _uiState = MutableStateFlow(ReminderEditUiState())
    val uiState: StateFlow<ReminderEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val locations = namedLocationRepository.getAll()
            _uiState.update { it.copy(namedLocations = locations) }
        }
        if (reminderId != 0L) {
            viewModelScope.launch {
                val config = reminderConfigRepository.getById(reminderId)
                _uiState.update { it.copy(config = config) }
            }
        }
    }

    /** Called when creating a new reminder from a template type. */
    fun initTemplate(type: ReminderType) {
        if (_uiState.value.config != null) return
        _uiState.update { it.copy(config = defaultConfigFor(type)) }
    }

    fun update(config: ReminderConfig) {
        _uiState.update { it.copy(config = config, labelError = null) }
    }

    fun setLocationFilterEnabled(enabled: Boolean) {
        val config = _uiState.value.config ?: return
        _uiState.update {
            it.copy(
                config = config.copy(
                    locationFilter = if (enabled) LocationFilter(allowedLocationIds = emptySet()) else null
                )
            )
        }
    }

    fun toggleAllowedLocation(locationId: Long) {
        val config = _uiState.value.config ?: return
        val filter = config.locationFilter ?: return
        val updated = if (locationId in filter.allowedLocationIds)
            filter.allowedLocationIds - locationId
        else
            filter.allowedLocationIds + locationId
        _uiState.update {
            it.copy(config = config.copy(locationFilter = filter.copy(allowedLocationIds = updated)))
        }
    }

    fun setStrictMode(strict: Boolean) {
        val config = _uiState.value.config ?: return
        val filter = config.locationFilter ?: return
        _uiState.update {
            it.copy(config = config.copy(locationFilter = filter.copy(strictMode = strict)))
        }
    }

    fun save() {
        val config = _uiState.value.config ?: return
        if (config.label.isBlank()) {
            _uiState.update { it.copy(labelError = "Name darf nicht leer sein") }
            return
        }
        viewModelScope.launch {
            val allConfigs = reminderConfigRepository.getAll()
            val nameTaken = allConfigs.any {
                it.label.trim().equals(config.label.trim(), ignoreCase = true) && it.id != config.id
            }
            if (nameTaken) {
                _uiState.update { it.copy(labelError = "Dieser Name ist bereits vergeben") }
                return@launch
            }
            reminderConfigRepository.save(config.copy(label = config.label.trim()))
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    private fun defaultConfigFor(type: ReminderType): ReminderConfig = ReminderConfig(
        type = type,
        label = type.defaultLabel(),
        activeDays = DayOfWeek.entries.toSet(),
        activeFrom = LocalTime.of(8, 0),
        activeUntil = LocalTime.of(22, 0),
        typeConfig = when (type) {
            ReminderType.MOVEMENT -> MovementConfig()
            ReminderType.SEDENTARY -> SedentaryConfig()
            ReminderType.HYDRATION -> HydrationConfig()
            ReminderType.SUPPLEMENT -> SupplementConfig(
                supplementName = "Supplement",
                scheduledTimes = listOf(LocalTime.of(8, 0)),
            )
            ReminderType.SCREEN_BREAK -> ScreenBreakConfig()
        },
    )
}

private fun ReminderType.defaultLabel(): String = when (this) {
    ReminderType.MOVEMENT -> "Bewegungserinnerung"
    ReminderType.SEDENTARY -> "Sitz-Pause"
    ReminderType.HYDRATION -> "Hydration"
    ReminderType.SUPPLEMENT -> "Supplement"
    ReminderType.SCREEN_BREAK -> "Bildschirmpause"
}
