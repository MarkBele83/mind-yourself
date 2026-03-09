package de.stroebele.mindyourself.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.stroebele.mindyourself.domain.repository.SupplementRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplementViewModel @Inject constructor(
    private val supplementRepository: SupplementRepository,
) : ViewModel() {

    fun log(supplementName: String) {
        viewModelScope.launch {
            supplementRepository.log(supplementName)
        }
    }
}
