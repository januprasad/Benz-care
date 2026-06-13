package com.toyota.demo.camera

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OcrEngine {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanImage(bitmap: Bitmap): String? {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            extractObdCode(result.text)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractObdCode(text: String): String? {
        // OBD-II regex: P, B, C, or U followed by 4 digits
        val regex = Regex("[PBCU][0-9]{4}")
        val match = regex.find(text.uppercase())
        return match?.value
    }
}
