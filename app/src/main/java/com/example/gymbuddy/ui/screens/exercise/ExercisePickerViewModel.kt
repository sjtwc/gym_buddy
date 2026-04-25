package com.csci3310.gymbuddy.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csci3310.gymbuddy.data.repository.ExerciseRepository
import com.csci3310.gymbuddy.domain.model.Exercise
import com.csci3310.gymbuddy.domain.model.MuscleGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption(val displayName: String) {
    A_Z("A-Z"),
    Z_A("Z-A"),
    RECENTLY_USED("Recently Used")
}

data class ExercisePickerUiState(
    val exercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val selectedExercises: Set<Exercise> = emptySet(),
    val searchQuery: String = "",
    val selectedMuscleGroup: String? = null,
    val sortOption: SortOption = SortOption.A_Z,
    val isLoading: Boolean = true
)

@HiltViewModel
class ExercisePickerViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExercisePickerUiState())
    val uiState: StateFlow<ExercisePickerUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        exerciseRepository.getAllExercises()
            .onEach { exercises ->
                _uiState.update {
                    it.copy(
                        exercises = exercises,
                        filteredExercises = filterAndSortExercises(
                            exercises,
                            it.searchQuery,
                            it.selectedMuscleGroup,
                            it.sortOption
                        ),
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredExercises = filterAndSortExercises(
                    state.exercises,
                    query,
                    state.selectedMuscleGroup,
                    state.sortOption
                )
            )
        }
    }

    fun updateMuscleGroupFilter(muscleGroup: String?) {
        _uiState.update { state ->
            state.copy(
                selectedMuscleGroup = muscleGroup,
                filteredExercises = filterAndSortExercises(
                    state.exercises,
                    state.searchQuery,
                    muscleGroup,
                    state.sortOption
                )
            )
        }
    }

    fun updateSortOption(sortOption: SortOption) {
        _uiState.update { state ->
            state.copy(
                sortOption = sortOption,
                filteredExercises = filterAndSortExercises(
                    state.exercises,
                    state.searchQuery,
                    state.selectedMuscleGroup,
                    sortOption
                )
            )
        }
    }

    fun toggleExerciseSelection(exercise: Exercise) {
        _uiState.update { state ->
            val newSelected = if (state.selectedExercises.contains(exercise)) {
                state.selectedExercises - exercise
            } else {
                state.selectedExercises + exercise
            }
            state.copy(selectedExercises = newSelected)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedExercises = emptySet()) }
    }

    fun getSelectedExercises(): List<Exercise> {
        return _uiState.value.selectedExercises.toList()
    }

    private fun filterAndSortExercises(
        exercises: List<Exercise>,
        searchQuery: String,
        muscleGroup: String?,
        sortOption: SortOption
    ): List<Exercise> {
        var filtered = exercises

        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.targetMuscle.contains(searchQuery, ignoreCase = true)
            }
        }

        if (muscleGroup != null) {
            filtered = filtered.filter { it.targetMuscle == muscleGroup }
        }

        filtered = when (sortOption) {
            SortOption.A_Z -> filtered.sortedBy { it.name }
            SortOption.Z_A -> filtered.sortedByDescending { it.name }
            SortOption.RECENTLY_USED -> filtered.sortedByDescending { it.createdAt }
        }

        return filtered
    }

    fun getGroupedExercises(): Map<String, List<Exercise>> {
        return _uiState.value.filteredExercises
            .groupBy { it.targetMuscle }
            .toSortedMap()
    }
}