package com.csci3310.gymbuddy.ui.screens.routines

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csci3310.gymbuddy.data.repository.RoutineRepository
import com.csci3310.gymbuddy.domain.model.Routine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutinesUiState(
    val routines: List<Routine> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineRepository: RoutineRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RoutinesUiState())
    val uiState: StateFlow<RoutinesUiState> = _uiState.asStateFlow()
    
    init {
        loadRoutines()
    }
    
    private fun loadRoutines() {
        routineRepository.getAllRoutines()
            .onEach { routines ->
                _uiState.update { it.copy(routines = routines, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }
    
    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routine)
        }
    }
}