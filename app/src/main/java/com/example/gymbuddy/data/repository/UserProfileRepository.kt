package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.local.dao.ExerciseDao
import com.example.gymbuddy.data.local.dao.RoutineDao
import com.example.gymbuddy.data.local.dao.RoutineExerciseDao
import com.example.gymbuddy.data.local.dao.RoutineSetDao
import com.example.gymbuddy.data.local.dao.RoutineTimerDao
import com.example.gymbuddy.data.local.dao.UserProfileDao
import com.example.gymbuddy.data.local.entity.ExerciseEntity
import com.example.gymbuddy.data.local.entity.RoutineEntity
import com.example.gymbuddy.data.local.entity.RoutineExerciseEntity
import com.example.gymbuddy.data.local.entity.RoutineSetEntity
import com.example.gymbuddy.data.local.entity.RoutineTimerEntity
import com.example.gymbuddy.data.local.entity.UserProfileEntity
import com.example.gymbuddy.data.repository.AchievementRepository
import com.example.gymbuddy.domain.model.AchievementType
import com.example.gymbuddy.domain.model.Exercise
import com.example.gymbuddy.domain.model.PetMood
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.RoutineExercise
import com.example.gymbuddy.domain.model.RoutineExerciseTimer
import com.example.gymbuddy.domain.model.RoutineSetData
import com.example.gymbuddy.domain.model.SetType
import com.example.gymbuddy.domain.model.UserProfile
import com.example.gymbuddy.domain.model.VirtualPet
import com.example.gymbuddy.domain.model.XpConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import javax.inject.Inject

