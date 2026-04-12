package com.example.gymbuddy.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.WorkoutRepository
import com.example.gymbuddy.domain.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ProgressUiState(
    val workouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()
    
    init {
        loadWorkouts()
    }
    
    private fun loadWorkouts() {
        workoutRepository.getRecentCompletedWorkouts(20)
            .onEach { workouts ->
                _uiState.update { it.copy(workouts = workouts, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }
}