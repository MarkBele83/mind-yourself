package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HydrationViewModel @Inject constructor(
    private val hydrationRepository: HydrationRepository,
) : ViewModel() {

    val todayTotalMl: StateFlow<Int> = hydrationRepository
        .observeToday()
        .map { logs -> logs.sumOf { it.amountMl } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun log(amountMl: Int) {
        viewModelScope.launch {
            hydrationRepository.log(amountMl)
        }
    }

    fun undoLast() {
        viewModelScope.launch {
            hydrationRepository.removeLatest()
        }
    }
}
