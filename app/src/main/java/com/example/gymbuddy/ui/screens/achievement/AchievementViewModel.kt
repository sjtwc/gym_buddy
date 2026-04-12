package com.example.gymbuddy.ui.screens.achievement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.local.dao.AchievementDao
import com.example.gymbuddy.data.local.dao.WorkoutDao
import com.example.gymbuddy.data.local.dao.WorkoutExerciseDao
import com.example.gymbuddy.data.local.dao.UserProfileDao
import com.example.gymbuddy.domain.model.AchievementType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementUiState(
    val achievements: List<AchievementDisplayItem> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

data class AchievementDisplayItem(
    val type: AchievementType,
    val claimCount: Int,
    val isUnlocked: Boolean,
    val progress: Int
)

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val achievementDao: AchievementDao,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val userProfileDao: UserProfileDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()
    
    init {
        loadAchievements()
    }
    
    private fun loadAchievements() {
        viewModelScope.launch {
            achievementDao.getAllAchievements().collect { earnedAchievements ->
                val profile = userProfileDao.getUserProfileSync()
                val totalWorkouts = profile?.totalWorkouts ?: 0
                val currentStreak = profile?.currentStreak ?: 0
                val currentLevel = profile?.level ?: 1
                val totalVolume = profile?.totalVolume ?: 0f
                
                val workoutsThisWeek = getWorkoutsThisWeek()
                val uniqueExercises = getUniqueExercisesCount()
                
                val earnedTypes = earnedAchievements.associate { it.achievementType to it.claimCount }
                
                val displayItems = AchievementType.entries.map { type ->
                    val claimCount = earnedTypes[type.name] ?: 0
                    val isUnlocked = claimCount > 0
                    val progress = calculateProgress(type, totalWorkouts, currentStreak, currentLevel, totalVolume, workoutsThisWeek, uniqueExercises)
                    
                    AchievementDisplayItem(
                        type = type,
                        claimCount = claimCount,
                        isUnlocked = isUnlocked,
                        progress = progress
                    )
                }
                
                val unlockedCount = displayItems.count { it.isUnlocked }
                val totalCount = AchievementType.entries.size
                
                _uiState.value = AchievementUiState(
                    achievements = displayItems,
                    unlockedCount = unlockedCount,
                    totalCount = totalCount,
                    isLoading = false
                )
            }
        }
    }
    
    private fun calculateProgress(
        type: AchievementType,
        totalWorkouts: Int,
        currentStreak: Int,
        currentLevel: Int,
        totalVolume: Float,
        workoutsThisWeek: Int,
        uniqueExercises: Int
    ): Int {
        val currentValue = when (type) {
            AchievementType.FIRST_WORKOUT -> if (totalWorkouts >= 1) 100 else (totalWorkouts * 100)
            AchievementType.STREAK_7_DAYS -> (currentStreak * 100 / 7).coerceIn(0, 100)
            AchievementType.HEAVY_LIFTER -> ((totalVolume / type.targetValue) * 100).toInt().coerceIn(0, 100)
            AchievementType.EARLY_BIRD -> if (totalWorkouts >= 1) 100 else 0
            AchievementType.NIGHT_OWL -> if (totalWorkouts >= 1) 100 else 0
            AchievementType.MARATHONER -> (totalWorkouts * 100 / type.targetValue).coerceIn(0, 100)
            AchievementType.STRENGTH_MASTER -> (currentLevel * 100 / type.targetValue).coerceIn(0, 100)
            AchievementType.SOCIAL_BUTTERFLY -> 0
            AchievementType.PERFECTIONIST -> 100
            AchievementType.VARIETY -> (uniqueExercises * 100 / type.targetValue).coerceIn(0, 100)
            AchievementType.BEAST_MODE -> (totalWorkouts * 100 / type.targetValue).coerceIn(0, 100)
            AchievementType.CHAMPIONSHIP -> 0
            AchievementType.WEEK_2_WORKOUTS -> (workoutsThisWeek * 100 / type.targetValue).coerceIn(0, 100)
            AchievementType.WEEK_3_WORKOUTS -> (workoutsThisWeek * 100 / type.targetValue).coerceIn(0, 100)
            AchievementType.WEEK_4_WORKOUTS -> (workoutsThisWeek * 100 / type.targetValue).coerceIn(0, 100)
            AchievementType.WEEK_5_WORKOUTS -> (workoutsThisWeek * 100 / type.targetValue).coerceIn(0, 100)
            AchievementType.WEEK_6_WORKOUTS -> (workoutsThisWeek * 100 / type.targetValue).coerceIn(0, 100)
        }
        return currentValue.coerceIn(0, 100)
    }
    
    private suspend fun getWorkoutsThisWeek(): Int {
        val now = System.currentTimeMillis()
        val weekStart = now - (7 * 24 * 60 * 60 * 1000L)
        return workoutDao.getWorkoutsByDateRangeSync(weekStart, now).size
    }
    
    private suspend fun getUniqueExercisesCount(): Int {
        val workouts = workoutDao.getRecentCompletedWorkouts(100).first()
        val exerciseIds = mutableSetOf<Long>()
        for (workout in workouts) {
            val exercises = workoutExerciseDao.getExercisesForWorkout(workout.id).first()
            exerciseIds.addAll(exercises.map { it.exerciseId })
        }
        return exerciseIds.size
    }
}