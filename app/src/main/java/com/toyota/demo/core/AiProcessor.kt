package com.toyota.demo.core

import com.toyota.demo.core.model.AiRequest
import com.toyota.demo.core.model.AiResponse
import com.toyota.demo.core.model.StreamChunk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AiProcessor {
    suspend fun process(request: AiRequest): AiResponse
    suspend fun processStreaming(request: AiRequest): Flow<StreamChunk>
    suspend fun postProcessChat(responseText: String): AiResponse
    suspend fun postProcessVisionChat(responseText: String, userMessage: String): AiResponse

    val isModelAvailable: StateFlow<Boolean>
    val isEngineReady: StateFlow<Boolean>
    val downloadProgress: StateFlow<Float>
    val isDownloading: StateFlow<Boolean>
    val downloadedBytes: StateFlow<Long>
    val totalBytes: StateFlow<Long>

    suspend fun downloadModel(): Result<Unit>
    suspend fun deleteModel(): Result<Unit>
    fun getModelSizeOnDisk(): Long
    fun hasPartialDownload(): Boolean
    suspend fun resetChat(conversationId: String)
    suspend fun cancelGeneration()
}
