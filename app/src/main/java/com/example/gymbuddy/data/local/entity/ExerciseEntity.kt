package com.csci3310.gymbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val targetMuscle: String,
    val secondaryMuscles: String,
    val equipmentType: String,
    val videoUrl: String? = null,
    val instructions: String,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)