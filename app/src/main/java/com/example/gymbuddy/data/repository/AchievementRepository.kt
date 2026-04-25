package com.csci3310.gymbuddy.data.repository

import com.csci3310.gymbuddy.data.local.dao.AchievementDao
import com.csci3310.gymbuddy.data.local.dao.UserProfileDao
import com.csci3310.gymbuddy.data.local.dao.WorkoutDao
import com.csci3310.gymbuddy.data.local.dao.WorkoutExerciseDao
import com.csci3310.gymbuddy.data.local.entity.AchievementEntity
import com.csci3310.gymbuddy.domain.model.AchievementType
import com.csci3310.gymbuddy.domain.model.XpConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
        return achievementDao.getAllAchievements().map { entities ->
            val earnedTypes = entities.associate { it.achievementType to it.claimCount }
            
            AchievementType.entries.map { type ->
                val claimCount = earnedTypes[type.name] ?: 0
                val isUnlocked = claimCount > 0
                
                // Calculate progress based on actual user stats
                val progress = if (isUnlocked) {
                    100
                } else {
                    calculateProgressForType(type)
                }
                
                AchievementWithProgress(
                    type = type,
                    claimCount = claimCount,
                    isUnlocked = isUnlocked,
                    progress = progress
                )
            }
        }
    }
    
    private suspend fun calculateProgressForType(type: AchievementType): Int {
        val profile = userProfileDao.getUserProfileSync()
        val totalWorkouts = profile?.totalWorkouts ?: 0
        val currentStreak = profile?.currentStreak ?: 0
        val currentLevel = profile?.level ?: 1
        val totalVolume = profile?.totalVolume ?: 0f
        
        val workoutsThisWeek = getWorkoutsThisWeek()
        val uniqueExercises = getUniqueExercisesCount()
        
        return when (type) {
            AchievementType.FIRST_WORKOUT -> if (totalWorkouts >= 1) 100 else (totalWorkouts * 100).coerceIn(0, 100)
            AchievementType.STREAK_7_DAYS -> (currentStreak * 100 / 7).coerceIn(0, 100)
            AchievementType.HEAVY_LIFTER -> if (type.targetValue > 0) ((totalVolume / type.targetValue) * 100).toInt().coerceIn(0, 100) else 0
            AchievementType.EARLY_BIRD -> if (totalWorkouts >= 1) 100 else 0
            AchievementType.NIGHT_OWL -> if (totalWorkouts >= 1) 100 else 0
            AchievementType.MARATHONER -> if (type.targetValue > 0) (totalWorkouts * 100 / type.targetValue).coerceIn(0, 100) else 0
            AchievementType.STRENGTH_MASTER -> if (type.targetValue > 0) (currentLevel * 100 / type.targetValue).coerceIn(0, 100) else 0
            AchievementType.SOCIAL_BUTTERFLY -> 0
            AchievementType.PERFECTIONIST -> 0
            AchievementType.VARIETY -> if (type.targetValue > 0) (uniqueExercises * 100 / type.targetValue).coerceIn(0, 100) else 0
            AchievementType.BEAST_MODE -> if (type.targetValue > 0) (totalWorkouts * 100 / type.targetValue).coerceIn(0, 100) else 0
            AchievementType.CHAMPIONSHIP -> 0
            AchievementType.WEEK_2_WORKOUTS -> if (type.targetValue > 0) (workoutsThisWeek * 100 / type.targetValue).coerceIn(0, 100) else 0
            AchievementType.WEEK_3_WORKOUTS -> if (type.targetValue > 0) (workoutsThisWeek * 100 / type.targetValue).coerceIn(0, 100) else 0
            AchievementType.WEEK_4_WORKOUTS -> if (type.targetValue > 0) (workoutsThisWeek * 100 / type.targetValue).coerceIn(0, 100) else 0
            AchievementType.WEEK_5_WORKOUTS -> if (type.targetValue > 0) (workoutsThisWeek * 100 / type.targetValue).coerceIn(0, 100) else 0
            AchievementType.WEEK_6_WORKOUTS -> if (type.targetValue > 0) (workoutsThisWeek * 100 / type.targetValue).coerceIn(0, 100) else 0
        }
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
    
    suspend fun resetAllAchievements() {
        achievementDao.deleteAllAchievements()
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