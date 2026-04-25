package com.csci3310.gymbuddy.data.local.dao

import androidx.room.*
import com.csci3310.gymbuddy.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>
    
    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileSync(): UserProfileEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity)
    
    @Update
    suspend fun updateUserProfile(userProfile: UserProfileEntity)
    
    @Query("UPDATE user_profile SET currentStreak = :streak, longestStreak = CASE WHEN :streak > longestStreak THEN :streak ELSE longestStreak END WHERE id = 1")
    suspend fun updateStreak(streak: Int)
    
    @Query("UPDATE user_profile SET xp = :newXp, level = :newLevel WHERE id = 1")
    suspend fun updateXpAndLevel(newXp: Int, newLevel: Int)
    
    @Query("UPDATE user_profile SET petHappiness = :happiness, petMood = :mood WHERE id = 1")
    suspend fun updatePetStatus(happiness: Int, mood: String)
}