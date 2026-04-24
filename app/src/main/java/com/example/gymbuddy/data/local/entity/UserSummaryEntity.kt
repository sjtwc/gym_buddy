package com.example.gymbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_summary")
data class UserSummaryEntity(
    @PrimaryKey
    val id: Long = 1,
    val generatedAt: Long = System.currentTimeMillis(),
    val profileJson: String,
    val statsJson: String,
    val weeklyProgressJson: String,
    val fourWeekComparisonJson: String,
    val derivedMetricsJson: String,
    val recentPrsJson: String,
    val topImprovementsJson: String,
    val insightsJson: String
)