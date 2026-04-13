package com.example.gymbuddy.domain.model

import com.example.gymbuddy.domain.model.SetType

data class Routine(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val type: String,
    val difficulty: String,
    val estimatedMinutes: Int = 60,
    val isCustom: Boolean = true,
    val isFavorite: Boolean = false,
    val exercises: List<RoutineExercise> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class RoutineExercise(
    val id: Long = 0,
    val routineId: Long,
    val exercise: Exercise,
    val orderIndex: Int,
    val setType: SetType = SetType.NORMAL,
    val targetSets: Int = 3,
    val targetReps: String = "8-12",
    val restSeconds: Int = 90,
    val notes: String? = null,
    val timers: List<RoutineExerciseTimer> = emptyList()
)

data class RoutineExerciseTimer(
    val type: SetType,
    val durationSeconds: Int = 60
)

enum class RoutineType(val displayName: String) {
    PUSH("Push"),
    PULL("Pull"),
    LEGS("Legs"),
    UPPER("Upper"),
    LOWER("Lower"),
    FULL_BODY("Full Body"),
    CUSTOM("Custom")
}

enum class Difficulty(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}