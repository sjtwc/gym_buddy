package com.example.gymbuddy.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.WorkoutRepository
import com.example.gymbuddy.domain.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class WorkoutUiState(
    val workouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()
    
    init {
        loadWorkouts()
    }
    
    private fun loadWorkouts() {
        workoutRepository.getAllWorkouts()
            .onEach { workouts ->
                _uiState.update { it.copy(workouts = workouts, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }
}