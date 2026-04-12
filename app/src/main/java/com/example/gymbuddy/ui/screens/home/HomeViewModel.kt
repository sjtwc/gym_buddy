package com.example.gymbuddy.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.RoutineRepository
import com.example.gymbuddy.data.repository.UserProfileRepository
import com.example.gymbuddy.data.repository.WorkoutRepository
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.UserProfile
import com.example.gymbuddy.domain.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userProfile: UserProfile? = null,
    val nextWorkout: Routine? = null,
    val recentWorkouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        userProfileRepository.getUserProfile()
            .onEach { profile ->
                _uiState.update { it.copy(userProfile = profile, isLoading = false) }
            }
            .launchIn(viewModelScope)
        
        routineRepository.getAllRoutines()
            .map { it.firstOrNull() }
            .onEach { routine ->
                _uiState.update { it.copy(nextWorkout = routine) }
            }
            .launchIn(viewModelScope)
        
        workoutRepository.getRecentCompletedWorkouts(3)
            .onEach { workouts ->
                _uiState.update { it.copy(recentWorkouts = workouts) }
            }
            .launchIn(viewModelScope)
    }
    
    fun startQuickWorkout() {
        viewModelScope.launch {
            val workout = Workout(
                name = "Quick Workout",
                date = System.currentTimeMillis(),
                isCompleted = false
            )
            workoutRepository.startWorkout(workout)
        }
    }
}