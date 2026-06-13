package com.toyota.demo.core.model

data class ChatMessage(
    val id: String,
    val role: Role,
    val content: String,
    val imageBytes: ByteArray? = null,
    val thinkingContent: String? = null,
    val stats: InferenceStats? = null
) {
    enum class Role { USER, ASSISTANT, SYSTEM }
}
