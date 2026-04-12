package com.example.gymbuddy.domain.model

import java.text.SimpleDateFormat
import java.util.*

data class WorkoutSession(
    val workoutId: Long = 0,
    val workoutName: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val exercises: List<WorkoutExerciseSession> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val isResting: Boolean = false,
    val restTimeRemaining: Int = 0,
    val restTimerType: SetType? = null,
    val isCompleted: Boolean = false,
    val locationName: String = ""
) {
    val currentExercise: WorkoutExerciseSession?
        get() = exercises.getOrNull(currentExerciseIndex)

    val totalSets: Int
        get() = exercises.sumOf { it.sets.size }

    val elapsedSeconds: Long
        get() = (System.currentTimeMillis() - startedAt) / 1000

    val elapsedTimeFormatted: String
        get() = formatTime(elapsedSeconds.toInt())

    val workoutDateFormatted: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(startedAt))
        }

    val workoutStartTimeFormatted: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(startedAt))
        }

    val progressText: String
        get() = "${currentExerciseIndex + 1}/${exercises.size} exercises"

    companion object {
        fun generateDefaultName(): String {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val period = when {
                hour < 12 -> "Morning"
                hour < 17 -> "Afternoon"
                else -> "Evening"
            }
            return "$period Workout"
        }
    }
}

data class WorkoutExerciseSession(
    val exercise: Exercise,
    val sets: List<WorkoutSetData> = listOf(WorkoutSetData(setNumber = 1)),
    val restTimers: List<RestTimer> = listOf(
        RestTimer(SetType.NORMAL, 0, false),
        RestTimer(SetType.WARMUP, 0, false),
        RestTimer(SetType.WORK, 0, false),
        RestTimer(SetType.DROP, 0, false),
        RestTimer(SetType.FAILURE, 0, false)
    )
) {
    val previousData: Pair<Double, Int>?
        get() = null
}

data class WorkoutSetData(
    val setNumber: Int = 1,
    val setType: SetType = SetType.NORMAL,
    val weight: Double? = null,
    val reps: Int? = null,
    val isCompleted: Boolean = false
)

enum class SetType(val abbreviation: String, val displayName: String) {
    NORMAL("1", "Normal"),
    WORK("W", "Work Set"),
    WARMUP("W", "Warmup Set"),
    DROP("D", "Drop Set"),
    FAILURE("F", "Failure Set")
}

data class RestTimer(
    val type: SetType,
    val durationSeconds: Int,
    val isActive: Boolean = false
)

enum class SessionState {
    IDLE,
    ACTIVE,
    RESTING,
    COMPLETED
}

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}