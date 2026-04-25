package com.csci3310.gymbuddy.domain.model

data class FreeTimeSlot(
    val date: String,
    val dayName: String,
    val startHour: Int,
    val endHour: Int,
    val durationHours: Int
)