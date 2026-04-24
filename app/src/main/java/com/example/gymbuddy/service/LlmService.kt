package com.example.gymbuddy.service

import android.content.Context
import android.util.Log
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val engine = AiChat.getInferenceEngine(context)

    private val _generationFlow = MutableSharedFlow<String>(replay = 0)
    val generationFlow: SharedFlow<String> = _generationFlow.asSharedFlow()

    private val _isModelLoaded = MutableSharedFlow<Boolean>(replay = 1)
    val isModelLoaded: SharedFlow<Boolean> = _isModelLoaded.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val modelFileName = "gemma-3-270m-it-Q4_K_M.gguf"

    private var isModelReady = false

    private val fallbackResponses = listOf(
        "Oops! My brain is taking a nap right now 💤 Try again in a moment, coach!",
        "Hmm, I'm having a mental block! 😅 Give me another shot in a bit!",
        "My circuits need a quick reboot ⚡ Stand by, fitness friend!",
        "Whoops! I tripped over my protein shake! 🍹 Back in action soon!",
        "Error 404: Motivation not found! 🔍 Let me search my archives..."
    )

    init {
        observeEngineState()
    }

    private fun observeEngineState() {
        CoroutineScope(Dispatchers.IO).launch {
            engine.state.collect { state ->
                when (state) {
                    is InferenceEngine.State.ModelReady,
                    is InferenceEngine.State.Generating -> {
                        if (!isModelReady) {
                            isModelReady = true
                            _isModelLoaded.tryEmit(true)
                        }
                    }
                    is InferenceEngine.State.Initialized -> {
                        isModelReady = false
                        _isModelLoaded.tryEmit(false)
                    }
                    is InferenceEngine.State.Error -> {
                        isModelReady = false
                        _isModelLoaded.tryEmit(false)
                    }
                    else -> {}
                }
            }
        }
    }

    suspend fun ensureModelReady(): Boolean = withContext(Dispatchers.IO) {
        if (isModelReady) {
            _isModelLoaded.tryEmit(true)
            return@withContext true
        }

        _isLoading.value = true
        try {
            val modelFile = getModelFile()
            if (!modelFile.exists()) {
                Log.e(TAG, "Model file not found: ${modelFile.absolutePath}")
                _isModelLoaded.tryEmit(false)
                return@withContext false
            }

            Log.d(TAG, "Loading model from: ${modelFile.absolutePath}")
            engine.loadModel(modelFile.absolutePath)

            engine.setSystemPrompt(SYSTEM_PROMPT)

            isModelReady = true
            _isModelLoaded.tryEmit(true)
            Log.d(TAG, "Model loaded successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model", e)
            isModelReady = false
            _isModelLoaded.tryEmit(false)
            false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun generate(
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.7f
    ) = withContext(Dispatchers.IO) {
        _generationFlow.tryEmit("")

        if (!isModelReady) {
            Log.e(TAG, "Engine not ready")
            emitFallback()
            return@withContext
        }

        try {
            engine.sendUserPrompt(prompt, maxTokens).collect { token ->
                _generationFlow.tryEmit(token)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Generation error", e)
            emitFallback()
        }
    }

    private suspend fun emitFallback() {
        val fallback = fallbackResponses.random()
        for (char in fallback) {
            _generationFlow.tryEmit(char.toString())
            kotlinx.coroutines.delay(20)
        }
    }

    fun unload() {
        try {
            isModelReady = false
            engine.cleanUp()
            _isModelLoaded.tryEmit(false)
            Log.d(TAG, "Model unloaded")
        } catch (e: Exception) {
            Log.e(TAG, "Error unloading model", e)
        }
    }

    private fun getModelFile(): File {
        val destFile = File(context.filesDir, modelFileName)
        if (destFile.exists()) {
            return destFile
        }

        context.assets.open(modelFileName).use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return destFile
    }

    companion object {
        private const val TAG = "LlmService"
        private const val SYSTEM_PROMPT =
            "You are GymBuddy, a motivational AI fitness coach. Be cheerful, supportive, and give practical advice. Keep responses concise and conversational."
    }
}
