package com.csci3310.gymbuddy.data.local.dao

import androidx.room.*
import com.csci3310.gymbuddy.data.local.entity.RoutineTimerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineTimerDao {
    @Query("SELECT * FROM routine_timers WHERE routineExerciseId = :routineExerciseId ORDER BY setType")
    fun getTimersForRoutineExercise(routineExerciseId: Long): Flow<List<RoutineTimerEntity>>

    @Query("SELECT * FROM routine_timers WHERE routineExerciseId = :routineExerciseId ORDER BY setType")
    suspend fun getTimersForRoutineExerciseSync(routineExerciseId: Long): List<RoutineTimerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineTimer(routineTimer: RoutineTimerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineTimers(routineTimers: List<RoutineTimerEntity>)

    @Query("DELETE FROM routine_timers WHERE routineExerciseId = :routineExerciseId")
    suspend fun deleteTimersForRoutineExercise(routineExerciseId: Long)

    @Query("DELETE FROM routine_timers WHERE routineExerciseId IN (SELECT id FROM routine_exercises WHERE routineId = :routineId)")
    suspend fun deleteTimersForRoutine(routineId: Long)
}