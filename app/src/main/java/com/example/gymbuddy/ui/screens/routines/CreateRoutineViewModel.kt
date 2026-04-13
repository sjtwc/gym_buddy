package com.example.gymbuddy.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.RoutineRepository
import com.example.gymbuddy.domain.model.Exercise
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.RoutineExercise
import com.example.gymbuddy.domain.model.RoutineExerciseTimer
import com.example.gymbuddy.domain.model.RoutineSetData
import com.example.gymbuddy.domain.model.SetType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateRoutineUiState(
    val routineId: Long = 0,
    val routineName: String = "",
    val routineTypes: List<String> = emptyList(),
    val estimatedMinutes: Int = 60,
    val exercises: List<Exercise> = emptyList(),
    val exerciseSets: List<List<RoutineSetData>> = emptyList(),
    val exerciseBodyFocus: List<String> = emptyList(),
    val exerciseTimers: List<List<RoutineExerciseTimer>> = emptyList()
)

@HiltViewModel
class CreateRoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRoutineUiState())
    val uiState: StateFlow<CreateRoutineUiState> = _uiState.asStateFlow()

    fun loadRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.getRoutineByIdWithExercises(routineId)?.let { routine ->
                _uiState.update {
                    CreateRoutineUiState(
                        routineId = routine.id,
                        routineName = routine.name,
                        routineTypes = if (routine.type.contains(",")) {
                            routine.type.split(",").map { it.trim() }
                        } else {
                            listOf(routine.type)
                        },
                        estimatedMinutes = routine.estimatedMinutes,
                        exercises = routine.exercises.map { it.exercise },
                        exerciseSets = routine.exercises.map { it.sets.ifEmpty { 
                            List(it.targetSets) { i -> 
                                RoutineSetData(setNumber = i + 1, reps = it.targetReps, weight = null, setType = SetType.NORMAL) 
                            }
                        }},
                        exerciseBodyFocus = routine.exercises.map { it.bodyFocus },
                        exerciseTimers = routine.exercises.map { it.timers.ifEmpty {
                            listOf(
                                RoutineExerciseTimer(SetType.NORMAL, 90),
                                RoutineExerciseTimer(SetType.WARMUP, 60),
                                RoutineExerciseTimer(SetType.WORK, 90),
                                RoutineExerciseTimer(SetType.DROP, 60),
                                RoutineExerciseTimer(SetType.FAILURE, 90)
                            )
                        }}
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateRoutineUiState()
    }

    fun updateRoutineName(name: String) {
        _uiState.update { it.copy(routineName = name) }
    }

    fun toggleRoutineType(type: String) {
        _uiState.update { current ->
            val newTypes = if (type in current.routineTypes) {
                current.routineTypes - type
            } else {
                current.routineTypes + type
            }
            current.copy(routineTypes = newTypes)
        }
    }

    fun updateEstimatedMinutes(minutes: Int) {
        _uiState.update { it.copy(estimatedMinutes = minutes.coerceAtLeast(1)) }
    }

    fun addExercises(exercises: List<Exercise>) {
        _uiState.update { current ->
            val newExercises = current.exercises + exercises
            val defaultTimers = listOf(
                RoutineExerciseTimer(SetType.NORMAL, 90),
                RoutineExerciseTimer(SetType.WARMUP, 60),
                RoutineExerciseTimer(SetType.WORK, 90),
                RoutineExerciseTimer(SetType.DROP, 60),
                RoutineExerciseTimer(SetType.FAILURE, 90)
            )
            val newSets = current.exerciseSets + exercises.map { _ ->
                List(3) { i ->
                    RoutineSetData(
                        setNumber = i + 1,
                        reps = "",
                        weight = null,
                        setType = SetType.NORMAL
                    )
                }
            }
            val newBodyFocus = current.exerciseBodyFocus + exercises.map { "" }
            val newTimers = current.exerciseTimers + exercises.map { defaultTimers }
            current.copy(
                exercises = newExercises,
                exerciseSets = newSets,
                exerciseBodyFocus = newBodyFocus,
                exerciseTimers = newTimers
            )
        }
    }

    fun removeExercise(index: Int) {
        _uiState.update { current ->
            current.copy(
                exercises = current.exercises.filterIndexed { i, _ -> i != index },
                exerciseSets = current.exerciseSets.filterIndexed { i, _ -> i != index },
                exerciseBodyFocus = current.exerciseBodyFocus.filterIndexed { i, _ -> i != index },
                exerciseTimers = current.exerciseTimers.filterIndexed { i, _ -> i != index }
            )
        }
    }

    fun updateExerciseSets(index: Int, sets: List<RoutineSetData>) {
        _uiState.update { current ->
            val newSets = current.exerciseSets.toMutableList()
            newSets[index] = sets
            current.copy(exerciseSets = newSets)
        }
    }

    fun updateExerciseBodyFocus(index: Int, bodyFocus: String) {
        _uiState.update { current ->
            val newBodyFocus = current.exerciseBodyFocus.toMutableList()
            newBodyFocus[index] = bodyFocus
            current.copy(exerciseBodyFocus = newBodyFocus)
        }
    }

    fun updateExerciseTimers(index: Int, timers: List<RoutineExerciseTimer>) {
        _uiState.update { current ->
            val newTimers = current.exerciseTimers.toMutableList()
            newTimers[index] = timers
            current.copy(exerciseTimers = newTimers)
        }
    }

    fun saveRoutine() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val routineExercises = state.exercises.mapIndexed { index, exercise ->
                    RoutineExercise(
                        routineId = state.routineId,
                        exercise = exercise,
                        orderIndex = index,
                        targetSets = state.exerciseSets[index].size,
                        targetReps = state.exerciseSets[index].firstOrNull()?.reps?.ifBlank { "8-12" } ?: "8-12",
                        restSeconds = state.exerciseTimers[index].find { it.type == SetType.WORK }?.durationSeconds ?: 90,
                        bodyFocus = state.exerciseBodyFocus[index],
                        sets = state.exerciseSets[index],
                        timers = state.exerciseTimers[index]
                    )
                }

                val routine = Routine(
                    id = state.routineId,
                    name = state.routineName.ifBlank { "New Routine" },
                    description = "Custom routine created by user",
                    type = state.routineTypes.joinToString(","),
                    difficulty = "Intermediate",
                    estimatedMinutes = state.estimatedMinutes,
                    isCustom = true,
                    exercises = routineExercises
                )

                if (state.routineId > 0) {
                    routineRepository.updateRoutine(routine)
                } else {
                    routineRepository.insertRoutine(routine)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
