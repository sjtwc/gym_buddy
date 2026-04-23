package com.example.gymbuddy.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.UserProfileRepository
import com.example.gymbuddy.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileEditUiState(
    val name: String = "",
    val petName: String = "",
    val gender: String? = null,
    val age: String? = null,
    val height: String? = null,
    val weight: String? = null,
    val avatarUri: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private var originalProfile: UserProfile? = null

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val profile = userProfileRepository.getUserProfileSync()
                originalProfile = profile
                profile?.let { p ->
                    _uiState.update {
                        it.copy(
                            name = p.name,
                            petName = p.pet.name,
                            gender = p.gender,
                            age = p.age?.toString(),
                            height = p.height?.toString(),
                            weight = p.weight?.toString(),
                            avatarUri = p.avatarUri,
                            isLoading = false
                        )
                    }
                } ?: run {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updatePetName(petName: String) {
        _uiState.update { it.copy(petName = petName) }
    }

    fun updateGender(gender: String) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun updateAge(age: String) {
        _uiState.update { it.copy(age = age) }
    }

    fun updateHeight(height: String) {
        _uiState.update { it.copy(height = height) }
    }

    fun updateWeight(weight: String) {
        _uiState.update { it.copy(weight = weight) }
    }

    fun updateAvatarUri(uri: String?) {
        _uiState.update { it.copy(avatarUri = uri) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val currentState = _uiState.value
                val profile = originalProfile ?: UserProfile()

                val updatedProfile = profile.copy(
                    name = currentState.name.ifBlank { "Trainer" },
                    pet = profile.pet.copy(name = currentState.petName.ifBlank { "GymBot" }),
                    gender = currentState.gender,
                    age = currentState.age?.toIntOrNull(),
                    height = currentState.height?.toFloatOrNull(),
                    weight = currentState.weight?.toFloatOrNull(),
                    avatarUri = currentState.avatarUri
                )

                userProfileRepository.updateUserProfile(updatedProfile)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
