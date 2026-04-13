package com.example.gymbuddy.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.UserProfileRepository
import com.example.gymbuddy.domain.model.PetMood
import com.example.gymbuddy.domain.model.UserProfile
import com.example.gymbuddy.domain.model.VirtualPet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = true,
    val effectivePetMood: PetMood = PetMood.NEUTRAL,
    val effectivePetHappiness: Int = 50
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
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
    }
}