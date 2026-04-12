package com.example.gymbuddy.data.local.dao

import androidx.room.*
import com.example.gymbuddy.data.local.entity.SetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {
    @Query("SELECT * FROM sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber")
    fun getSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<SetEntity>>
    
    @Query("SELECT * FROM sets WHERE id = :id")
    suspend fun getSetById(id: Long): SetEntity?
    
    @Query("""
        SELECT s.* FROM sets s
        INNER JOIN workout_exercises we ON s.workoutExerciseId = we.id
        WHERE we.exerciseId = :exerciseId
        ORDER BY s.completedAt DESC
        LIMIT :limit
    """)
    fun getPreviousSetsForExercise(exerciseId: Long, limit: Int): Flow<List<SetEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<SetEntity>)
    
    @Update
    suspend fun updateSet(set: SetEntity)
    
    @Delete
    suspend fun deleteSet(set: SetEntity)
    
    @Query("DELETE FROM sets WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun deleteAllForWorkoutExercise(workoutExerciseId: Long)
}