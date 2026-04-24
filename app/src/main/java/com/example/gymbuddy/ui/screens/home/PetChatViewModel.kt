package com.example.gymbuddy.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repository.ChatMessage
import com.example.gymbuddy.data.repository.LlmRepository
import com.example.gymbuddy.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isModelReady: Boolean = false,
    val petEmoji: String = "🤖",
    val aiPetEmoji: String = "🐱",
    val petName: String = "GymBuddy",
    val userAvatarUri: String? = null
)

@HiltViewModel
class PetChatViewModel @Inject constructor(
    private val llmRepository: LlmRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetChatUiState())
    val uiState: StateFlow<PetChatUiState> = _uiState.asStateFlow()

    private var currentResponse = StringBuilder()

    init {
        loadPetInfo()
        observeGeneration()
        observeModelStatus()
    }

    private fun loadPetInfo() {
        viewModelScope.launch {
            val profile = userProfileRepository.getUserProfileSync()
            _uiState.update {
                it.copy(
                    petName = profile?.pet?.name ?: "GymBot",
                    petEmoji = profile?.pet?.mood?.emoji ?: "🤖",
                    userAvatarUri = profile?.avatarUri
                )
            }
        }
    }

    private fun observeGeneration() {
        viewModelScope.launch {
            llmRepository.generationFlow.collect { token ->
                currentResponse.append(token)
                updateLastMessage(currentResponse.toString())
            }
        }
    }

    private fun observeModelStatus() {
        viewModelScope.launch {
            llmRepository.isModelLoaded.collect { loaded ->
                _uiState.update { it.copy(isModelReady = loaded) }
            }
        }
    }

    fun initializeAndGenerateGreeting() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val modelReady = llmRepository.ensureModelReady()
            if (modelReady) {
                currentResponse.clear()
                llmRepository.generateInitialGreeting(_uiState.value.petName)
            } else {
                addMessage("Sorry, I'm having trouble loading. Please try again later.", isUser = false)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        addMessage(message, isUser = true)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            currentResponse.clear()

            try {
                llmRepository.generateResponse(
                    userMessage = message,
                    chatHistory = _uiState.value.messages,
                    petName = _uiState.value.petName
                )
            } catch (e: Exception) {
                addMessage("Sorry, something went wrong. Please try again.", isUser = false)
            }
        }
    }

    fun onGenerationComplete() {
        _uiState.update { it.copy(isLoading = false) }
    }

    private fun addMessage(content: String, isUser: Boolean) {
        _uiState.update {
            it.copy(messages = it.messages + ChatMessage(content, isUser))
        }
    }

    private fun updateLastMessage(content: String) {
        _uiState.update { state ->
            if (state.messages.isEmpty()) {
                state
            } else {
                val updatedMessages = state.messages.dropLast(1) +
                    ChatMessage(content.trim(), isUser = false)
                state.copy(messages = updatedMessages)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}