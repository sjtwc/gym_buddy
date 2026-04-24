package com.example.gymbuddy.data.local.dao

import androidx.room.*
import com.example.gymbuddy.data.local.entity.UserSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSummaryDao {
    @Query("SELECT * FROM user_summary WHERE id = 1")
    fun getUserSummary(): Flow<UserSummaryEntity?>

    @Query("SELECT * FROM user_summary WHERE id = 1")
    suspend fun getUserSummarySync(): UserSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSummary(userSummary: UserSummaryEntity)

    @Query("DELETE FROM user_summary")
    suspend fun deleteAll()

    @Query("SELECT generatedAt FROM user_summary WHERE id = 1")
    suspend fun getLastGeneratedAt(): Long?
}