class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val routineExerciseDao: RoutineExerciseDao,
    private val routineSetDao: RoutineSetDao,
    private val routineTimerDao: RoutineTimerDao,
    private val exerciseDao: ExerciseDao
) {
    fun getAllRoutines(): Flow<List<Routine>> =
        routineDao.getAllRoutines().map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun getRoutineById(id: Long): Routine? =
        routineDao.getRoutineById(id)?.toDomain()
    
    suspend fun getRoutineByIdWithExercises(id: Long): Routine? {
        val routineEntity = routineDao.getRoutineById(id) ?: return null
        val exerciseEntities = routineExerciseDao.getExercisesForRoutine(id).first()
        val exercises = exerciseEntities.mapNotNull { entity ->
            val exerciseEntity = exerciseDao.getExerciseById(entity.exerciseId) ?: return@mapNotNull null
            val sets = routineSetDao.getSetsForRoutineExerciseSync(entity.id)
            val timers = routineTimerDao.getTimersForRoutineExerciseSync(entity.id)
            entity.toDomain(exerciseEntity.toDomain(), sets, timers)
        }
        return routineEntity.toDomain(exercises)
    }
    
    fun getRoutinesByType(type: String): Flow<List<Routine>> =
        routineDao.getRoutinesByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    
    fun getFavoriteRoutines(): Flow<List<Routine>> =
        routineDao.getFavoriteRoutines().map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun insertRoutine(routine: Routine): Long {
        val routineId = routineDao.insertRoutine(routine.toEntity())
        saveRoutineExercises(routineId, routine.exercises)
        return routineId
    }
    
    suspend fun updateRoutine(routine: Routine) {
        routineDao.updateRoutine(routine.toEntity())
        routineSetDao.deleteSetsForRoutine(routine.id)
        routineTimerDao.deleteTimersForRoutine(routine.id)
        routineExerciseDao.deleteAllForRoutine(routine.id)
        saveRoutineExercises(routine.id, routine.exercises)
    }
    
    private suspend fun saveRoutineExercises(routineId: Long, exercises: List<RoutineExercise>) {
        for ((index, exercise) in exercises.withIndex()) {
            val routineExerciseId = routineExerciseDao.insertRoutineExercise(
                exercise.toEntity(routineId, index)
            )
            val setEntities = exercise.sets.map { it.toEntity(routineExerciseId) }
            routineSetDao.insertRoutineSets(setEntities)
            val timerEntities = exercise.timers.map { it.toEntity(routineExerciseId) }
            routineTimerDao.insertRoutineTimers(timerEntities)
        }
    }
    
    suspend fun deleteRoutine(routine: Routine) =
        routineDao.deleteRoutine(routine.toEntity())
    
    suspend fun toggleFavorite(routineId: Long) {
        routineDao.getRoutineById(routineId)?.let { routine ->
            routineDao.updateRoutine(routine.copy(isFavorite = !routine.isFavorite))
        }
    }
    
    private fun RoutineEntity.toDomain(exercises: List<RoutineExercise> = emptyList()) = Routine(
        id = id,
        name = name,
        description = description,
        type = type,
        difficulty = difficulty,
        estimatedMinutes = estimatedMinutes,
        isCustom = isCustom,
        isFavorite = isFavorite,
        createdAt = createdAt,
        exercises = exercises
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
    
    private fun RoutineExerciseEntity.toDomain(
        exercise: Exercise,
        sets: List<RoutineSetEntity>,
        timers: List<RoutineTimerEntity>
    ): RoutineExercise {
        val setsData = if (sets.isNotEmpty()) {
            sets.map { it.toDomain() }
        } else {
            List(targetSets) { i ->
                RoutineSetData(
                    setNumber = i + 1,
                    reps = targetReps,
                    weight = null,
                    setType = SetType.NORMAL
                )
            }
        }
        val timersData = if (timers.isNotEmpty()) {
            timers.map { it.toDomain() }
        } else {
            listOf(
                RoutineExerciseTimer(SetType.NORMAL, 90),
                RoutineExerciseTimer(SetType.WARMUP, 60),
                RoutineExerciseTimer(SetType.WORK, 90),
                RoutineExerciseTimer(SetType.DROP, 60),
                RoutineExerciseTimer(SetType.FAILURE, 90)
            )
        }
        return RoutineExercise(
            id = id,
            routineId = routineId,
            exercise = exercise,
            orderIndex = orderIndex,
            targetSets = targetSets,
            targetReps = targetReps,
            restSeconds = restSeconds,
            notes = notes,
            bodyFocus = bodyFocus,
            sets = setsData,
            timers = timersData
        )
    }

    private fun RoutineSetEntity.toDomain() = RoutineSetData(
        setNumber = setNumber,
        reps = reps,
        weight = weight,
        setType = SetType.entries.find { it.name.equals(setType, true) } ?: SetType.NORMAL
    )

    private fun RoutineSetData.toEntity(routineExerciseId: Long) = RoutineSetEntity(
        routineExerciseId = routineExerciseId,
        setNumber = setNumber,
        reps = reps,
        weight = weight,
        setType = setType.name
    )

    private fun RoutineTimerEntity.toDomain() = RoutineExerciseTimer(
        type = SetType.entries.find { it.name.equals(setType, true) } ?: SetType.NORMAL,
        durationSeconds = durationSeconds
    )

    private fun RoutineExerciseTimer.toEntity(routineExerciseId: Long) = RoutineTimerEntity(
        routineExerciseId = routineExerciseId,
        setType = type.name,
        durationSeconds = durationSeconds
    )
    
    private fun RoutineExercise.toEntity(routineId: Long, orderIndex: Int) = RoutineExerciseEntity(
        id = id,
        routineId = routineId,
        exerciseId = exercise.id,
        orderIndex = orderIndex,
        targetSets = targetSets,
        targetReps = targetReps,
        restSeconds = restSeconds,
        notes = notes,
        bodyFocus = bodyFocus
    )
    
    private fun ExerciseEntity.toDomain() = Exercise(
        id = id,
        name = name,
        description = description,
        targetMuscle = targetMuscle,
        secondaryMuscles = secondaryMuscles.split(",").filter { it.isNotBlank() },
        equipmentType = equipmentType,
        videoUrl = videoUrl,
        instructions = instructions.split("\n").filter { it.isNotBlank() },
        isCustom = isCustom,
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
        val calToday = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calLast = Calendar.getInstance().apply {
            timeInMillis = lastWorkoutDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val daysSince = (calToday.timeInMillis - calLast.timeInMillis) / (1000L * 60 * 60 * 24)
        return if (daysSince == 0L) {
            currentStreak
        } else if (daysSince == 1L) {
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
        lastWorkoutDate = lastWorkoutDate,
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