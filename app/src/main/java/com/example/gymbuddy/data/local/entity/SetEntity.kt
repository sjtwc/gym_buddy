package com.example.gymbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutExerciseId")]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true)
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
    val completedAt: Long? = null,
    val previousSetId: Long? = null
)