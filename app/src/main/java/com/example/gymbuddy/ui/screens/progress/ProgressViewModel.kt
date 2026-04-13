package com.example.gymbuddy.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.local.MuscleGoalPreferences
import com.example.gymbuddy.data.local.dao.PersonalRecordDao
import com.example.gymbuddy.data.repository.DailyVolume
import com.example.gymbuddy.data.repository.ExerciseRepository
import com.example.gymbuddy.data.repository.WorkoutRepository
import com.example.gymbuddy.data.repository.WorkoutWithDetails
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
    val selectedWorkoutDetails: WorkoutWithDetails? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val muscleGoalPreferences: MuscleGoalPreferences,
    private val personalRecordDao: PersonalRecordDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()
    
    private val exerciseCache = mutableMapOf<Long, String>()
    
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
        workoutRepository.getRecentCompletedWorkouts(14)
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
    
    private fun loadPersonalRecords() {
        personalRecordDao.getAllRecords(20)
            .onEach { records ->
                val exerciseIds = records.map { it.exerciseId }.distinct()
                exerciseCache.clear()
                exerciseIds.forEach { id ->
                    exerciseRepository.getExerciseById(id)?.let { exercise ->
                        exerciseCache[id] = exercise.name
                    }
                }
                
                val prList = records.map { record ->
                    PersonalRecordItem(
                        exerciseId = record.exerciseId,
                        exerciseName = exerciseCache[record.exerciseId] ?: "Unknown",
                        weight = record.weight,
                        reps = record.reps,
                        date = record.date,
                        type = if (record.type == "REP_MAX") "${record.reps}RM" else record.type
                    )
                }
                _uiState.update { it.copy(personalRecords = prList) }
            }
            .launchIn(viewModelScope)
    }
    
    fun selectWorkout(workoutId: Long) {
        viewModelScope.launch {
            val details = workoutRepository.getWorkoutWithDetails(workoutId)
            _uiState.update { it.copy(selectedWorkoutDetails = details) }
        }
    }
    
    fun clearSelectedWorkout() {
        _uiState.update { it.copy(selectedWorkoutDetails = null) }
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