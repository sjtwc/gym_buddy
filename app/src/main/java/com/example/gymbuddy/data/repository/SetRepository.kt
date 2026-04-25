package com.csci3310.gymbuddy.data.repository

import com.csci3310.gymbuddy.data.local.dao.SetDao
import com.csci3310.gymbuddy.data.local.entity.SetEntity
import com.csci3310.gymbuddy.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

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