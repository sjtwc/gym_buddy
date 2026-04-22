package com.example.gymbuddy.ui.screens.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.RoutineRepository
import com.example.gymbuddy.data.repository.ScheduledWorkoutRepository
import com.example.gymbuddy.data.repository.UserProfileRepository
import com.example.gymbuddy.data.repository.WorkoutRepository
import com.example.gymbuddy.domain.model.GymChain
import com.example.gymbuddy.domain.model.GymLocation
import com.example.gymbuddy.domain.model.PetMood
import com.example.gymbuddy.domain.model.Routine
import com.example.gymbuddy.domain.model.UserProfile
import com.example.gymbuddy.domain.model.VirtualPet
import com.example.gymbuddy.domain.model.Workout
import com.example.gymbuddy.service.GeocodingService
import com.example.gymbuddy.service.LocationService
import com.example.gymbuddy.util.GymLocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val userProfile: UserProfile? = null,
    val nextWorkout: Routine? = null,
    val recentWorkouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true,
    val selectedGymChain: GymChain? = null,
    val userLocation: Location? = null,
    val nearestGym: GymLocation? = null,
    val chainGymLocations: List<GymLocation> = emptyList(),
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
    private val locationService: LocationService,
    private val geocodingService: GeocodingService,
    private val scheduledWorkoutRepository: ScheduledWorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val gymChains: List<GymChain> = GymLocationHelper.getAllChains()

    init {
        GymLocationHelper.inject(geocodingService)
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

        scheduledWorkoutRepository.getWeeklySchedule()
            .onEach { scheduledWorkouts ->
                val todayDayOfWeek = getCurrentDayOfWeek()
                val todaySchedule = scheduledWorkouts.find { it.dayOfWeek == todayDayOfWeek }
                val nextWorkout = if (todaySchedule?.isRestDay == false && todaySchedule?.routineId != null) {
                    scheduledWorkoutRepository.getRoutineById(todaySchedule.routineId)
                } else {
                    routineRepository.getAllRoutines().first().firstOrNull()
                }
                _uiState.update { it.copy(nextWorkout = nextWorkout) }
            }
            .launchIn(viewModelScope)

        workoutRepository.getRecentCompletedWorkouts(3)
            .onEach { workouts ->
                _uiState.update { it.copy(recentWorkouts = workouts) }
            }
            .launchIn(viewModelScope)
    }

    private fun getCurrentDayOfWeek(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
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

                    val chainLocations = GymLocationHelper.getLocationsByChain(chain)
                    val sortedGyms = GymLocationHelper.sortGymsByDistance(
                        lastLocation.latitude,
                        lastLocation.longitude,
                        chainLocations
                    ).take(15)

                    val nearest = sortedGyms.firstOrNull()
                    _uiState.update {
                        it.copy(
                            nearestGym = nearest,
                            chainGymLocations = sortedGyms,
                            isLoadingLocation = false
                        )
                    }
                } else {
                    locationService.getCurrentLocation()
                        .first()
                        .let { location ->
                            _uiState.update { it.copy(userLocation = location) }

                            val chainLocations = GymLocationHelper.getLocationsByChain(chain)
                            val sortedGyms = GymLocationHelper.sortGymsByDistance(
                                location.latitude,
                                location.longitude,
                                chainLocations
                            ).take(15)

                            val nearest = sortedGyms.firstOrNull()
                            _uiState.update {
                                it.copy(
                                    nearestGym = nearest,
                                    chainGymLocations = sortedGyms,
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