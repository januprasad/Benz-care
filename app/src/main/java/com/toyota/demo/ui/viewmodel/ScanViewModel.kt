package com.toyota.demo.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toyota.demo.core.AiProcessor
import com.toyota.demo.core.model.AiRequest
import com.toyota.demo.core.model.AiResponse
import com.toyota.demo.data.db.ScanHistory
import com.toyota.demo.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val aiProcessor: AiProcessor
) : ViewModel() {

    private val _scannedCode = MutableStateFlow<String?>(null)
    val scannedCode: StateFlow<String?> = _scannedCode.asStateFlow()

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    private val _explanation = MutableStateFlow<String?>(null)
    val explanation: StateFlow<String?> = _explanation.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    val scanHistory = scanRepository.allScans

    fun onFrameAnalyzed(bitmap: Bitmap) {
        if (_capturedBitmap.value != null || _isAnalyzing.value || _explanation.value != null) return
        _capturedBitmap.value = bitmap
    }

    fun confirmAnalysis() {
        val bitmap = _capturedBitmap.value ?: return
        _scannedCode.value = "Dashboard Snapshot"
        analyzeDashboard(bitmap)
    }

    private fun analyzeDashboard(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            
            val stream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val imageBytes = stream.toByteArray()

            val prompt = """
                Analyze this car dashboard image. 
                Identify all active warning lights (symbols/indicators) that are illuminated.
                For each identified light, provide:
                1. Symbol Name:
                2. Meaning:
                3. Urgency Level (CRITICAL, CAUTION, or INFORMATION):
                4. Recommended Action:
                5. Potential Repair Cost in INR:

                If no warning lights are visible, state that the dashboard appears clear.
                Respond in a clear, professional, structured format.
            """.trimIndent()

            val request = AiRequest.VisionChat(
                message = prompt,
                imageBytes = imageBytes,
                conversationId = "dash_scan_${System.currentTimeMillis()}",
                history = emptyList()
            )

            val response = aiProcessor.process(request)
            if (response is AiResponse.TextResponse) {
                _explanation.value = response.text
                scanRepository.saveScan(
                    ScanHistory(
                        errorCode = "Dashboard Analysis",
                        explanation = response.text,
                        language = "English"
                    )
                )
            }
            _isAnalyzing.value = false
        }
    }

    fun resetScan() {
        _scannedCode.value = null
        _capturedBitmap.value = null
        _explanation.value = null
    }

    fun deleteScan(scan: ScanHistory) {
        viewModelScope.launch {
            scanRepository.deleteScan(scan)
        }
    }
}
