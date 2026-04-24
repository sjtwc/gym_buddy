package com.example.gymbuddy.data.repository

import com.example.gymbuddy.domain.model.Trend
import com.example.gymbuddy.domain.model.UserSummary
import com.example.gymbuddy.domain.model.Improvement
import com.example.gymbuddy.service.LlmService
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmRepository @Inject constructor(
    private val llmService: LlmService,
    private val userSummaryRepository: UserSummaryRepository
) {
    val generationFlow: SharedFlow<String> = llmService.generationFlow
    val isModelLoaded: SharedFlow<Boolean> = llmService.isModelLoaded
    val isLoading: StateFlow<Boolean> = llmService.isLoading

    suspend fun ensureModelReady(): Boolean = llmService.ensureModelReady()

    suspend fun generateResponse(
        userMessage: String,
        chatHistory: List<ChatMessage>,
        petName: String = "GymBot"
    ) {
        val userSummary = userSummaryRepository.getUserSummarySync()
        val context = buildContext(userSummary)
        val prompt = buildPrompt(userMessage, chatHistory, context, petName)

        llmService.generate(
            prompt = prompt,
            maxTokens = 512,
            temperature = 0.7f
        )
    }

    suspend fun generateInitialGreeting(petName: String = "GymBot") {
        val userSummary = userSummaryRepository.getUserSummarySync()
        val context = buildContext(userSummary)

        val prompt = buildGreetingPrompt(petName, context)
        llmService.generate(
            prompt = prompt,
            maxTokens = 256,
            temperature = 0.8f
        )
    }

    private fun buildContext(summary: UserSummary?): String {
        if (summary == null) {
            return "No workout data available yet. User is just getting started!"
        }

        return buildString {
            appendLine("=== USER PROFILE ===")
            appendLine("Name: ${summary.profile.name}")
            appendLine("Level: ${summary.profile.level} (${summary.profile.title})")
            appendLine("XP: ${summary.profile.xp}/${summary.profile.xpForNextLevel}")
            appendLine()
            appendLine("=== STATS ===")
            appendLine("Total Workouts: ${summary.stats.totalWorkouts}")
            appendLine("Current Streak: ${summary.stats.currentStreak} days")
            appendLine("Longest Streak: ${summary.stats.longestStreak} days")
            appendLine("Total Volume: ${summary.stats.totalVolume.toInt()}kg")
            appendLine()
            appendLine("=== THIS WEEK ===")
            appendLine("Workouts: ${summary.weeklyProgress.workoutCount}/4 (${summary.weeklyProgress.workoutCount * 100 / 4}%)")
            appendLine("Volume: ${summary.weeklyProgress.totalVolume.toInt()}kg (${formatChange(summary.weeklyProgress.volumeChangePercent)})")
            appendLine("Duration: ${summary.weeklyProgress.avgDurationMinutes} min avg")
            appendLine("Muscles Worked: ${summary.weeklyProgress.muscleGroupsWorked.joinToString(", ")}")
            appendLine()
            appendLine("=== 4-WEEK TRENDS ===")
            appendLine("Workouts: ${trendArrow(summary.fourWeekComparison.workoutCountTrend)}")
            appendLine("Volume: ${trendArrow(summary.fourWeekComparison.volumeTrend)}")
            appendLine()
            appendLine("=== BALANCE ===")
            appendLine("Push/Pull Ratio: ${String.format("%.1f", summary.derivedMetrics.pushPullRatio)} (ideal: 1.0-1.5)")
            appendLine("Consistency: ${summary.derivedMetrics.consistencyScore}%")
            if (summary.topImprovements.isNotEmpty()) {
                appendLine()
                appendLine("=== TOP IMPROVEMENTS ===")
                summary.topImprovements.forEach { improvement ->
                    appendLine("- ${improvement.metric}: ${improvement.changeValue}")
                }
            }
            if (summary.insights.positives.isNotEmpty()) {
                appendLine()
                appendLine("Positives: ${summary.insights.positives.joinToString(" ")}")
            }
            if (summary.insights.areasForImprovement.isNotEmpty()) {
                appendLine("Areas to Improve: ${summary.insights.areasForImprovement.joinToString(" ")}")
            }
        }
    }

    private fun formatChange(percent: Float): String {
        return when {
            percent > 0 -> "+${percent.toInt()}%"
            percent < 0 -> "${percent.toInt()}%"
            else -> "0%"
        }
    }

    private fun trendArrow(trend: Trend): String {
        return when (trend) {
            Trend.UP -> "Up ↑"
            Trend.DOWN -> "Down ↓"
            Trend.STABLE -> "Stable →"
        }
    }

    private fun buildPrompt(
        userMessage: String,
        chatHistory: List<ChatMessage>,
        context: String,
        petName: String
    ): String {
        val historyText = if (chatHistory.isEmpty()) {
            ""
        } else {
            buildString {
                appendLine("Previous conversation:")
                chatHistory.takeLast(6).forEach { msg ->
                    if (msg.isUser) {
                        appendLine("User: ${msg.content}")
                    } else {
                        appendLine("$petName: ${msg.content}")
                    }
                }
                appendLine()
            }
        }

        return """You are $petName, a motivational AI fitness coach. Be cheerful, supportive, and give practical advice. Keep responses concise and conversational.

User's fitness profile:
$context

$historyText
User: $userMessage

$petName:"""
    }

    private fun buildGreetingPrompt(petName: String, context: String): String {
        return """You are $petName, a motivational AI fitness coach. Be cheerful, supportive, and give a brief personalized greeting. Keep it to 1-2 short sentences. Use an emoji or two.

User's fitness profile:
$context

$petName:"""
    }

    fun unloadModel() = llmService.unload()
}

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)