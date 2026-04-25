package com.csci3310.gymbuddy.domain.usecase

import javax.inject.Inject
import kotlin.math.pow

class CalculateLevelUseCase @Inject constructor() {
    operator fun invoke(totalXp: Int): LevelResult {
        var level = 1
        var xpRequired = 100
        var totalXpUsed = 0

        while (totalXpUsed + xpRequired <= totalXp) {
            totalXpUsed += xpRequired
            level++
            xpRequired = (100 * 1.5.pow((level - 1).toDouble())).toInt()
        }

        return LevelResult(
            level = level,
            currentLevelXp = totalXp - totalXpUsed,
            xpNeededForNextLevel = xpRequired,
            progressPercentage = (totalXp.toFloat() - totalXpUsed) / xpRequired * 100
        )
    }

    data class LevelResult(
        val level: Int,
        val currentLevelXp: Int,
        val xpNeededForNextLevel: Int,
        val progressPercentage: Float
    )
}