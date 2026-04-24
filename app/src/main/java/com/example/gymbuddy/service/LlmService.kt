package com.example.gymbuddy.service

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.google.ai.edge.litertlm.SamplerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var engine: Engine? = null
    private var conversation: Conversation? = null

    private val _generationFlow = MutableSharedFlow<String>(replay = 0)
    val generationFlow: SharedFlow<String> = _generationFlow.asSharedFlow()

    private val _isModelLoaded = MutableSharedFlow<Boolean>(replay = 1)
    val isModelLoaded: SharedFlow<Boolean> = _isModelLoaded.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val modelFileName = "gemma3-270m-it-q8.litertlm"

    private val fallbackResponses = listOf(
        "Oops! My brain is taking a nap right now 💤 Try again in a moment, coach!",
        "Hmm, I'm having a mental block! 😅 Give me another shot in a bit!",
        "My circuits need a quick reboot ⚡ Stand by, fitness friend!",
        "Whoops! I tripped over my protein shake! 🍹 Back in action soon!",
        "Error 404: Motivation not found! 🔍 Let me search my archives..."
    )

    suspend fun ensureModelReady(): Boolean = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            if (engine != null) {
                _isModelLoaded.tryEmit(true)
                return@withContext true
            }

            val modelFile = getModelFile()
            if (!modelFile.exists()) {
                Log.e(TAG, "Model file not found: ${modelFile.absolutePath}")
                _isModelLoaded.tryEmit(false)
                return@withContext false
            }

            val engineConfig = EngineConfig(
                modelPath = modelFile.absolutePath,
                backend = Backend.GPU,
                maxNumTokens = 512,
                cacheDir = context.cacheDir.absolutePath
            )

            engine = Engine(engineConfig)
            engine?.initialize()

            _isModelLoaded.tryEmit(true)
            Log.d(TAG, "Model loaded successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model", e)
            _isModelLoaded.tryEmit(false)
            false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun generate(
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.7f,
        stopTokens: List<String> = listOf("<end_of_turn>", "\n\n")
    ) = withContext(Dispatchers.IO) {
        _generationFlow.tryEmit("")

        val currentEngine = engine
        if (currentEngine == null) {
            Log.e(TAG, "Engine not initialized")
            emitFallback()
            return@withContext
        }

        try {
            val systemMessage = Message.of(
                Content.Text("You are GymBot, a motivational AI fitness coach. Be cheerful, supportive, and give practical advice. Keep responses concise and conversational.")
            )
            val samplerConfig = SamplerConfig(
                topK = 40,
                topP = 0.95,
                temperature = temperature.toDouble(),
                seed = 0
            )
            val conversationConfig = ConversationConfig(
                systemMessage = systemMessage,
                samplerConfig = samplerConfig
            )

            currentEngine.createConversation(conversationConfig).use { conv ->
                conversation = conv
                val message = Message.of(Content.Text(prompt))

                conv.sendMessageAsync(message, object : MessageCallback {
                    override fun onMessage(msg: Message) {
                        msg.contents.firstOrNull()?.let { content ->
                            when (content) {
                                is Content.Text -> {
                                    _generationFlow.tryEmit(content.text)
                                }
                                else -> {}
                            }
                        }
                    }

                    override fun onDone() {
                        Log.d(TAG, "Generation complete")
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "Generation error", e)
                        _generationFlow.tryEmit("")
                    }
                })
            }
            conversation = null
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
            conversation?.close()
            conversation = null
            engine?.close()
            engine = null
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
    }
}