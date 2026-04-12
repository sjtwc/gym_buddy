package com.example.gymbuddy.domain.model

data class WorkoutSession(
    val workoutId: Long,
    val workoutName: String,
    val startedAt: Long,
    val currentExercise: Exercise? = null,
    val currentExerciseIndex: Int = 0,
    val totalExercises: Int = 0,
    val currentSetIndex: Int = 0,
    val totalSets: Int = 0,
    val isResting: Boolean = false,
    val restTimeRemaining: Int = 0,
    val isCompleted: Boolean = false
) {
    val elapsedSeconds: Long
        get() = (System.currentTimeMillis() - startedAt) / 1000
    
    val elapsedTimeFormatted: String
        get() {
            val totalSeconds = elapsedSeconds.toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    
    val progressText: String
        get() = if (currentExercise != null) {
            "Set ${currentSetIndex + 1}/$totalSets"
        } else {
            "Starting..."
        }
    
    val restTimeFormatted: String
        get() {
            val minutes = restTimeRemaining / 60
            val seconds = restTimeRemaining % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}

enum class SessionState {
    IDLE,
    ACTIVE,
    RESTING,
    COMPLETED
}