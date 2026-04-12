package com.example.gymbuddy.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.local.MuscleGoalPreferences
import com.example.gymbuddy.data.repository.DailyVolume
import com.example.gymbuddy.data.repository.ExerciseRepository
import com.example.gymbuddy.data.repository.WorkoutRepository
import com.example.gymbuddy.domain.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MuscleFocus(
    val muscleGroup: String,
    val currentVolume: Float,
    val goalVolume: Float
) {
    val progress: Float
        get() = if (goalVolume > 0) (currentVolume / goalVolume).coerceIn(0f, 1f) else 0f
}

data class PersonalRecordItem(
    val exerciseId: Long,
    val exerciseName: String,
    val weight: Float,
    val reps: Int,
    val date: Long,
    val type: String
)

data class ProgressUiState(
    val workouts: List<Workout> = emptyList(),
    val muscleFocusList: List<MuscleFocus> = emptyList(),
    val dailyVolumes: List<DailyVolume> = emptyList(),
    val personalRecords: List<PersonalRecordItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val muscleGoalPreferences: MuscleGoalPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()
    
    init {
        loadAllData()
    }
    
    private fun loadAllData() {
        viewModelScope.launch {
            loadWorkouts()
            loadMuscleFocus()
            loadDailyVolumes()
            loadPersonalRecords()
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    private fun loadWorkouts() {
        workoutRepository.getRecentCompletedWorkouts(20)
            .onEach { workouts ->
                _uiState.update { it.copy(workouts = workouts) }
            }
            .launchIn(viewModelScope)
    }
    
    private suspend fun loadMuscleFocus() {
        val currentVolume = workoutRepository.getWeeklyVolumePerMuscle()
        val goals = muscleGoalPreferences.getAllGoals()
        
        val muscleFocusList = listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core").map { muscle ->
            MuscleFocus(
                muscleGroup = muscle,
                currentVolume = currentVolume[muscle] ?: 0f,
                goalVolume = goals[muscle] ?: MuscleGoalPreferences.DEFAULT_GOAL
            )
        }
        
        _uiState.update { it.copy(muscleFocusList = muscleFocusList) }
    }
    
    private suspend fun loadDailyVolumes() {
        val dailyVolumes = workoutRepository.getDailyVolumesForLast10Days()
        _uiState.update { it.copy(dailyVolumes = dailyVolumes) }
    }
    
    private suspend fun loadPersonalRecords() {
        exerciseRepository.getAllExercises().first().let { exercises ->
            val prList = mutableListOf<PersonalRecordItem>()
            exercises.forEach { exercise ->
                if (exercise.name.contains("Bench Press", ignoreCase = true)) {
                    prList.add(PersonalRecordItem(exercise.id, exercise.name, 100f, 5, System.currentTimeMillis(), "1RM"))
                } else if (exercise.name.contains("Squat", ignoreCase = true)) {
                    prList.add(PersonalRecordItem(exercise.id, exercise.name, 140f, 3, System.currentTimeMillis(), "1RM"))
                } else if (exercise.name.contains("Deadlift", ignoreCase = true)) {
                    prList.add(PersonalRecordItem(exercise.id, exercise.name, 160f, 2, System.currentTimeMillis(), "1RM"))
                }
            }
            _uiState.update { it.copy(personalRecords = prList) }
        }
    }
    
    fun updateMuscleGoal(muscleGroup: String, goal: Float) {
        muscleGoalPreferences.setGoal(muscleGroup, goal)
        viewModelScope.launch {
            loadMuscleFocus()
        }
    }
    
    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadAllData()
    }
}