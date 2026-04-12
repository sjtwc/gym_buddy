package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.local.dao.AchievementDao
import com.example.gymbuddy.data.local.dao.UserProfileDao
import com.example.gymbuddy.data.local.dao.WorkoutDao
import com.example.gymbuddy.data.local.dao.WorkoutExerciseDao
import com.example.gymbuddy.data.local.entity.AchievementEntity
import com.example.gymbuddy.domain.model.AchievementType
import com.example.gymbuddy.domain.model.XpConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class AchievementWithProgress(
    val type: AchievementType,
    val claimCount: Int,
    val isUnlocked: Boolean,
    val progress: Int
)

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val userProfileDao: UserProfileDao
) {
    fun getAllAchievementsWithProgress(): Flow<List<AchievementWithProgress>> {
        return achievementDao.getAllAchievements()
    }
    
    suspend fun checkAndGrantAchievements(): List<AchievementType> {
        val earnedAchievements = mutableListOf<AchievementType>()
        
        val profile = userProfileDao.getUserProfileSync()
        val totalWorkouts = profile?.totalWorkouts ?: 0
        val currentStreak = profile?.currentStreak ?: 0
        val currentLevel = profile?.level ?: 1
        val totalVolume = profile?.totalVolume ?: 0f
        
        val workoutsThisWeek = getWorkoutsThisWeek()
        val uniqueExercises = getUniqueExercisesCount()
        
        for (type in AchievementType.entries) {
            val shouldGrant = when (type) {
                AchievementType.FIRST_WORKOUT -> totalWorkouts >= type.targetValue
                AchievementType.STREAK_7_DAYS -> currentStreak >= type.targetValue
                AchievementType.HEAVY_LIFTER -> totalVolume >= type.targetValue
                AchievementType.EARLY_BIRD -> isEarlyBirdWorkout()
                AchievementType.NIGHT_OWL -> isNightOwlWorkout()
                AchievementType.MARATHONER -> totalWorkouts >= type.targetValue
                AchievementType.STRENGTH_MASTER -> currentLevel >= type.targetValue
                AchievementType.SOCIAL_BUTTERFLY -> false
                AchievementType.PERFECTIONIST -> true
                AchievementType.VARIETY -> uniqueExercises >= type.targetValue
                AchievementType.BEAST_MODE -> totalWorkouts >= type.targetValue
                AchievementType.CHAMPIONSHIP -> false
                AchievementType.WEEK_2_WORKOUTS -> workoutsThisWeek >= type.targetValue
                AchievementType.WEEK_3_WORKOUTS -> workoutsThisWeek >= type.targetValue
                AchievementType.WEEK_4_WORKOUTS -> workoutsThisWeek >= type.targetValue
                AchievementType.WEEK_5_WORKOUTS -> workoutsThisWeek >= type.targetValue
                AchievementType.WEEK_6_WORKOUTS -> workoutsThisWeek >= type.targetValue
            }
            
            if (shouldGrant) {
                val existingAchievement = achievementDao.getAchievementByType(type.name)
                if (existingAchievement == null) {
                    achievementDao.insertAchievement(
                        AchievementEntity(
                            achievementType = type.name,
                            claimCount = 1
                        )
                    )
                    addXpToProfile(type.xpReward)
                    earnedAchievements.add(type)
                } else {
                    achievementDao.incrementClaimCount(type.name)
                    addXpToProfile(type.xpReward)
                    earnedAchievements.add(type)
                }
            }
        }
        
        return earnedAchievements
    }
    
    private suspend fun addXpToProfile(xpAmount: Int) {
        val profile = userProfileDao.getUserProfileSync() ?: return
        val newTotalXp = profile.xp + xpAmount
        val newLevel = XpConfig.calculateLevel(newTotalXp)
        val newXpInLevel = XpConfig.xpInCurrentLevel(newTotalXp)
        
        userProfileDao.updateXpAndLevel(newXpInLevel, newLevel)
    }
    
    private suspend fun getWorkoutsThisWeek(): Int {
        val now = System.currentTimeMillis()
        val weekStart = now - (7 * 24 * 60 * 60 * 1000L)
        return workoutDao.getWorkoutsByDateRange(weekStart, now).first().size
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
    
    private suspend fun isEarlyBirdWorkout(): Boolean {
        val workouts = workoutDao.getRecentCompletedWorkouts(1).first()
        if (workouts.isEmpty()) return false
        val lastWorkout = workouts.first()
        val hour = java.util.Calendar.getInstance().apply {
            timeInMillis = lastWorkout.date
        }.get(java.util.Calendar.HOUR_OF_DAY)
        return hour < 7
    }
    
    private suspend fun isNightOwlWorkout(): Boolean {
        val workouts = workoutDao.getRecentCompletedWorkouts(1).first()
        if (workouts.isEmpty()) return false
        val lastWorkout = workouts.first()
        val hour = java.util.Calendar.getInstance().apply {
            timeInMillis = lastWorkout.date
        }.get(java.util.Calendar.HOUR_OF_DAY)
        return hour >= 21
    }
}