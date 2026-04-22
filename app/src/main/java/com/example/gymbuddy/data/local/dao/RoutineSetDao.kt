package com.example.gymbuddy.data.local.dao

import androidx.room.*
import com.example.gymbuddy.data.local.entity.RoutineSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineSetDao {
    @Query("SELECT * FROM routine_sets WHERE routineExerciseId = :routineExerciseId ORDER BY setNumber")
    fun getSetsForRoutineExercise(routineExerciseId: Long): Flow<List<RoutineSetEntity>>

    @Query("SELECT * FROM routine_sets WHERE routineExerciseId = :routineExerciseId ORDER BY setNumber")
    suspend fun getSetsForRoutineExerciseSync(routineExerciseId: Long): List<RoutineSetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineSet(routineSet: RoutineSetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineSets(routineSets: List<RoutineSetEntity>)

    @Query("DELETE FROM routine_sets WHERE routineExerciseId = :routineExerciseId")
    suspend fun deleteSetsForRoutineExercise(routineExerciseId: Long)

    @Query("DELETE FROM routine_sets WHERE routineExerciseId IN (SELECT id FROM routine_exercises WHERE routineId = :routineId)")
    suspend fun deleteSetsForRoutine(routineId: Long)
}