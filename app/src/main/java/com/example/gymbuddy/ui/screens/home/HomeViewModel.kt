package com.example.gymbuddy.ui.screens.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.RoutineRepository
import com.example.gymbuddy.data.repository.UserProfileRepository
import com.example.gymbuddy.data.repository.WorkoutRepository
import com.example.gymbuddy.domain.model.GymChain
import com.example.gymbuddy.domain.model.GymLocation
import com.example.gymbuddy.domain.model.PetMood
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.UserProfile
import com.example.gymbuddy.domain.model.VirtualPet
import com.example.gymbuddy.domain.model.Workout
import com.example.gymbuddy.service.LocationService
import com.example.gymbuddy.util.GymLocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userProfile: UserProfile? = null,
    val nextWorkout: Routine? = null,
    val recentWorkouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true,
    val selectedGymChain: GymChain? = null,
    val userLocation: Location? = null,
    val nearestGym: GymLocation? = null,
    val isLoadingLocation: Boolean = false,
    val locationError: String? = null,
    val effectivePetMood: PetMood = PetMood.NEUTRAL,
    val effectivePetHappiness: Int = 50
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
    private val locationService: LocationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val gymChains: List<GymChain> = GymLocationHelper.getAllChains()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        userProfileRepository.getUserProfile()
            .onEach { profile ->
                val lastWorkoutDate = profile?.lastWorkoutDate
                val baseHappiness = profile?.pet?.happiness ?: 50
                val effectiveMood = VirtualPet.calculateMood(lastWorkoutDate)
                val effectiveHappiness = VirtualPet.calculateHappiness(lastWorkoutDate, baseHappiness)
                
                _uiState.update { 
                    it.copy(
                        userProfile = profile, 
                        isLoading = false,
                        effectivePetMood = effectiveMood,
                        effectivePetHappiness = effectiveHappiness
                    ) 
                }
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

    fun selectGymChain(chain: GymChain) {
        _uiState.update { it.copy(selectedGymChain = chain, nearestGym = null, locationError = null) }
        
        if (locationService.hasLocationPermission()) {
            findNearestGym(chain)
        } else {
            _uiState.update { it.copy(locationError = "Location permission required") }
        }
    }

    fun onLocationPermissionGranted() {
        val chain = _uiState.value.selectedGymChain
        if (chain != null) {
            findNearestGym(chain)
        }
    }

    private fun findNearestGym(chain: GymChain) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true, locationError = null) }
            
            try {
                val lastLocation = locationService.getLastKnownLocation()
                if (lastLocation != null) {
                    _uiState.update { it.copy(userLocation = lastLocation) }
                    
                    val nearest = GymLocationHelper.findNearestGym(
                        lastLocation.latitude,
                        lastLocation.longitude,
                        chain
                    )
                    _uiState.update { 
                        it.copy(
                            nearestGym = nearest, 
                            isLoadingLocation = false
                        )
                    }
                } else {
                    locationService.getCurrentLocation()
                        .first()
                        .let { location ->
                            _uiState.update { it.copy(userLocation = location) }
                            
                            val nearest = GymLocationHelper.findNearestGym(
                                location.latitude,
                                location.longitude,
                                chain
                            )
                            _uiState.update { 
                                it.copy(
                                    nearestGym = nearest, 
                                    isLoadingLocation = false
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        locationError = "Could not get location: ${e.message}",
                        isLoadingLocation = false
                    )
                }
            }
        }
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

    fun hasLocationPermission(): Boolean = locationService.hasLocationPermission()
}