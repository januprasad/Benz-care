package com.toyota.demo.ai

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.toyota.demo.ai.model.ModelDownloadManager
import com.toyota.demo.ai.prompts.PromptRouter
import com.toyota.demo.core.model.AiRequest
import com.toyota.demo.core.model.AiResponse
import com.toyota.demo.core.model.ChatMessage
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class GemmaService : LifecycleService() {

    @Inject lateinit var engine: GemmaEngine
    @Inject lateinit var promptRouter: PromptRouter
    @Inject lateinit var modelDownloadManager: ModelDownloadManager

    private val binder = GemmaBinder()

    private val _isEngineReady = MutableStateFlow(false)
    val isEngineReady: StateFlow<Boolean> = _isEngineReady.asStateFlow()

    inner class GemmaBinder : Binder() {
        fun getService(): GemmaService = this@GemmaService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, buildNotification("Initializing…"), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, buildNotification("Initializing…"))
        }

        lifecycleScope.launch(Dispatchers.IO) {
            if (modelDownloadManager.isModelAvailable()) {
                initializeEngine()
            }
        }

        lifecycleScope.launch {
            modelDownloadManager.isModelAvailableFlow.collect { available ->
                if (available && !engine.isInitialized) {
                    withContext(Dispatchers.IO) { initializeEngine() }
                }
            }
        }
    }

    private suspend fun initializeEngine() {
        try {
            engine.initialize(
                modelPath = modelDownloadManager.getModelPath(),
                cacheDir = cacheDir.path
            )
            _isEngineReady.value = true
            updateNotification("AI engine active")
        } catch (e: Exception) {
            Log.e("GemmaService", "Failed to initialize engine", e)
        }
    }

    suspend fun process(request: AiRequest): AiResponse {
        if (!engine.isInitialized) return AiResponse.ErrorResponse("Engine not initialized")
        return withContext(Dispatchers.IO) {
            try {
                when (request) {
                    is AiRequest.TextChat -> processChat(request)
                    is AiRequest.VisionChat -> processVisionChat(request)
                }
            } catch (e: Exception) {
                AiResponse.ErrorResponse("Processing failed: ${e.message}")
            }
        }
    }

    private suspend fun processChat(request: AiRequest.TextChat): AiResponse {
        // Reroute check: only process if related to both vehicle and damage
        if (!promptRouter.isMessageRelevant(request.message)) {
            return AiResponse.TextResponse("I'm sorry, I can only assist with queries related to both vehicles and damage. Please try again later with a relevant message.")
        }

        val systemPrompt = promptRouter.getSystemPrompt(request.message)
        val config = buildConversationConfig(systemPrompt, request.history)
        val managed = engine.getOrCreateConversation(request.conversationId, config, systemPrompt)
        val result = managed.conversation.sendMessage(request.message)
        val rawResponse = result.toString()
        val responseText = promptRouter.convertUsdToInr(rawResponse)
        managed.recordExchange(request.message.length, responseText.length)
        return AiResponse.TextResponse(responseText)
    }

    private suspend fun processVisionChat(request: AiRequest.VisionChat): AiResponse {
        val systemPrompt = promptRouter.getSystemPrompt(request.message)
        val config = buildConversationConfig(systemPrompt, request.history)
        val managed = engine.getOrCreateConversation(request.conversationId, config, systemPrompt)
        val message = Contents.of(
            Content.ImageBytes(request.imageBytes),
            Content.Text(request.message)
        )
        val result = managed.conversation.sendMessage(message)
        val rawResponse = result.toString()
        val responseText = promptRouter.convertUsdToInr(rawResponse)
        managed.recordExchange(request.message.length, responseText.length)
        return AiResponse.TextResponse(responseText)
    }

    private fun buildConversationConfig(
        systemPrompt: String,
        history: List<ChatMessage>
    ): ConversationConfig {
        val initialMessages = history.mapNotNull { msg ->
            when (msg.role) {
                ChatMessage.Role.USER -> Message.user(msg.content)
                ChatMessage.Role.ASSISTANT -> Message.model(msg.content)
                ChatMessage.Role.SYSTEM -> null
            }
        }
        return ConversationConfig(
            systemInstruction = Contents.of(systemPrompt),
            initialMessages = initialMessages,
            samplerConfig = SamplerConfig(topK = 64, topP = 0.95, temperature = 1.0)
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "ToyotaCare AI", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("ToyotaCare")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    fun resetChat(conversationId: String) {
        engine.closeConversation(conversationId)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "toyotacare_ai_channel"
    }
}
