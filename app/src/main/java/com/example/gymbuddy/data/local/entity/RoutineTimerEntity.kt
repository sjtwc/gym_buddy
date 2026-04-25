package com.csci3310.gymbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_timers",
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
data class RoutineTimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineExerciseId: Long,
    val setType: String,
    val durationSeconds: Int
)