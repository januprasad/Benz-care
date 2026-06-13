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

    suspend fun detectDashboard(bitmap: Bitmap): Boolean {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            val text = result.text.uppercase()
            // Keywords often found on dashboards (odometer, gear, warnings)
            val keywords = listOf("KM", "ODO", "TEMP", "TRIP", "RPM", "MPH", "OIL", "BRAKE", "ABS", "CHECK", "ECO", "SPORT", "AUTO", "READY", "OFF")
            val hasDashboardText = keywords.any { text.contains(it) }
            
            // Or if we see a lot of numbers (likely odometer/speed)
            val hasNumbers = text.count { it.isDigit() } > 5
            
            hasDashboardText || hasNumbers
        } catch (e: Exception) {
            false
        }
    }

    private fun extractObdCode(text: String): String? {
        // OBD-II regex: P, B, C, or U followed by 4 digits
        val regex = Regex("[PBCU][0-9]{4}")
        val match = regex.find(text.uppercase())
        return match?.value
    }
}
