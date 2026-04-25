package com.csci3310.gymbuddy.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csci3310.gymbuddy.data.repository.ExerciseRepository
import com.csci3310.gymbuddy.domain.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseUiState(
    val exercises: List<Exercise> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscle: String? = null,
    val muscleGroups: List<String> = listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core"),
    val isLoading: Boolean = true
)

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()
    
    init {
        loadExercises()
    }
    
    private fun loadExercises() {
        exerciseRepository.getAllExercises()
            .onEach { exercises ->
                _uiState.update { state ->
                    state.copy(
                        exercises = filterExercises(exercises, state.searchQuery, state.selectedMuscle),
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshFilteredExercises()
    }
    
    fun filterByMuscle(muscle: String?) {
        _uiState.update { it.copy(selectedMuscle = muscle) }
        refreshFilteredExercises()
    }
    
    private fun refreshFilteredExercises() {
        viewModelScope.launch {
            exerciseRepository.getAllExercises().first().let { exercises ->
                _uiState.update { state ->
                    state.copy(
                        exercises = filterExercises(exercises, state.searchQuery, state.selectedMuscle)
                    )
                }
            }
        }
    }
    
    private fun filterExercises(exercises: List<Exercise>, query: String, muscle: String?): List<Exercise> {
        return exercises.filter { exercise ->
            val matchesQuery = query.isEmpty() || exercise.name.contains(query, ignoreCase = true)
            val matchesMuscle = muscle == null || exercise.targetMuscle == muscle
            matchesQuery && matchesMuscle
        }
    }
}