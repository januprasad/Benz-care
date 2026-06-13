package com.toyota.demo.ai

import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaEngine @Inject constructor() {

    private var engine: Engine? = null
    private var activeConversation: ManagedConversation? = null
    private val lock = Any()

    @Volatile
    var isInitialized: Boolean = false
        private set

    @Volatile
    var initError: String? = null
        private set

    @Volatile
    var backendName: String = "CPU"
        private set

    suspend fun initialize(modelPath: String, cacheDir: String) {
        withContext(Dispatchers.IO) {
            try {
                val modelFile = File(modelPath)
                if (!modelFile.exists()) {
                    initError = "Model file not found: $modelPath"
                    return@withContext
                }

                try {
                    @OptIn(ExperimentalApi::class)
                    run { ExperimentalFlags.enableSpeculativeDecoding = true }
                    val config = EngineConfig(
                        modelPath = modelPath,
                        backend = Backend.GPU(),
                        visionBackend = Backend.CPU(),
                        cacheDir = cacheDir
                    )
                    engine = Engine(config).also { it.initialize() }
                    isInitialized = true
                    initError = null
                    backendName = "GPU"
                    Log.i(TAG, "GemmaEngine initialized with GPU")
                } catch (gpuEx: Exception) {
                    Log.w(TAG, "GPU init failed, falling back to CPU", gpuEx)
                    val cpuConfig = EngineConfig(
                        modelPath = modelPath,
                        backend = Backend.CPU(),
                        visionBackend = Backend.CPU(),
                        cacheDir = cacheDir
                    )
                    engine = Engine(cpuConfig).also { it.initialize() }
                    isInitialized = true
                    initError = null
                    backendName = "CPU"
                }
            } catch (e: Exception) {
                initError = "Engine initialization failed: ${e.message}"
                Log.e(TAG, initError!!, e)
            }
        }
    }

    fun getOrCreateConversation(
        conversationId: String,
        config: ConversationConfig,
        systemPrompt: String
    ): ManagedConversation {
        synchronized(lock) {
            val existing = activeConversation
            if (existing != null && existing.id == conversationId) {
                if (existing.systemPrompt == systemPrompt) return existing
                closeActiveConversation()
            }
            closeActiveConversation()
            val eng = engine ?: throw IllegalStateException(initError ?: "Engine not initialized")
            val conversation = eng.createConversation(config)
            val managed = ManagedConversation(conversation, conversationId, systemPrompt)
            activeConversation = managed
            return managed
        }
    }

    fun resetConversation(
        conversationId: String,
        config: ConversationConfig,
        systemPrompt: String
    ): ManagedConversation {
        synchronized(lock) {
            closeActiveConversation()
            val eng = engine ?: throw IllegalStateException("Engine not initialized")
            val conversation = eng.createConversation(config)
            val managed = ManagedConversation(conversation, conversationId, systemPrompt)
            activeConversation = managed
            return managed
        }
    }

    fun closeConversation(conversationId: String) {
        synchronized(lock) {
            if (activeConversation?.id == conversationId) {
                closeActiveConversation()
            }
        }
    }

    private fun closeActiveConversation() {
        activeConversation?.let { managed ->
            try {
                managed.conversation.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error closing conversation", e)
            }
        }
        activeConversation = null
    }

    fun release() {
        synchronized(lock) {
            closeActiveConversation()
            engine?.close()
            engine = null
            isInitialized = false
        }
    }

    companion object {
        private const val TAG = "GemmaEngine"
    }
}

class ManagedConversation(
    val conversation: Conversation,
    val id: String,
    val systemPrompt: String
) {
    @Volatile
    var estimatedTokens: Int = 0
        private set

    @Volatile
    var turnCount: Int = 0
        private set

    fun recordExchange(userMessageChars: Int, responseChars: Int) {
        estimatedTokens += (userMessageChars + 3) / 4 + (responseChars + 3) / 4
        turnCount++
    }

    fun recordSystemPrompt(promptChars: Int) {
        estimatedTokens += (promptChars + 3) / 4
    }
}
