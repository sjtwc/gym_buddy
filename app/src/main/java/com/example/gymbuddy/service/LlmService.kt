package com.example.gymbuddy.service

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _generationFlow = MutableSharedFlow<String>(replay = 0)
    val generationFlow: SharedFlow<String> = _generationFlow.asSharedFlow()

    private val _isModelLoaded = MutableSharedFlow<Boolean>(replay = 1)
    val isModelLoaded: SharedFlow<Boolean> = _isModelLoaded.asSharedFlow()

    private var petName: String = "GymBot"

    init {
        _isModelLoaded.tryEmit(false)
    }

    suspend fun ensureModelReady(): Boolean {
        _isModelLoaded.tryEmit(false)
        return false
    }

    suspend fun generate(
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.7f,
        stopTokens: List<String> = listOf("<end_of_turn>", "\n\n")
    ) = withContext(Dispatchers.IO) {
        _generationFlow.tryEmit("")
        petName = extractPetName(prompt)

        val response = "no response from $petName"

        for (char in response) {
            _generationFlow.tryEmit(char.toString())
            delay(15)
        }
    }

    private fun extractPetName(prompt: String): String {
        val regex = Regex("You are (.+?), a motivational")
        val match = regex.find(prompt)
        return match?.groupValues?.get(1) ?: "GymBot"
    }

    fun unload() {
        _isModelLoaded.tryEmit(false)
        Log.d(TAG, "Model unloaded (stub)")
    }

    companion object {
        private const val TAG = "LlmService"
    }
}