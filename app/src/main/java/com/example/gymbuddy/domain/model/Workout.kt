package com.example.gymbuddy.domain.model

data class Workout(
    val id: Long = 0,
    val name: String,
    val date: Long,
    val startedAt: Long? = null,
    val duration: Int = 0,
    val notes: String? = null,
    val routineId: Long? = null,
    val isCompleted: Boolean = false,
    val exercises: List<WorkoutExercise> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class WorkoutExercise(
    val id: Long = 0,
    val workoutId: Long,
    val exercise: Exercise,
    val orderIndex: Int,
    val notes: String? = null,
    val restTimerSeconds: Int = 90,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val sets: List<WorkoutSet> = emptyList()
)

data class WorkoutSet(
    val id: Long = 0,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val reps: Int,
    val weight: Float,
    val rpe: Int? = null,
    val isWarmUp: Boolean = false,
    val isDropSet: Boolean = false,
    val isFailureSet: Boolean = false,
    val isSuperset: Boolean = false,
    val notes: String? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val previousSet: WorkoutSet? = null
)