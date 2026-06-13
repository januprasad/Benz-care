package com.toyota.demo.ai

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.toyota.demo.ai.model.ModelDownloadManager
import com.toyota.demo.core.AiProcessor
import com.toyota.demo.core.model.AiRequest
import com.toyota.demo.core.model.AiResponse
import com.toyota.demo.core.model.StreamChunk
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaProcessorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelDownloadManager: ModelDownloadManager
) : AiProcessor {

    private val _service = MutableStateFlow<GemmaService?>(null)
    private val _isEngineReady = MutableStateFlow(false)
    override val isEngineReady: StateFlow<Boolean> = _isEngineReady.asStateFlow()

    private var serviceScope: CoroutineScope? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = (binder as GemmaService.GemmaBinder).getService()
            _service.value = service
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            serviceScope = scope
            scope.launch {
                service.isEngineReady.collect { ready ->
                    _isEngineReady.value = ready
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _service.value = null
            _isEngineReady.value = false
            serviceScope?.cancel()
            serviceScope = null
        }
    }

    init {
        val intent = Intent(context, GemmaService::class.java)
        context.startForegroundService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override suspend fun process(request: AiRequest): AiResponse {
        val service = withTimeoutOrNull(10_000L) {
            _service.filterNotNull().first()
        } ?: return AiResponse.ErrorResponse("Service not available")
        return service.process(request)
    }

    override suspend fun processStreaming(request: AiRequest): Flow<StreamChunk> {
        throw UnsupportedOperationException("Streaming not implemented in basic integration")
    }

    override suspend fun postProcessChat(responseText: String): AiResponse {
        return AiResponse.TextResponse(responseText)
    }

    override suspend fun postProcessVisionChat(responseText: String, userMessage: String): AiResponse {
        return AiResponse.TextResponse(responseText)
    }

    override val isModelAvailable: StateFlow<Boolean> = modelDownloadManager.isModelAvailableFlow
    override val downloadProgress: StateFlow<Float> = modelDownloadManager.downloadProgress
    override val isDownloading: StateFlow<Boolean> = modelDownloadManager.isDownloading
    override val downloadedBytes: StateFlow<Long> = modelDownloadManager.downloadedBytes
    override val totalBytes: StateFlow<Long> = modelDownloadManager.totalBytes

    override suspend fun downloadModel(): Result<Unit> {
        modelDownloadManager.downloadModel()
        return Result.success(Unit)
    }

    override suspend fun deleteModel(): Result<Unit> = modelDownloadManager.deleteModel()
    override fun getModelSizeOnDisk(): Long = modelDownloadManager.getModelSizeOnDisk()
    override fun hasPartialDownload(): Boolean = modelDownloadManager.hasPartialDownload()
    override suspend fun resetChat(conversationId: String) {
        _service.value?.resetChat(conversationId)
    }
    override suspend fun cancelGeneration() {}
}
