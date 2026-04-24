package com.example.gymbuddy.domain.model

data class UserSummary(
    val generatedAt: Long = System.currentTimeMillis(),
    val profile: ProfileSummary,
    val stats: StatsSummary,
    val weeklyProgress: WeeklyProgress,
    val fourWeekComparison: FourWeekComparison,
    val derivedMetrics: DerivedMetrics,
    val recentPrs: List<PersonalRecordSummary>,
    val topImprovements: List<Improvement>,
    val insights: Insights
)

data class ProfileSummary(
    val name: String,
    val level: Int,
    val title: String,
    val xp: Int,
    val xpForNextLevel: Int
)

data class StatsSummary(
    val totalWorkouts: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalVolume: Float,
    val avgVolumePerWorkout: Float
)

data class WeeklyProgress(
    val weekStartDate: String,
    val workoutCount: Int,
    val targetWorkouts: Int,
    val totalVolume: Float,
    val avgDurationMinutes: Int,
    val muscleGroupsWorked: List<String>,
    val volumeChangePercent: Float,
    val durationChangeMinutes: Int
)

data class FourWeekComparison(
    val workoutCountThisWeek: Int,
    val workoutCountAvg: Float,
    val workoutCountTrend: Trend,
    val volumeThisWeek: Float,
    val volumeAvg: Float,
    val volumeTrend: Trend,
    val durationThisWeekAvg: Int,
    val durationAvg: Int,
    val durationTrend: Trend
)

enum class Trend {
    UP, DOWN, STABLE
}

data class DerivedMetrics(
    val volumePerMuscle: Map<String, Float>,
    val pushPullRatio: Float,
    val intensityScore: Int,
    val consistencyScore: Int,
    val recoveryScore: Int
)

data class PersonalRecordSummary(
    val exerciseName: String,
    val previousBest: Float,
    val newBest: Float,
    val improvement: Float,
    val date: String
)

data class Improvement(
    val metric: String,
    val direction: Trend,
    val changeValue: String
)

data class Insights(
    val positives: List<String>,
    val areasForImprovement: List<String>
)