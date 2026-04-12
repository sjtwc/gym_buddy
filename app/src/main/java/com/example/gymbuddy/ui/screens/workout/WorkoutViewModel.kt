package com.example.gymbuddy.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.RoutineRepository
import com.example.gymbuddy.data.repository.WorkoutRepository
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.Workout
import com.example.gymbuddy.service.WorkoutSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val routines: List<Routine> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
    private val sessionManager: WorkoutSessionManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()
    
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
    
    fun startQuickWorkout(sessionManager: WorkoutSessionManager) {
        viewModelScope.launch {
            val workout = Workout(
                name = "Quick Workout",
                date = System.currentTimeMillis(),
                startedAt = System.currentTimeMillis(),
                isCompleted = false
            )
            val workoutId = workoutRepository.startWorkout(workout)
            sessionManager.startSession(workoutId, "Quick Workout")
        }
    }
    
    fun startWorkoutWithRoutine(routine: Routine, sessionManager: WorkoutSessionManager) {
        viewModelScope.launch {
            val workout = Workout(
                name = routine.name,
                date = System.currentTimeMillis(),
                startedAt = System.currentTimeMillis(),
                routineId = routine.id,
                isCompleted = false
            )
            val workoutId = workoutRepository.startWorkout(workout)
            sessionManager.startSession(workoutId, routine.name)
        }
    }
}