package com.example.gymbuddy.data.local.dao

import androidx.room.*
import com.example.gymbuddy.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>
    
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?
    
    @Query("SELECT * FROM workouts WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkoutsByDateRange(startDate: Long, endDate: Long): Flow<List<WorkoutEntity>>
    
    @Query("SELECT * FROM workouts WHERE isCompleted = 0 ORDER BY date DESC LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?
    
    @Query("SELECT * FROM workouts WHERE isCompleted = 1 ORDER BY date DESC LIMIT :limit")
    fun getRecentCompletedWorkouts(limit: Int): Flow<List<WorkoutEntity>>
    
    @Query("SELECT COUNT(*) FROM workouts WHERE isCompleted = 1")
    fun getCompletedWorkoutCount(): Flow<Int>
    
    @Query("SELECT SUM(duration) FROM workouts WHERE isCompleted = 1")
    fun getTotalWorkoutDuration(): Flow<Int?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long
    
    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)
    
    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)
}