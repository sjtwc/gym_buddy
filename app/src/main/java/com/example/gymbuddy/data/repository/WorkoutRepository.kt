package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.local.dao.DailyVolumeResult
import com.example.gymbuddy.data.local.dao.SetDao
import com.example.gymbuddy.data.local.dao.WorkoutDao
import com.example.gymbuddy.data.local.dao.WorkoutExerciseDao
import com.example.gymbuddy.data.local.entity.SetEntity
import com.example.gymbuddy.data.local.entity.WorkoutEntity
import com.example.gymbuddy.data.local.entity.WorkoutExerciseEntity
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.RoutineExercise
import com.example.gymbuddy.domain.model.SetType
import com.example.gymbuddy.domain.model.Workout
import com.example.gymbuddy.domain.model.WorkoutExercise
import com.example.gymbuddy.domain.model.WorkoutSession
import com.example.gymbuddy.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val setDao: SetDao,
    private val exerciseRepository: ExerciseRepository
) {
    private fun SetEntity.toDomainSet() = WorkoutSet(
        id = id,
        workoutExerciseId = workoutExerciseId,
        setNumber = setNumber,
        reps = reps,
        weight = weight,
        rpe = rpe,
        isWarmUp = isWarmUp,
        isDropSet = isDropSet,
        isFailureSet = isFailureSet,
        isSuperset = isSuperset,
        notes = notes,
        completedAt = completedAt
    )
    
    fun getAllWorkouts(): Flow<List<Workout>> =
        workoutDao.getAllWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun getWorkoutById(id: Long): Workout? =
        workoutDao.getWorkoutById(id)?.toDomain()
    
    suspend fun getWorkoutWithDetails(id: Long): WorkoutWithDetails? {
        val workout = workoutDao.getWorkoutById(id)?.toDomain() ?: return null
        val weEntities = workoutExerciseDao.getExercisesForWorkoutOnce(id)
        
        val exercises = weEntities.mapNotNull { we ->
            val sets = setDao.getSetsForWorkoutExerciseOnce(we.id)
            val exercise = exerciseRepository.getExerciseById(we.exerciseId)
            exercise?.let {
                WorkoutExercise(
                    id = we.id,
                    workoutId = workout.id,
                    exercise = it,
                    orderIndex = we.orderIndex,
                    notes = we.notes,
                    restTimerSeconds = we.restTimerSeconds,
                    targetReps = we.targetReps,
                    sets = sets.map { s -> s.toDomainSet() }
                )
            }
        }
        
        return WorkoutWithDetails(workout, exercises)
    }
    
    fun getWorkoutsByDateRange(startDate: Long, endDate: Long): Flow<List<Workout>> =
        workoutDao.getWorkoutsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun getActiveWorkout(): Workout? =
        workoutDao.getActiveWorkout()?.toDomain()
    
    fun getRecentCompletedWorkouts(limit: Int): Flow<List<Workout>> =
        workoutDao.getRecentCompletedWorkouts(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun startWorkout(workout: Workout): Long =
        workoutDao.insertWorkout(workout.toEntity())

    suspend fun startWorkoutFromRoutine(workout: Workout, routineExercises: List<RoutineExercise>): Long {
        val workoutId = workoutDao.insertWorkout(workout.toEntity())
        routineExercises.forEachIndexed { index, routineExercise ->
            val weEntity = WorkoutExerciseEntity(
                workoutId = workoutId,
                exerciseId = routineExercise.exercise.id,
                orderIndex = index,
                notes = routineExercise.notes,
                restTimerSeconds = routineExercise.restSeconds,
                targetReps = routineExercise.targetReps
            )
            val weId = workoutExerciseDao.insertWorkoutExercise(weEntity)
            val setEntities = routineExercise.sets.map { set ->
                SetEntity(
                    workoutExerciseId = weId,
                    setNumber = set.setNumber,
                    reps = set.reps.toIntOrNull() ?: 8,
                    weight = set.weight?.toFloat() ?: 0f,
                    completedAt = null,
                    isWarmUp = set.setType == SetType.WARMUP,
                    isDropSet = set.setType == SetType.DROP,
                    isFailureSet = set.setType == SetType.FAILURE,
                    isSuperset = false
                )
            }
            setDao.insertSets(setEntities)
        }
        return workoutId
    }
    
    suspend fun updateWorkout(workout: Workout) =
        workoutDao.updateWorkout(workout.toEntity())
    
    suspend fun completeWorkout(workoutId: Long, duration: Int): Float {
        val volume = setDao.getWorkoutVolume(workoutId)
        workoutDao.getWorkoutById(workoutId)?.let { workout ->
            workoutDao.updateWorkout(workout.copy(isCompleted = true, duration = duration))
        }
        return volume
    }
    
    suspend fun saveCompletedWorkout(session: WorkoutSession, duration: Int, feeling: Int? = null): Float {
        val workout = getWorkoutById(session.workoutId) ?: return 0f
        updateWorkout(workout.copy(
            name = session.workoutName.ifEmpty { workout.name },
            duration = duration,
            isCompleted = true,
            feeling = feeling
        ))
        workoutExerciseDao.deleteAllForWorkout(session.workoutId)
        session.exercises.forEachIndexed { index, exSession ->
            val weEntity = WorkoutExerciseEntity(
                workoutId = session.workoutId,
                exerciseId = exSession.exercise.id,
                orderIndex = index
            )
            val weId = workoutExerciseDao.insertWorkoutExercise(weEntity)
            val setEntities = exSession.sets.map { set ->
                SetEntity(
                    workoutExerciseId = weId,
                    setNumber = set.setNumber,
                    reps = set.reps ?: 8,
                    weight = set.weight?.toFloat() ?: 0f,
                    completedAt = if (set.isCompleted) System.currentTimeMillis() else null,
                    isWarmUp = set.setType == SetType.WARMUP,
                    isDropSet = set.setType == SetType.DROP,
                    isFailureSet = set.setType == SetType.FAILURE,
                    isSuperset = false
                )
            }
            setDao.insertSets(setEntities)
        }
        return setDao.getWorkoutVolume(session.workoutId)
    }
    
    suspend fun deleteWorkout(workout: Workout) =
        workoutDao.deleteWorkout(workout.toEntity())
    
    suspend fun getWeeklyVolumePerMuscle(): Map<String, Float> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis
        
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val endOfWeek = calendar.timeInMillis - 1
        
        val muscleGroups = listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core")
        return muscleGroups.associateWith { muscle ->
            setDao.getVolumeForMuscleGroup(startOfWeek, endOfWeek, muscle)
        }
    }
    
    suspend fun getDailyVolumesForLast10Days(): List<DailyVolume> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_YEAR, -9)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startDate = calendar.timeInMillis
        
        val results = setDao.getDailyVolumes(startDate, endDate)
        
        val volumeByDay = results.associate { result ->
            val cal = Calendar.getInstance().apply { timeInMillis = result.workoutDate }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis to result.dailyVolume
        }
        
        val dailyVolumes = mutableListOf<DailyVolume>()
        val currentCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        for (i in 9 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = currentCal.timeInMillis
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dayStart = dayCal.timeInMillis
            val volume = volumeByDay[dayStart] ?: 0f
            dailyVolumes.add(DailyVolume(dayStart, volume))
        }
        
        return dailyVolumes
    }
    
    private fun WorkoutEntity.toDomain() = Workout(
        id = id,
        name = name,
        date = date,
        duration = duration,
        notes = notes,
        routineId = routineId,
        isCompleted = isCompleted,
        feeling = feeling,
        createdAt = createdAt
    )
    
    private fun Workout.toEntity() = WorkoutEntity(
        id = id,
        name = name,
        date = date,
        duration = duration,
        notes = notes,
        routineId = routineId,
        isCompleted = isCompleted,
        feeling = feeling,
        createdAt = createdAt
    )
}

