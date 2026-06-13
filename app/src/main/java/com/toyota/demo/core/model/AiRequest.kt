package com.toyota.demo.core.model

sealed class AiRequest {
    data class TextChat(
        val message: String,
        val conversationId: String,
        val history: List<ChatMessage> = emptyList()
    ) : AiRequest()

    data class VisionChat(
        val message: String,
        val imageBytes: ByteArray,
        val conversationId: String,
        val history: List<ChatMessage> = emptyList()
    ) : AiRequest()
}
