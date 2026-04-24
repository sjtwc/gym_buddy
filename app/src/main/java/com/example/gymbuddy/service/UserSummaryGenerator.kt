package com.example.gymbuddy.service

import com.example.gymbuddy.data.repository.UserProfileRepository
import com.example.gymbuddy.domain.model.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class UserSummaryGenerator @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    suspend fun generateSummary(): UserSummary {
        val profile = userProfileRepository.getUserProfileSync()
        val xpForNextLevel = calculateXpForLevel((profile?.level ?: 1) + 1)

        val profileSummary = ProfileSummary(
            name = profile?.name ?: "Trainer",
            level = profile?.level ?: 1,
            title = profile?.title ?: "Novice",
            xp = profile?.xp ?: 0,
            xpForNextLevel = xpForNextLevel
        )

        val statsSummary = StatsSummary(
            totalWorkouts = profile?.totalWorkouts ?: 0,
            currentStreak = profile?.currentStreak ?: 0,
            longestStreak = profile?.longestStreak ?: 0,
            totalVolume = profile?.totalVolume ?: 0f,
            avgVolumePerWorkout = if ((profile?.totalWorkouts ?: 0) > 0)
                (profile?.totalVolume ?: 0f) / (profile?.totalWorkouts ?: 1)
            else 0f
        )

        return UserSummary(
            profile = profileSummary,
            stats = statsSummary,
            weeklyProgress = WeeklyProgress(
                weekStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                workoutCount = 0,
                targetWorkouts = 4,
                totalVolume = 0f,
                avgDurationMinutes = 0,
                muscleGroupsWorked = emptyList(),
                volumeChangePercent = 0f,
                durationChangeMinutes = 0
            ),
            fourWeekComparison = FourWeekComparison(
                workoutCountThisWeek = 0,
                workoutCountAvg = 0f,
                workoutCountTrend = Trend.STABLE,
                volumeThisWeek = 0f,
                volumeAvg = 0f,
                volumeTrend = Trend.STABLE,
                durationThisWeekAvg = 0,
                durationAvg = 0,
                durationTrend = Trend.STABLE
            ),
            derivedMetrics = DerivedMetrics(
                volumePerMuscle = emptyMap(),
                pushPullRatio = 1f,
                intensityScore = 0,
                consistencyScore = 0,
                recoveryScore = 100
            ),
            recentPrs = emptyList(),
            topImprovements = emptyList(),
            insights = Insights(
                positives = listOf("Start your first workout to see personalized insights!"),
                areasForImprovement = emptyList()
            )
        )
    }

    private fun calculateXpForLevel(level: Int): Int {
        return (100 * 1.5.pow((level - 1).toDouble())).toInt()
    }
}