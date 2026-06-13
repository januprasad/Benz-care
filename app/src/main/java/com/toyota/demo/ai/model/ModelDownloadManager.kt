package com.toyota.demo.ai.model

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) {
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading

    private val _downloadedBytes = MutableStateFlow(0L)
    val downloadedBytes: StateFlow<Long> = _downloadedBytes

    private val _totalBytes = MutableStateFlow(MODEL_SIZE_BYTES)
    val totalBytes: StateFlow<Long> = _totalBytes

    private val _isModelAvailable = MutableStateFlow(false)
    val isModelAvailableFlow: StateFlow<Boolean> = _isModelAvailable

    private val modelDir: File = File(context.filesDir, "models").also { it.mkdirs() }
    private val modelFile = File(modelDir, MODEL_FILENAME)
    private val tempFile = File(modelDir, "$MODEL_FILENAME.tmp")

    init {
        _isModelAvailable.value = modelFile.exists() && modelFile.length() > 0
    }

    fun isModelAvailable(): Boolean = modelFile.exists() && modelFile.length() > 0
    fun getModelPath(): String = modelFile.absolutePath
    fun getModelSizeOnDisk(): Long = if (modelFile.exists()) modelFile.length() else 0L
    fun hasPartialDownload(): Boolean = tempFile.exists() && tempFile.length() > 0

    suspend fun downloadModel(): Result<File> = withContext(Dispatchers.IO) {
        if (isModelAvailable()) {
            _isModelAvailable.value = true
            return@withContext Result.success(modelFile)
        }
        if (_isDownloading.value) return@withContext Result.failure(Exception("Already downloading"))

        _isDownloading.value = true
        try {
            val existingBytes = if (tempFile.exists()) tempFile.length() else 0L
            val request = Request.Builder().url(MODEL_URL)
                .let { if (existingBytes > 0) it.addHeader("Range", "bytes=$existingBytes-") else it }
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 206) {
                    throw Exception("Download failed: HTTP ${response.code}")
                }
                val body = response.body ?: throw Exception("Empty body")
                val contentLength = body.contentLength()
                val totalSize = if (response.code == 206) existingBytes + contentLength else contentLength
                _totalBytes.value = if (totalSize > 0) totalSize else MODEL_SIZE_BYTES

                var bytesRead = if (response.code == 206) existingBytes else 0L
                body.byteStream().use { input ->
                    FileOutputStream(tempFile, response.code == 206).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            bytesRead += read
                            _downloadedBytes.value = bytesRead
                            _downloadProgress.value = bytesRead.toFloat() / _totalBytes.value
                        }
                    }
                }
            }
            if (!tempFile.renameTo(modelFile)) throw Exception("Rename failed")
            _isModelAvailable.value = true
            Result.success(modelFile)
        } catch (e: Exception) {
            Log.e("ModelDownload", "Failed", e)
            Result.failure(e)
        } finally {
            _isDownloading.value = false
        }
    }

    suspend fun deleteModel(): Result<Unit> = withContext(Dispatchers.IO) {
        tempFile.delete()
        if (modelFile.exists() && modelFile.delete()) {
            _isModelAvailable.value = false
            Result.success(Unit)
        } else Result.success(Unit)
    }

    companion object {
        private const val MODEL_FILENAME = "gemma-4-E2B-it.litertlm"
        const val MODEL_SIZE_BYTES = 2_770_000_000L
        const val MODEL_URL = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
    }
}
