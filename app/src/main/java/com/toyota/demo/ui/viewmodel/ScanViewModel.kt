package com.toyota.demo.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toyota.demo.analysis.*
import com.toyota.demo.camera.OcrEngine
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
    private val pipeline: AnalysisPipeline
) : ViewModel() {

    private val ocrEngine = OcrEngine()

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    private val _explanation = MutableStateFlow<String?>(null)
    val explanation: StateFlow<String?> = _explanation.asStateFlow()

    private val _analysisResult = MutableStateFlow<ErrorAnalysis?>(null)
    val analysisResult: StateFlow<ErrorAnalysis?> = _analysisResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _pipelineState = MutableStateFlow<PipelineState>(PipelineState.Idle)
    val pipelineState: StateFlow<PipelineState> = _pipelineState.asStateFlow()

    val scanHistory = scanRepository.allScans

    private var isDetectionRunning = false
    private var lastAnalyzedTime = 0L

    fun onFrameAnalyzed(bitmap: Bitmap) {
        if (_capturedBitmap.value != null || _isAnalyzing.value || _analysisResult.value != null || isDetectionRunning) return

        val now = System.currentTimeMillis()
        if (now - lastAnalyzedTime < 500) return 

        lastAnalyzedTime = now
        isDetectionRunning = true
        viewModelScope.launch {
            try {
                if (ocrEngine.detectDashboard(bitmap)) {
                    _capturedBitmap.value = bitmap
                }
            } catch (e: Exception) {
                android.util.Log.e("ScanViewModel", "Detection failed", e)
            } finally {
                isDetectionRunning = false
            }
        }
    }

    fun confirmAnalysis() {
        val bitmap = _capturedBitmap.value ?: return

        viewModelScope.launch {
            _isAnalyzing.value = true
            pipeline.run(
                bitmap = bitmap,
                rotationDegrees = 0,
                onState = { state ->
                    _pipelineState.value = state
                    when (state) {
                        is PipelineState.Done -> {
                            _isAnalyzing.value = false
                            _analysisResult.value = state.result
                            _explanation.value = formatAnalysisToText(state.result)
                            saveToHistory(state.result)
                        }
                        is PipelineState.Failed -> {
                            _isAnalyzing.value = false
                            _explanation.value = "Analysis failed: ${state.reason}"
                        }
                        else -> {}
                    }
                }
            )
        }
    }

    private fun formatAnalysisToText(analysis: ErrorAnalysis): String {
        val safeLabel = when (analysis.safeToDriver) {
            SafeToDriver.YES           -> "Yes"
            SafeToDriver.NO            -> "No — stop immediately"
            SafeToDriver.CONDITIONALLY -> "Short trips only"
        }

        val urgencyLabel = when (analysis.urgency) {
            Urgency.IMMEDIATE    -> "🔴 Immediate — stop now"
            Urgency.THIS_WEEK    -> "🟠 This week — book mechanic"
            Urgency.NEXT_SERVICE -> "🟡 Next service"
            Urgency.NO_ACTION    -> "🟢 No action needed"
        }

        return """
            ${analysis.title}
            
            Detected Lights (${analysis.detectedLights.size}):
            ${analysis.detectedLights.joinToString("\n") { "• ${it.name}: ${it.color}" }}
            
            Explanation: ${analysis.plainExplanation}
            
            Likely Causes:
            ${analysis.likelyCauses.joinToString("\n") { "• $it" }}
            
            Severity: ${analysis.severity}
            Safe to Drive: $safeLabel
            Urgency: $urgencyLabel
            
            Action: ${analysis.driverAction}
            Estimated Cost: ${analysis.estimatedCostRange}
        """.trimIndent()
    }

    private fun saveToHistory(analysis: ErrorAnalysis) {
        viewModelScope.launch {
            scanRepository.saveScan(
                ScanHistory(
                    errorCode   = analysis.title,
                    explanation = formatAnalysisToText(analysis),
                    language    = "English"
                )
            )
        }
    }

    fun resetScan() {
        _capturedBitmap.value  = null
        _analysisResult.value  = null
        _explanation.value     = null
        _isAnalyzing.value     = false
        _pipelineState.value   = PipelineState.Idle
    }

    fun deleteScan(scan: ScanHistory) {
        viewModelScope.launch {
            scanRepository.deleteScan(scan)
        }
    }

    fun forceCapture(bitmap: Bitmap) {
        if (_capturedBitmap.value == null) {
            _capturedBitmap.value = bitmap
        }
    }
}
