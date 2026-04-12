package com.example.gymbuddy.data.local.dao

import androidx.room.*
import com.example.gymbuddy.data.local.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY name")
    fun getAllRoutines(): Flow<List<RoutineEntity>>
    
    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): RoutineEntity?
    
    @Query("SELECT * FROM routines WHERE type = :type ORDER BY name")
    fun getRoutinesByType(type: String): Flow<List<RoutineEntity>>
    
    @Query("SELECT * FROM routines WHERE isFavorite = 1")
    fun getFavoriteRoutines(): Flow<List<RoutineEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long
    
    @Update
    suspend fun updateRoutine(routine: RoutineEntity)
    
    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)
}