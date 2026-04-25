package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.local.dao.WorkoutExerciseDao
import com.example.gymbuddy.data.local.entity.WorkoutExerciseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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