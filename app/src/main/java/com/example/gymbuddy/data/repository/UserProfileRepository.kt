package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.local.dao.RoutineDao
import com.example.gymbuddy.data.local.dao.RoutineExerciseDao
import com.example.gymbuddy.data.local.dao.UserProfileDao
import com.example.gymbuddy.data.local.entity.RoutineEntity
import com.example.gymbuddy.data.local.entity.RoutineExerciseEntity
import com.example.gymbuddy.data.local.entity.UserProfileEntity
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.UserProfile
import com.example.gymbuddy.domain.model.VirtualPet
import com.example.gymbuddy.domain.model.PetMood
import com.example.gymbuddy.domain.model.XpConfig
import com.example.gymbuddy.domain.model.AchievementType
import kotlinx.coroutines.flow.Flow
import kotlin.math.pow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val routineExerciseDao: RoutineExerciseDao
) {
    fun getAllRoutines(): Flow<List<Routine>> =
        routineDao.getAllRoutines().map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun getRoutineById(id: Long): Routine? =
        routineDao.getRoutineById(id)?.toDomain()
    
    fun getRoutinesByType(type: String): Flow<List<Routine>> =
        routineDao.getRoutinesByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    
    fun getFavoriteRoutines(): Flow<List<Routine>> =
        routineDao.getFavoriteRoutines().map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun insertRoutine(routine: Routine): Long =
        routineDao.insertRoutine(routine.toEntity())
    
    suspend fun updateRoutine(routine: Routine) =
        routineDao.updateRoutine(routine.toEntity())
    
    suspend fun deleteRoutine(routine: Routine) =
        routineDao.deleteRoutine(routine.toEntity())
    
    suspend fun toggleFavorite(routineId: Long) {
        routineDao.getRoutineById(routineId)?.let { routine ->
            routineDao.updateRoutine(routine.copy(isFavorite = !routine.isFavorite))
        }
    }
    
    private fun RoutineEntity.toDomain() = Routine(
        id = id,
        name = name,
        description = description,
        type = type,
        difficulty = difficulty,
        estimatedMinutes = estimatedMinutes,
        isCustom = isCustom,
        isFavorite = isFavorite,
        createdAt = createdAt
    )
    
    private fun Routine.toEntity() = RoutineEntity(
        id = id,
        name = name,
        description = description,
        type = type,
        difficulty = difficulty,
        estimatedMinutes = estimatedMinutes,
        isCustom = isCustom,
        isFavorite = isFavorite,
        createdAt = createdAt
    )
}

class UserProfileRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val achievementRepository: AchievementRepository
) {
    fun getUserProfile(): Flow<UserProfile?> =
        userProfileDao.getUserProfile().map { entity ->
            entity?.toDomain()
        }
    
    suspend fun getUserProfileSync(): UserProfile? =
        userProfileDao.getUserProfileSync()?.toDomain()
    
    suspend fun updateUserProfile(userProfile: UserProfile) =
        userProfileDao.updateUserProfile(userProfile.toEntity())
    
    suspend fun addXp(xpAmount: Int) {
        val profile = userProfileDao.getUserProfileSync() ?: return
        val newXp = profile.xp + xpAmount
        val level = calculateLevel(newXp)
        userProfileDao.updateXpAndLevel(newXp, level)
    }
    
    private fun calculateLevel(xp: Int): Int {
        var level = 1
        var xpRequired = 100
        var totalXp = 0
        while (totalXp + xpRequired <= xp) {
            totalXp += xpRequired
            level++
            xpRequired = (100 * 1.5.pow((level - 1).toDouble())).toInt()
        }
        return level
    }
    
    suspend fun updateStreak(streak: Int) {
        userProfileDao.updateStreak(streak)
    }
    
    suspend fun updatePetStatus(happiness: Int, mood: PetMood) {
        userProfileDao.updatePetStatus(happiness, mood.name.lowercase())
    }
    
    suspend fun recordWorkout(totalVolume: Float = 0f): List<AchievementType> {
        val profile = userProfileDao.getUserProfileSync() ?: return emptyList()
        val newStreak = calculateStreak(profile.lastWorkoutDate, profile.currentStreak)
        val totalWorkouts = profile.totalWorkouts + 1
        
        userProfileDao.updateUserProfile(
            profile.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(newStreak, profile.longestStreak),
                totalWorkouts = totalWorkouts,
                totalVolume = profile.totalVolume + totalVolume,
                lastWorkoutDate = System.currentTimeMillis()
            )
        )
        
        val happinessBoost = minOf(20, 100 - profile.petHappiness)
        userProfileDao.updatePetStatus(profile.petHappiness + happinessBoost, "happy")
        
        val currentTotalXp = profile.xp + XpConfig.XP_PER_WORKOUT
        val newLevel = XpConfig.calculateLevel(currentTotalXp)
        val newXpInLevel = XpConfig.xpInCurrentLevel(currentTotalXp)
        userProfileDao.updateXpAndLevel(newXpInLevel, newLevel)
        
        val earnedAchievements = achievementRepository.checkAndGrantAchievements()
        
        return earnedAchievements
    }
    
    private fun calculateStreak(lastWorkoutDate: Long?, currentStreak: Int): Int {
        if (lastWorkoutDate == null) return 1
        val daysSinceLastWorkout = (System.currentTimeMillis() - lastWorkoutDate) / (1000 * 60 * 60 * 24)
        return if (daysSinceLastWorkout <= 1) {
            currentStreak + 1
        } else {
            1
        }
    }
    
    private fun UserProfileEntity.toDomain() = UserProfile(
        id = id,
        name = name,
        level = level,
        xp = xp,
        title = title,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        totalWorkouts = totalWorkouts,
        totalVolume = totalVolume,
        pet = VirtualPet(
            name = petName,
            happiness = petHappiness,
            mood = PetMood.entries.find { it.name.equals(petMood, true) } ?: PetMood.NEUTRAL
        )
    )
    
    private fun UserProfile.toEntity() = UserProfileEntity(
        id = id,
        name = name,
        title = title,
        level = level,
        xp = xp,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        totalWorkouts = totalWorkouts,
        totalVolume = totalVolume,
        petName = pet.name,
        petHappiness = pet.happiness,
        petMood = pet.mood.name.lowercase()
    )
}