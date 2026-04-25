package com.csci3310.gymbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_workouts")
data class ScheduledWorkoutEntity(
    @PrimaryKey
    val scheduleKey: String,
    val weekStartDate: Long,
    val dayOfWeek: Int,
    val routineId: Long?,
    val isRestDay: Boolean
) {
    companion object {
        fun createKey(weekStartDate: Long, dayOfWeek: Int) = "${weekStartDate}_$dayOfWeek"
    }
}
