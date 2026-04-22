package com.example.gymbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_sets",
    foreignKeys = [
        ForeignKey(
            entity = RoutineExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineExerciseId")]
)
data class RoutineSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineExerciseId: Long,
    val setNumber: Int,
    val reps: String,
    val weight: Float?,
    val setType: String
)