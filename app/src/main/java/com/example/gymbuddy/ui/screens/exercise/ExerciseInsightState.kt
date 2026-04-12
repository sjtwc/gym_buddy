package com.example.gymbuddy.ui.screens.exercise

import com.example.gymbuddy.domain.model.Exercise
import com.example.gymbuddy.domain.model.WorkoutSet

data class ExerciseInsightState(
    val exercise: Exercise? = null,
    val historicalSets: List<HistoricalSetWithDate> = emptyList(),
    val personalRecords: List<PersonalRecord> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val graphData: GraphData? = null,
    val predictedRecords: PredictedRecords? = null
)

data class HistoricalSetWithDate(
    val date: Long,
    val sets: List<WorkoutSet>,
    val workoutId: Long
)

data class GraphData(
    val bestEstimated1RM: List<GraphPoint>,
    val bestMaxWeight: List<GraphPoint>,
    val totalVolume: List<GraphPoint>,
    val maxReps: List<GraphPoint>
)

data class GraphPoint(
    val date: Long,
    val value: Float
)

data class PredictedRecords(
    val oneRM: Float?,
    val twoRM: Float?,
    val threeRM: Float?,
    val fourRM: Float?,
    val fiveRM: Float?,
    val sixRM: Float?,
    val sevenRM: Float?,
    val eightRM: Float?,
    val nineRM: Float?,
    val tenRM: Float?,
    val elevenRM: Float?,
    val twelveRM: Float?
)

data class PersonalRecord(
    val type: String,
    val value: Float,
    val reps: Int,
    val weight: Float,
    val date: Long
)