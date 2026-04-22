package com.example.gymbuddy.data.local.dao

import androidx.room.*
import com.example.gymbuddy.data.local.entity.ScheduledWorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledWorkoutDao {
    @Query("SELECT * FROM scheduled_workouts WHERE weekStartDate = :weekStartDate ORDER BY dayOfWeek")
    fun getScheduledWorkoutsForWeek(weekStartDate: Long): Flow<List<ScheduledWorkoutEntity>>

    @Query("SELECT * FROM scheduled_workouts WHERE weekStartDate = :weekStartDate AND dayOfWeek = :dayOfWeek")
    suspend fun getScheduledWorkout(weekStartDate: Long, dayOfWeek: Int): ScheduledWorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledWorkout(scheduledWorkout: ScheduledWorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledWorkouts(scheduledWorkouts: List<ScheduledWorkoutEntity>)

    @Delete
    suspend fun deleteScheduledWorkout(scheduledWorkout: ScheduledWorkoutEntity)

    @Query("DELETE FROM scheduled_workouts WHERE weekStartDate = :weekStartDate")
    suspend fun clearWeekSchedule(weekStartDate: Long)

    @Query("DELETE FROM scheduled_workouts WHERE weekStartDate = :weekStartDate AND dayOfWeek = :dayOfWeek")
    suspend fun clearDaySchedule(weekStartDate: Long, dayOfWeek: Int)
}
