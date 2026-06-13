package com.toyota.demo.analysis

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed class PipelineState {
    object Idle : PipelineState()
    object Preprocessing : PipelineState()
    object Analysing : PipelineState()
    data class Done(val result: ErrorAnalysis) : PipelineState()
    data class Failed(val reason: String) : PipelineState()
}

@Singleton
class AnalysisPipeline @Inject constructor(
    private val gemmaAnalyser: GemmaAnalyser
) {
    suspend fun run(
        bitmap: Bitmap,
        rotationDegrees: Int,
        language: String = "English",
        vehicleHint: String? = null,
        onState: (PipelineState) -> Unit
    ) {
        try {
            // 1. Preprocess
            onState(PipelineState.Preprocessing)
            val processed = ImagePreprocessor.process(bitmap, rotationDegrees)
            
            // 2. Convert to bytes for vision model
            val stream = ByteArrayOutputStream()
            processed.bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val imageBytes = stream.toByteArray()

            // 3. Gemma analysis
            onState(PipelineState.Analysing)
            val result = gemmaAnalyser.analyse(imageBytes, language, vehicleHint)

            when (result) {
                is GemmaResult.Success -> onState(PipelineState.Done(result.analysis))
                is GemmaResult.Error -> onState(PipelineState.Failed(result.message))
            }
        } catch (e: Exception) {
            onState(PipelineState.Failed(e.message ?: "Unknown error during analysis"))
        }
    }
}
