package com.csci3310.gymbuddy.domain.model

data class Exercise(
    val id: Long = 0,
    val name: String,
    val description: String,
    val targetMuscle: String,
    val secondaryMuscles: List<String> = emptyList(),
    val equipmentType: String,
    val videoUrl: String? = null,
    val instructions: List<String> = emptyList(),
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MuscleGroup(val displayName: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    ARMS("Arms"),
    LEGS("Legs"),
    CORE("Core")
}

enum class EquipmentType(val displayName: String) {
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    CABLE("Cable"),
    MACHINE("Machine"),
    BODYWEIGHT("Bodyweight"),
    KETTLEBELL("Kettlebell"),
    OTHER("Other")
}