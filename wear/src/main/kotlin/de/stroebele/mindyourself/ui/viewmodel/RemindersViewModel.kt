package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.model.NamedLocation
import de.stroebele.mindyourself.domain.model.ReminderConfig
import de.stroebele.mindyourself.domain.repository.NamedLocationRepository
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class RemindersDisplayItem(
    val config: ReminderConfig,
    val locationNames: List<String>,
)

@HiltViewModel
class RemindersViewModel @Inject constructor(
    reminderConfigRepository: ReminderConfigRepository,
    namedLocationRepository: NamedLocationRepository,
) : ViewModel() {

    val items: StateFlow<List<RemindersDisplayItem>> = combine(
        reminderConfigRepository.observeAll(),
        namedLocationRepository.observeAll(),
    ) { configs, locations ->
        configs.map { config ->
            val locationNames = config.locationFilter?.allowedLocationIds
                ?.mapNotNull { id -> locations.find { it.id == id }?.name }
                ?.sorted()
                ?: emptyList()
            RemindersDisplayItem(config, locationNames)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
}
