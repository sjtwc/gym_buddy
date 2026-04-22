package com.example.gymbuddy.domain.model

data class ScheduledWorkout(
    val dayOfWeek: Int,
    val routineId: Long?,
    val isRestDay: Boolean
)

data class WeeklySchedule(
    val weekStartDate: Long,
    val scheduledWorkouts: List<ScheduledWorkout>
)
