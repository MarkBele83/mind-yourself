package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.model.NamedLocation
import de.stroebele.mindyourself.domain.repository.NamedLocationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationListViewModel @Inject constructor(
    private val namedLocationRepository: NamedLocationRepository,
) : ViewModel() {

    val locations: StateFlow<List<NamedLocation>> = namedLocationRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun delete(id: Long) {
        viewModelScope.launch { namedLocationRepository.delete(id) }
    }
}
