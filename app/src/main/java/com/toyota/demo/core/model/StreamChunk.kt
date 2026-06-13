package com.toyota.demo.core.model

data class StreamChunk(
    val responseText: String,
    val thinkingText: String? = null,
    val stats: InferenceStats? = null
)
