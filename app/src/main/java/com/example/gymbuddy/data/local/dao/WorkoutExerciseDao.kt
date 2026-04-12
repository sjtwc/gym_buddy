package com.example.gymbuddy.data.local.dao

import androidx.room.*
import com.example.gymbuddy.data.local.entity.WorkoutExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutExerciseDao {
    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY orderIndex")
    fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExerciseEntity>>
    
    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    suspend fun getWorkoutExerciseById(id: Long): WorkoutExerciseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExerciseEntity): Long
    
    @Update
    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExerciseEntity)
    
    @Delete
    suspend fun deleteWorkoutExercise(workoutExercise: WorkoutExerciseEntity)
    
    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteAllForWorkout(workoutId: Long)
}