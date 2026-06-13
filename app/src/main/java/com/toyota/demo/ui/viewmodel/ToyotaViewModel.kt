package com.toyota.demo.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toyota.demo.data.db.ChatMessage
import com.toyota.demo.data.db.ChatSession
import com.toyota.demo.data.repository.ToyotaRepository
import com.toyota.demo.core.AiProcessor
import com.toyota.demo.core.model.AiRequest
import com.toyota.demo.core.model.AiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ToyotaViewModel @Inject constructor(
    private val repository: ToyotaRepository,
    private val aiProcessor: AiProcessor
) : ViewModel() {

    val allSessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSessionId = MutableStateFlow<Long?>(null)
    val activeSessionId: StateFlow<Long?> = _activeSessionId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<ChatMessage>> = _activeSessionId
        .flatMapLatest { id -> if (id != null) repository.getMessages(id) else flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _selectedImageBase64 = MutableStateFlow<String?>(null)
    val selectedImageBase64: StateFlow<String?> = _selectedImageBase64.asStateFlow()

    val isModelAvailable: StateFlow<Boolean> = aiProcessor.isModelAvailable

    fun selectSession(sessionId: Long?) { _activeSessionId.value = sessionId }
    fun deleteSession(sessionId: Long) { viewModelScope.launch { repository.deleteSession(sessionId); if (_activeSessionId.value == sessionId) _activeSessionId.value = null } }
    fun clearError() { _errorMessage.value = null }
    fun setImage(uri: Uri?, base64: String?) { _selectedImageUri.value = uri; _selectedImageBase64.value = base64 }

    fun handleSelectedImageUri(context: Context, uri: Uri) {
        _selectedImageUri.value = uri
        // In this simplified version, we just store the URI and read bytes when sending
    }

    fun sendMessage(userText: String, context: Context) {
        if (userText.trim().isEmpty() && _selectedImageBase64.value == null && _selectedImageUri.value == null) return

        if (!isModelAvailable.value) {
            viewModelScope.launch {
                var sessionId = _activeSessionId.value
                if (sessionId == null) {
                    sessionId = repository.createSession("New Appraisal")
                    _activeSessionId.value = sessionId
                }
                repository.addMessage(ChatMessage(sessionId = sessionId, isUser = true, messageText = userText.trim()))
                repository.addMessage(ChatMessage(sessionId = sessionId, isUser = false, messageText = "The AI model is not yet downloaded. Please go to the Settings tab to download the Gemma model."))
            }
            return
        }

        val textPayload = userText.trim()
        val imageBase64 = _selectedImageBase64.value
        val imageUri = _selectedImageUri.value

        _selectedImageUri.value = null
        _selectedImageBase64.value = null

        viewModelScope.launch {
            var sessionId = _activeSessionId.value
            if (sessionId == null) {
                sessionId = repository.createSession(if (textPayload.isNotEmpty()) textPayload.take(20) else "New Appraisal")
                _activeSessionId.value = sessionId
            }

            repository.addMessage(ChatMessage(sessionId = sessionId, isUser = true, messageText = textPayload, imageUri = imageUri?.toString() ?: imageBase64))

            _isGenerating.value = true
            try {
                val history = activeMessages.value.map { msg ->
                    com.toyota.demo.core.model.ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = if (msg.isUser) com.toyota.demo.core.model.ChatMessage.Role.USER else com.toyota.demo.core.model.ChatMessage.Role.ASSISTANT,
                        content = msg.messageText
                    )
                }

                val imageBytes = when {
                    imageBase64 != null -> Base64.decode(imageBase64, Base64.DEFAULT)
                    imageUri != null -> context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                    else -> null
                }

                val request = if (imageBytes != null) {
                    AiRequest.VisionChat(message = textPayload.ifEmpty { "Analyze this damage" }, imageBytes = imageBytes, conversationId = sessionId.toString(), history = history)
                } else {
                    AiRequest.TextChat(message = textPayload, conversationId = sessionId.toString(), history = history)
                }

                val response = aiProcessor.process(request)
                if (response is AiResponse.TextResponse) {
                    val json = extractJson(response.text)
                    repository.addMessage(ChatMessage(
                        sessionId = sessionId, 
                        isUser = false, 
                        messageText = response.text, 
                        estimateJson = json,
                        imageUri = imageUri?.toString() ?: imageBase64 // Associate the image with the report
                    ))
                } else if (response is AiResponse.ErrorResponse) {
                    repository.addMessage(ChatMessage(sessionId = sessionId, isUser = false, messageText = "System Error: ${response.message}"))
                }
            } catch (e: Exception) {
                repository.addMessage(ChatMessage(sessionId = sessionId, isUser = false, messageText = "Connection Error: ${e.message}"))
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun extractJson(text: String): String? {
        val start = text.indexOf("{")
        val end = text.lastIndexOf("}")
        return if (start != -1 && end != -1 && end > start) text.substring(start, end + 1) else null
    }
}
