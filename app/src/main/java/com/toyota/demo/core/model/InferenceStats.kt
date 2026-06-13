package com.toyota.demo.core.model

data class InferenceStats(
    val tokensPerSecond: Float,
    val timeToFirstTokenMs: Long,
    val totalTokens: Int,
    val totalTimeMs: Long,
    val backend: String
)
