package com.example.gymbuddy.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.RoutineRepository
import com.example.gymbuddy.data.repository.ScheduledWorkoutRepository
import com.example.gymbuddy.data.repository.WorkoutRepository
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.ScheduledWorkout
import com.example.gymbuddy.domain.model.Workout
import com.example.gymbuddy.service.WorkoutSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val routines: List<Routine> = emptyList(),
    val isLoading: Boolean = true,
    val scheduledWorkouts: List<ScheduledWorkout> = emptyList(),
    val suggestedSchedule: List<ScheduledWorkout> = emptyList(),
    val expandedSections: Set<String> = emptySet()
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
    private val scheduledWorkoutRepository: ScheduledWorkoutRepository,
    private val sessionManager: WorkoutSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        loadRoutines()
        loadScheduledWorkouts()
    }

    private fun loadRoutines() {
        routineRepository.getAllRoutines()
            .onEach { routines ->
                _uiState.update { it.copy(routines = routines, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadScheduledWorkouts() {
        scheduledWorkoutRepository.getWeeklySchedule()
            .onEach { scheduled ->
                _uiState.update { it.copy(scheduledWorkouts = scheduled) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleSection(section: String) {
        _uiState.update { state ->
            val newExpanded = if (section in state.expandedSections) {
                state.expandedSections - section
            } else {
                state.expandedSections + section
            }
            state.copy(expandedSections = newExpanded)
        }
    }

    fun generateSuggestedSchedule() {
        viewModelScope.launch {
            val suggested = scheduledWorkoutRepository.getSuggestedSchedule()
            _uiState.update { it.copy(suggestedSchedule = suggested) }
        }
    }

    fun applySuggestedSchedule() {
        viewModelScope.launch {
            scheduledWorkoutRepository.clearWeekSchedule()
            _uiState.value.suggestedSchedule.forEach { scheduled ->
                scheduledWorkoutRepository.saveScheduledWorkout(
                    dayOfWeek = scheduled.dayOfWeek,
                    routineId = scheduled.routineId,
                    isRestDay = scheduled.isRestDay
                )
            }
            _uiState.update { it.copy(suggestedSchedule = emptyList()) }
        }
    }

    fun saveScheduledWorkout(dayOfWeek: Int, routineId: Long?, isRestDay: Boolean) {
        viewModelScope.launch {
            scheduledWorkoutRepository.saveScheduledWorkout(dayOfWeek, routineId, isRestDay)
        }
    }

    fun clearWeekSchedule() {
        viewModelScope.launch {
            scheduledWorkoutRepository.clearWeekSchedule()
        }
    }

    suspend fun getRoutineById(id: Long): Routine? {
        return scheduledWorkoutRepository.getRoutineById(id)
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
        if (sessionManager.isActive.value) {
            sessionManager.expand()
            return
        }
        viewModelScope.launch {
            val fullRoutine = routineRepository.getRoutineByIdWithExercises(routine.id)
            val workout = Workout(
                name = routine.name,
                date = System.currentTimeMillis(),
                startedAt = System.currentTimeMillis(),
                routineId = routine.id,
                isCompleted = false
            )
            val workoutId = if (fullRoutine != null && fullRoutine.exercises.isNotEmpty()) {
                workoutRepository.startWorkoutFromRoutine(workout, fullRoutine.exercises)
            } else {
                workoutRepository.startWorkout(workout)
            }
            sessionManager.startSession(workoutId, routine.name)
            sessionManager.loadExercisesFromWorkout(workoutId)
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routine)
        }
    }
}