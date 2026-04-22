package com.example.gymbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_workouts")
data class ScheduledWorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekStartDate: Long,
    val dayOfWeek: Int,
    val routineId: Long?,
    val isRestDay: Boolean
)
