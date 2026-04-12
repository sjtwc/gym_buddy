package com.example.gymbuddy.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.local.dao.SetDao
import com.example.gymbuddy.data.local.dao.WorkoutDao
import com.example.gymbuddy.data.repository.ExerciseRepository
import com.example.gymbuddy.data.repository.SetRepository
import com.example.gymbuddy.domain.model.WorkoutSet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseInsightViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository,
    private val workoutDao: WorkoutDao,
    private val setDao: SetDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseInsightState())
    val uiState: StateFlow<ExerciseInsightState> = _uiState.asStateFlow()

    fun loadExerciseData(exerciseId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val exercise = exerciseRepository.getExerciseById(exerciseId)
                _uiState.update { it.copy(exercise = exercise) }

                loadHistoricalData(exerciseId)
                calculateGraphData()
                calculatePredictedRecords()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private suspend fun loadHistoricalData(exerciseId: Long) {
        workoutDao.getWorkoutsWithExercise(exerciseId, 50).collect { workouts ->
            val historicalSets = workouts.mapNotNull { workout ->
                val sets = setDao.getPreviousSetsForExercise(exerciseId, 100).first()
                    .filter { it.workoutExerciseId in getWorkoutExerciseIds(workout.id, exerciseId) }
                
                if (sets.isNotEmpty()) {
                    HistoricalSetWithDate(
                        date = workout.date,
                        sets = sets.map { entity ->
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
                } else null
            }.sortedByDescending { it.date }

            _uiState.update { 
                it.copy(
                    historicalSets = historicalSets,
                    isLoading = false
                ) 
            }
        }
    }

    private suspend fun getWorkoutExerciseIds(workoutId: Long, exerciseId: Long): List<Long> {
        return emptyList()
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

        val recentSessions = sets.take(5)
        
        val best1RM = recentSessions.flatMap { it.sets }
            .filter { !it.isWarmUp }
            .maxOfOrNull { calculateEstimated1RM(it.weight, it.reps) } ?: 0f

        val daysSinceLast = if (sets.isNotEmpty()) {
            ((System.currentTimeMillis() - sets.first().date) / (1000 * 60 * 60 * 24)).toInt()
        } else 0

        val predicted1RM = predictRecordEpley(best1RM, daysSinceLast)
        
        val predicted3RM = predictRecordEpley(best1RM * 0.93f, daysSinceLast)
        val predicted5RM = predictRecordEpley(best1RM * 0.87f, daysSinceLast)

        _uiState.update {
            it.copy(predictedRecords = PredictedRecords(
                oneRM = predicted1RM,
                threeRM = predicted3RM,
                fiveRM = predicted5RM
            ))
        }
    }

    private fun calculateEstimated1RM(weight: Float, reps: Int): Float {
        if (reps <= 0 || weight <= 0) return 0f
        if (reps == 1) return weight
        return weight * (1 + reps / 30f)
    }

    private fun predictRecordEpley(historicalBest: Float, daysSinceLast: Int): Float {
        if (historicalBest <= 0) return 0f
        
        val progressionRate = 0.005f
        val weeks = daysSinceLast / 7f
        
        return historicalBest * (1 + (progressionRate * weeks))
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
        
        val bestEst1RM = allSets.maxOfOrNull { calculateEstimated1RM(it.weight, it.reps) } ?: 0f
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
}

data class InsightSummary(
    val bestEstimated1RM: Float,
    val bestMaxWeight: Float,
    val totalVolume: Float,
    val maxReps: Int
)