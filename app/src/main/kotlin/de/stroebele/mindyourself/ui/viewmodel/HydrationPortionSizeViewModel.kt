package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.model.HydrationPortionSize
import de.stroebele.mindyourself.domain.repository.HydrationPortionSizeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HydrationPortionSizeViewModel @Inject constructor(
    private val repository: HydrationPortionSizeRepository,
) : ViewModel() {

    val sizes: StateFlow<List<HydrationPortionSize>> = repository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun save(amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch {
            repository.save(HydrationPortionSize(amountMl = amountMl))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
