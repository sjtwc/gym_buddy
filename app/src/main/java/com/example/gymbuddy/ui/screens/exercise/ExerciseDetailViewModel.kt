package com.csci3310.gymbuddy.ui.screens.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csci3310.gymbuddy.data.repository.ExerciseRepository
import com.csci3310.gymbuddy.domain.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseDetailUiState(
    val exercise: Exercise? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: 0L
    
    private val _uiState = MutableStateFlow(ExerciseDetailUiState())
    val uiState: StateFlow<ExerciseDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadExercise()
    }
    
    private fun loadExercise() {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExerciseById(exerciseId)
            _uiState.update { it.copy(exercise = exercise, isLoading = false) }
        }
    }
}