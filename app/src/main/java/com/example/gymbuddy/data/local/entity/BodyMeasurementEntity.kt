package com.csci3310.gymbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val weight: Float? = null,
    val bodyFat: Float? = null,
    val chest: Float? = null,
    val waist: Float? = null,
    val hips: Float? = null,
    val biceps: Float? = null,
    val thighs: Float? = null,
    val calves: Float? = null,
    val shoulders: Float? = null,
    val notes: String? = null
)