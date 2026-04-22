package com.example.gymbuddy.data.local.dao

import androidx.room.*
import com.example.gymbuddy.data.local.entity.RoutineExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineExerciseDao {
    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex")
    fun getExercisesForRoutine(routineId: Long): Flow<List<RoutineExerciseEntity>>
    
    @Query("SELECT * FROM routine_exercises WHERE id = :id")
    suspend fun getRoutineExerciseById(id: Long): RoutineExerciseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercise(routineExercise: RoutineExerciseEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExerciseEntity>)
    
    @Update
    suspend fun updateRoutineExercise(routineExercise: RoutineExerciseEntity)
    
    @Delete
    suspend fun deleteRoutineExercise(routineExercise: RoutineExerciseEntity)
    
    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteAllForRoutine(routineId: Long)

    @Query("SELECT id FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex")
    suspend fun getExerciseIdsForRoutine(routineId: Long): List<Long>
}