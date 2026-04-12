package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.local.dao.SetDao
import com.example.gymbuddy.data.local.dao.WorkoutDao
import com.example.gymbuddy.data.local.dao.WorkoutExerciseDao
import com.example.gymbuddy.data.local.entity.SetEntity
import com.example.gymbuddy.data.local.entity.WorkoutEntity
import com.example.gymbuddy.data.local.entity.WorkoutExerciseEntity
import com.example.gymbuddy.domain.model.Workout
import com.example.gymbuddy.domain.model.WorkoutExercise
import com.example.gymbuddy.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val setDao: SetDao
) {
    fun getAllWorkouts(): Flow<List<Workout>> =
        workoutDao.getAllWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun getWorkoutById(id: Long): Workout? =
        workoutDao.getWorkoutById(id)?.toDomain()
    
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
    
    suspend fun updateWorkout(workout: Workout) =
        workoutDao.updateWorkout(workout.toEntity())
    
    suspend fun completeWorkout(workoutId: Long, duration: Int): Float {
        val volume = setDao.getWorkoutVolume(workoutId)
        workoutDao.getWorkoutById(workoutId)?.let { workout ->
            workoutDao.updateWorkout(workout.copy(isCompleted = true, duration = duration))
        }
        return volume
    }
    
    suspend fun deleteWorkout(workout: Workout) =
        workoutDao.deleteWorkout(workout.toEntity())
    
    private fun WorkoutEntity.toDomain() = Workout(
        id = id,
        name = name,
        date = date,
        duration = duration,
        notes = notes,
        routineId = routineId,
        isCompleted = isCompleted,
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
        createdAt = createdAt
    )
}

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