package com.example.gymbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long = 1,
    val name: String = "Trainer",
    val title: String = "Novice",
    val level: Int = 1,
    val xp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val totalVolume: Float = 0f,
    val petName: String = "GymBot",
    val petHappiness: Int = 50,
    val petMood: String = "neutral",
    val lastWorkoutDate: Long? = null,
    val gender: String? = null,
    val age: Int? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val createdAt: Long = System.currentTimeMillis()
)