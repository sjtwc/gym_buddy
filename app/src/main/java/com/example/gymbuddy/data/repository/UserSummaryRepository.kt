package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.local.dao.UserSummaryDao
import com.example.gymbuddy.data.local.entity.UserSummaryEntity
import com.example.gymbuddy.domain.model.UserSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class UserSummaryRepository @Inject constructor(
    private val userSummaryDao: UserSummaryDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getUserSummary(): Flow<UserSummary?> =
        userSummaryDao.getUserSummary().map { entity ->
            entity?.toDomain()
        }

    suspend fun getUserSummarySync(): UserSummary? =
        userSummaryDao.getUserSummarySync()?.toDomain()

    suspend fun saveUserSummary(summary: UserSummary) {
        userSummaryDao.insertUserSummary(summary.toEntity())
    }

    suspend fun getLastGeneratedAt(): Long? =
        userSummaryDao.getLastGeneratedAt()

    suspend fun shouldRegenerate(): Boolean {
        val lastGenerated = userSummaryDao.getLastGeneratedAt() ?: return true
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return lastGenerated < oneDayAgo
    }

    private fun UserSummaryEntity.toDomain(): UserSummary {
        return UserSummary(
            generatedAt = generatedAt,
            profile = json.decodeFromString(profileJson),
            stats = json.decodeFromString(statsJson),
            weeklyProgress = json.decodeFromString(weeklyProgressJson),
            fourWeekComparison = json.decodeFromString(fourWeekComparisonJson),
            derivedMetrics = json.decodeFromString(derivedMetricsJson),
            recentPrs = json.decodeFromString(recentPrsJson),
            topImprovements = json.decodeFromString(topImprovementsJson),
            insights = json.decodeFromString(insightsJson)
        )
    }

    private fun UserSummary.toEntity(): UserSummaryEntity {
        return UserSummaryEntity(
            id = 1,
            generatedAt = generatedAt,
            profileJson = json.encodeToString(profile),
            statsJson = json.encodeToString(stats),
            weeklyProgressJson = json.encodeToString(weeklyProgress),
            fourWeekComparisonJson = json.encodeToString(fourWeekComparison),
            derivedMetricsJson = json.encodeToString(derivedMetrics),
            recentPrsJson = json.encodeToString(recentPrs),
            topImprovementsJson = json.encodeToString(topImprovements),
            insightsJson = json.encodeToString(insights)
        )
    }
}