package com.example.gymbuddy.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MuscleGoalPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "muscle_goals"
        private const val KEY_CHEST_GOAL = "goal_chest"
        private const val KEY_BACK_GOAL = "goal_back"
        private const val KEY_SHOULDERS_GOAL = "goal_shoulders"
        private const val KEY_ARMS_GOAL = "goal_arms"
        private const val KEY_LEGS_GOAL = "goal_legs"
        private const val KEY_CORE_GOAL = "goal_core"

        private val GOAL_KEYS = mapOf(
            "Chest" to KEY_CHEST_GOAL,
            "Back" to KEY_BACK_GOAL,
            "Shoulders" to KEY_SHOULDERS_GOAL,
            "Arms" to KEY_ARMS_GOAL,
            "Legs" to KEY_LEGS_GOAL,
            "Core" to KEY_CORE_GOAL
        )

        const val DEFAULT_GOAL = 10000f
    }

    fun getGoal(muscleGroup: String): Float {
        val key = GOAL_KEYS[muscleGroup] ?: return DEFAULT_GOAL
        return prefs.getFloat(key, DEFAULT_GOAL)
    }

    fun setGoal(muscleGroup: String, goal: Float) {
        val key = GOAL_KEYS[muscleGroup] ?: return
        prefs.edit().putFloat(key, goal).apply()
    }

    fun getAllGoals(): Map<String, Float> {
        return GOAL_KEYS.mapValues { (muscle, _) -> getGoal(muscle) }
    }

    fun resetGoals() {
        prefs.edit().clear().apply()
    }
}