data class WorkoutWithDetails(
    val workout: Workout,
    val exercises: List<WorkoutExercise>
)

data class DailyVolume(
    val date: Long,
    val volume: Float
)

class SetRepository @Inject constructor(
    private val setDao: SetDao
) {
    fun getSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<WorkoutSet>> =
        setDao.getSetsForWorkoutExercise(workoutExerciseId).map { entities ->
            entities.map { it.toDomain() }
        }
    
    fun getPreviousSetsForExercise(exerciseId: Long, limit: Int): Flow<List<WorkoutSet>> =
        setDao.getPreviousSetsForExercise(exerciseId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun insertSet(set: WorkoutSet): Long =
        setDao.insertSet(set.toEntity())
    
    suspend fun updateSet(set: WorkoutSet) =
        setDao.updateSet(set.toEntity())
    
    suspend fun deleteSet(set: WorkoutSet) =
        setDao.deleteSet(set.toEntity())
    
    private fun SetEntity.toDomain() = WorkoutSet(
        id = id,
        workoutExerciseId = workoutExerciseId,
        setNumber = setNumber,
        reps = reps,
        weight = weight,
        rpe = rpe,
        isWarmUp = isWarmUp,
        isDropSet = isDropSet,
        isFailureSet = isFailureSet,
        isSuperset = isSuperset,
        notes = notes,
        completedAt = completedAt
    )
    
    private fun WorkoutSet.toEntity() = SetEntity(
        id = id,
        workoutExerciseId = workoutExerciseId,
        setNumber = setNumber,
        reps = reps,
        weight = weight,
        rpe = rpe,
        isWarmUp = isWarmUp,
        isDropSet = isDropSet,
        isFailureSet = isFailureSet,
        isSuperset = isSuperset,
        notes = notes,
        completedAt = completedAt
    )
}

class WorkoutExerciseRepository @Inject constructor(
    private val workoutExerciseDao: WorkoutExerciseDao
) {
    fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExerciseEntity>> =
        workoutExerciseDao.getExercisesForWorkout(workoutId)
    
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExerciseEntity): Long =
        workoutExerciseDao.insertWorkoutExercise(workoutExercise)
    
    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExerciseEntity) =
        workoutExerciseDao.updateWorkoutExercise(workoutExercise)
    
    suspend fun deleteWorkoutExercise(workoutExercise: WorkoutExerciseEntity) =
        workoutExerciseDao.deleteWorkoutExercise(workoutExercise)
}