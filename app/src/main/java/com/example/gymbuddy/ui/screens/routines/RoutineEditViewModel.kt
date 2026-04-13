package com.example.gymbuddy.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.ExerciseRepository
import com.example.gymbuddy.data.repository.RoutineRepository
import com.example.gymbuddy.domain.model.Exercise
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.RoutineExercise
import com.example.gymbuddy.domain.model.RoutineSetData
import com.example.gymbuddy.domain.model.SetType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineEditUiState(
    val routineId: Long = 0,
    val routineName: String = "",
    val routineDescription: String = "",
    val routineType: String = "Custom",
    val difficulty: String = "Intermediate",
    val exercises: List<RoutineExercise> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class RoutineEditViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineEditUiState())
    val uiState: StateFlow<RoutineEditUiState> = _uiState.asStateFlow()

    private var allExercises: List<Exercise> = emptyList()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            exerciseRepository.getAllExercises()
                .first()
                .let { allExercises = it }
        }
    }

    fun loadRoutine(routineId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            routineRepository.getRoutineById(routineId)?.let { routine ->
                _uiState.update {
                    it.copy(
                        routineId = routine.id,
                        routineName = routine.name,
                        routineDescription = routine.description ?: "",
                        routineType = routine.type,
                        difficulty = routine.difficulty,
                        exercises = routine.exercises,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateRoutineName(name: String) {
        _uiState.update { it.copy(routineName = name) }
    }

    fun updateRoutineDescription(description: String) {
        _uiState.update { it.copy(routineDescription = description) }
    }

    fun addExercise(exercise: Exercise) {
        val defaultSets = List(3) { i ->
            RoutineSetData(
                setNumber = i + 1,
                reps = "8-12",
                weight = null,
                setType = SetType.NORMAL
            )
        }
        
        val newExercise = RoutineExercise(
            id = 0,
            routineId = _uiState.value.routineId,
            exercise = exercise,
            orderIndex = _uiState.value.exercises.size,
            targetSets = 3,
            targetReps = "8-12",
            sets = defaultSets
        )
        _uiState.update {
            it.copy(exercises = it.exercises + newExercise)
        }
    }

    fun removeExercise(index: Int) {
        val updatedList = _uiState.value.exercises.toMutableList().apply {
            removeAt(index)
        }.mapIndexed { idx, ex -> ex.copy(orderIndex = idx) }
        _uiState.update { it.copy(exercises = updatedList) }
    }

    fun updateExerciseSets(index: Int, sets: List<RoutineSetData>) {
        val updatedList = _uiState.value.exercises.toMutableList()
        if (index < updatedList.size) {
            updatedList[index] = updatedList[index].copy(
                sets = sets,
                targetSets = sets.size
            )
            _uiState.update { it.copy(exercises = updatedList) }
        }
    }

    fun updateExerciseBodyFocus(index: Int, bodyFocus: String) {
        val updatedList = _uiState.value.exercises.toMutableList()
        if (index < updatedList.size) {
            updatedList[index] = updatedList[index].copy(bodyFocus = bodyFocus)
            _uiState.update { it.copy(exercises = updatedList) }
        }
    }

    fun saveRoutine() {
        viewModelScope.launch {
            val state = _uiState.value
            val routine = Routine(
                id = state.routineId,
                name = state.routineName.ifBlank { "New Routine" },
                description = state.routineDescription.ifBlank { null },
                type = state.routineType,
                difficulty = state.difficulty,
                isCustom = true,
                exercises = state.exercises
            )

            if (state.routineId > 0) {
                routineRepository.updateRoutine(routine)
            } else {
                routineRepository.insertRoutine(routine)
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
