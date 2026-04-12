package com.example.gymbuddy.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.RoutineRepository
import com.example.gymbuddy.domain.model.Exercise
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.RoutineExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateRoutineUiState(
    val selectedExercises: List<Exercise> = emptyList()
)


@HiltViewModel
class CreateRoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRoutineUiState())
    val uiState: StateFlow<CreateRoutineUiState> = _uiState.asStateFlow()

    fun addExercises(exercises: List<Exercise>) {
        _uiState.update { current ->
            current.copy(
                selectedExercises = current.selectedExercises + exercises
            )
        }
    }

    fun removeExercise(exercise: Exercise) {
        _uiState.update { current ->
            current.copy(
                selectedExercises = current.selectedExercises.filter { it.id != exercise.id }
            )
        }
    }

    fun saveRoutine(name: String, type: String, difficulty: String, exercises: List<Exercise>) {
        viewModelScope.launch {
            val routineExercises = exercises.mapIndexed { index, exercise ->
                RoutineExercise(
                    routineId = 0,
                    exercise = exercise,
                    orderIndex = index,
                    targetSets = 3,
                    targetReps = "8-12",
                    restSeconds = 90
                )
            }

            val routine = Routine(
                name = name,
                description = "Custom routine created by user",
                type = type,
                difficulty = difficulty,
                estimatedMinutes = exercises.size * 15,
                isCustom = true,
                exercises = routineExercises
            )

            routineRepository.insertRoutine(routine)
        }
    }
}
