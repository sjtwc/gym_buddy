package com.csci3310.gymbuddy.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csci3310.gymbuddy.data.local.dao.PersonalRecordDao
import com.csci3310.gymbuddy.data.local.dao.SetDao
import com.csci3310.gymbuddy.data.local.dao.WorkoutDao
import com.csci3310.gymbuddy.data.local.dao.WorkoutExerciseDao
import com.csci3310.gymbuddy.data.local.entity.PersonalRecordEntity
import com.csci3310.gymbuddy.data.repository.ExerciseRepository
import com.csci3310.gymbuddy.data.repository.SetRepository
import com.csci3310.gymbuddy.domain.model.WorkoutSet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseInsightViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val setDao: SetDao,
    private val personalRecordDao: PersonalRecordDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseInsightState())
    val uiState: StateFlow<ExerciseInsightState> = _uiState.asStateFlow()

    private var currentExerciseId: Long = 0

    fun loadExerciseData(exerciseId: Long) {
        currentExerciseId = exerciseId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val exercise = exerciseRepository.getExerciseById(exerciseId)
                _uiState.update { it.copy(exercise = exercise) }

                loadHistoricalData(exerciseId)
                calculateGraphData()
                calculateAndSaveRepMaxRecords(exerciseId)
                calculatePredictedRecords()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private suspend fun loadHistoricalData(exerciseId: Long) {
        val workouts = workoutDao.getWorkoutsWithExercise(exerciseId, 50).first()
        
        val historicalSets = workouts.mapNotNull { workout ->
            val workoutExerciseIds = getWorkoutExerciseIds(workout.id, exerciseId)
            if (workoutExerciseIds.isEmpty()) return@mapNotNull null
            
            val allSets = workoutExerciseIds.flatMap { weId ->
                setDao.getSetsForWorkoutExerciseOnce(weId)
            }
            
            if (allSets.isEmpty()) {
                null
            } else {
                HistoricalSetWithDate(
                    date = workout.date,
                    sets = allSets.map { entity ->
                        WorkoutSet(
                            id = entity.id,
                            workoutExerciseId = entity.workoutExerciseId,
                            setNumber = entity.setNumber,
                            reps = entity.reps,
                            weight = entity.weight,
                            rpe = entity.rpe,
                            isWarmUp = entity.isWarmUp,
                            isDropSet = entity.isDropSet,
                            isFailureSet = entity.isFailureSet,
                            isSuperset = entity.isSuperset,
                            notes = entity.notes,
                            completedAt = entity.completedAt
                        )
                    },
                    workoutId = workout.id
                )
            }
        }.sortedByDescending { it.date }

        _uiState.update { 
            it.copy(
                historicalSets = historicalSets,
                isLoading = false
            ) 
        }
    }

    private suspend fun getWorkoutExerciseIds(workoutId: Long, exerciseId: Long): List<Long> {
        return workoutExerciseDao.getWorkoutExerciseIdsForWorkout(workoutId, exerciseId)
    }

    private suspend fun calculateAndSaveRepMaxRecords(exerciseId: Long) {
        val sets = _uiState.value.historicalSets
        if (sets.isEmpty()) return

        val allSets = sets.flatMap { it.sets }.filter { !it.isWarmUp }
        
        val repMaxRecords = (1..12).mapNotNull { targetReps ->
            val matchingSets = allSets.filter { it.reps == targetReps }
            if (matchingSets.isEmpty()) return@mapNotNull null
            
            val bestSet = matchingSets.maxByOrNull { it.weight } ?: return@mapNotNull null
            val estimated1RM = calculateEstimated1RM(bestSet.weight, bestSet.reps)
            
            PersonalRecordEntity(
                exerciseId = exerciseId,
                type = "REP_MAX",
                reps = targetReps,
                value = estimated1RM,
                weight = bestSet.weight,
                date = sets.first().date,
                workoutId = sets.first().workoutId
            )
        }
        
        if (repMaxRecords.isNotEmpty()) {
            personalRecordDao.insertRecords(repMaxRecords)
        }
    }

    private fun calculateGraphData() {
        val sets = _uiState.value.historicalSets
        
        val bestEstimated1RM = sets.map { session ->
            val bestEst1RM = session.sets
                .filter { !it.isWarmUp }
                .maxOfOrNull { calculateEstimated1RM(it.weight, it.reps) } ?: 0f
            GraphPoint(session.date, bestEst1RM)
        }.sortedBy { it.date }

        val bestMaxWeight = sets.map { session ->
            val maxWeight = session.sets
                .filter { !it.isWarmUp }
                .maxOfOrNull { it.weight } ?: 0f
            GraphPoint(session.date, maxWeight)
        }.sortedBy { it.date }

        val totalVolume = sets.map { session ->
            val volume = session.sets
                .filter { !it.isWarmUp }
                .sumOf { (it.weight * it.reps).toDouble() }.toFloat()
            GraphPoint(session.date, volume)
        }.sortedBy { it.date }

        val maxReps = sets.map { session ->
            val maxReps = session.sets
                .filter { !it.isWarmUp }
                .maxOfOrNull { it.reps } ?: 0
            GraphPoint(session.date, maxReps.toFloat())
        }.sortedBy { it.date }

        _uiState.update {
            it.copy(graphData = GraphData(
                bestEstimated1RM = bestEstimated1RM,
                bestMaxWeight = bestMaxWeight,
                totalVolume = totalVolume,
                maxReps = maxReps
            ))
        }
    }

    private fun calculatePredictedRecords() {
        val sets = _uiState.value.historicalSets

        if (sets.isEmpty()) {
            _uiState.update { it.copy(predictedRecords = null) }
            return
        }

        // Time window: 1.5 months (6 weeks)
        val timeWindowMs = 6L * 7 * 24 * 60 * 60 * 1000
        val cutoffTime = System.currentTimeMillis() - timeWindowMs

        // Get records within time window
        val recentRecords = sets.filter { it.date >= cutoffTime }

        if (recentRecords.isEmpty()) {
            return
        }

        val allSets = recentRecords.flatMap { it.sets }.filter { !it.isWarmUp }

        // Find best actual record within window
        // Priority: 1RM > 2RM > 3RM > ... > 12RM
        val baseRecord: Pair<Int, Float>? = (1..12).firstNotNullOf { reps ->
            allSets.filter { it.reps == reps }.maxOfOrNull { it.weight }?.let { weight ->
                reps to weight
            }
        }

        if (baseRecord == null) {
            return
        }

        val (baseReps, baseWeight) = baseRecord
        val effective1RM = calculateEstimated1RM(baseWeight, baseReps)

        if (effective1RM <= 0) {
            return
        }

        val currentPredictions = _uiState.value.predictedRecords

        val predictedRecords = (1..12).map { reps ->
            val calculated = inverseEpleyFormula(effective1RM, reps)
            val current = when (reps) {
                1 -> currentPredictions?.oneRM
                2 -> currentPredictions?.twoRM
                3 -> currentPredictions?.threeRM
                4 -> currentPredictions?.fourRM
                5 -> currentPredictions?.fiveRM
                6 -> currentPredictions?.sixRM
                7 -> currentPredictions?.sevenRM
                8 -> currentPredictions?.eightRM
                9 -> currentPredictions?.nineRM
                10 -> currentPredictions?.tenRM
                11 -> currentPredictions?.elevenRM
                12 -> currentPredictions?.twelveRM
                else -> null
            }
            if (current != null && calculated <= current) current else calculated
        }

        _uiState.update {
            it.copy(predictedRecords = PredictedRecords(
                oneRM = predictedRecords.getOrNull(0),
                twoRM = predictedRecords.getOrNull(1),
                threeRM = predictedRecords.getOrNull(2),
                fourRM = predictedRecords.getOrNull(3),
                fiveRM = predictedRecords.getOrNull(4),
                sixRM = predictedRecords.getOrNull(5),
                sevenRM = predictedRecords.getOrNull(6),
                eightRM = predictedRecords.getOrNull(7),
                nineRM = predictedRecords.getOrNull(8),
                tenRM = predictedRecords.getOrNull(9),
                elevenRM = predictedRecords.getOrNull(10),
                twelveRM = predictedRecords.getOrNull(11)
            ))
        }
    }

    private fun inverseEpleyFormula(oneRM: Float, reps: Int): Float {
        if (reps <= 0 || oneRM <= 0) return 0f
        if (reps == 1) return oneRM
        return oneRM / (1 + reps / 30f)
    }

    fun resetPredictedRecords() {
        _uiState.update { it.copy(predictedRecords = null) }
    }

    fun getSummaryStats(): InsightSummary {
        val sets = _uiState.value.historicalSets
        
        if (sets.isEmpty()) {
            return InsightSummary(
                bestEstimated1RM = 0f,
                bestMaxWeight = 0f,
                totalVolume = 0f,
                maxReps = 0
            )
        }

        val allSets = sets.flatMap { it.sets }.filter { !it.isWarmUp }
        
        val actual1RMSets = allSets.filter { it.reps == 1 }
        val bestEst1RM = if (actual1RMSets.isNotEmpty()) {
            actual1RMSets.maxOf { it.weight }
        } else 0f
        val bestMaxWeight = allSets.maxOfOrNull { it.weight } ?: 0f
        val totalVolume = allSets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
        val maxReps = allSets.maxOfOrNull { it.reps } ?: 0

        return InsightSummary(
            bestEstimated1RM = bestEst1RM,
            bestMaxWeight = bestMaxWeight,
            totalVolume = totalVolume,
            maxReps = maxReps
        )
    }

    private fun calculateEstimated1RM(weight: Float, reps: Int): Float {
        if (reps <= 0 || weight <= 0) return 0f
        if (reps == 1) return weight
        return weight * (1 + reps / 30f)
    }
}

data class InsightSummary(
    val bestEstimated1RM: Float,
    val bestMaxWeight: Float,
    val totalVolume: Float,
    val maxReps: Int
)