package com.csci3310.gymbuddy.data.local.dao

import androidx.room.*
import com.csci3310.gymbuddy.data.local.entity.PersonalRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId ORDER BY date DESC")
    fun getRecordsForExercise(exerciseId: Long): Flow<List<PersonalRecordEntity>>
    
    @Query("SELECT * FROM personal_records WHERE type = :type ORDER BY date DESC")
    fun getRecordsByType(type: String): Flow<List<PersonalRecordEntity>>
    
    @Query("SELECT * FROM personal_records ORDER BY date DESC LIMIT :limit")
    fun getAllRecords(limit: Int): Flow<List<PersonalRecordEntity>>
    
    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND type = :type AND reps = :reps ORDER BY date DESC LIMIT 1")
    suspend fun getRecordForRepMax(exerciseId: Long, type: String, reps: Int): PersonalRecordEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PersonalRecordEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<PersonalRecordEntity>)
    
    @Delete
    suspend fun deleteRecord(record: PersonalRecordEntity)
}