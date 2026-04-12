package com.example.gymbuddy.data.local.dao

import androidx.room.*
import com.example.gymbuddy.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY claimedAt DESC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE achievementType = :type LIMIT 1")
    suspend fun getAchievementByType(type: String): AchievementEntity?
    
    @Query("SELECT * FROM achievements WHERE achievementType = :type")
    fun getAchievementsByType(type: String): Flow<List<AchievementEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity): Long
    
    @Query("UPDATE achievements SET claimCount = claimCount + 1, claimedAt = :timestamp WHERE achievementType = :type")
    suspend fun incrementClaimCount(type: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM achievements WHERE achievementType = :type")
    suspend fun getAchievementCount(type: String): Int
    
    @Query("DELETE FROM achievements")
    suspend fun deleteAllAchievements()
}