package com.toyota.demo.analysis

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint

data class ProcessedImage(
    val bitmap: Bitmap,
    val wasEnhanced: Boolean,
    val originalSize: Pair<Int, Int>,
    val processedSize: Pair<Int, Int>
)

object ImagePreprocessor {
    
    fun process(raw: Bitmap, rotationDegrees: Int): ProcessedImage {
        val originalSize = raw.width to raw.height
        
        // 1. Rotate
        var bmp = rotate(raw, rotationDegrees)
        
        // 2. Crop to centre (dashboard lights are usually in the middle)
        bmp = cropCentre(bmp, 0.7f)
        
        // 3. Resize to optimal resolution for Vision/OCR
        bmp = resizeToTarget(bmp, 1280)
        
        // 4. Contrast enhancement
        val enhancedBmp = toHighContrastGrayscale(bmp)
        
        return ProcessedImage(
            bitmap = enhancedBmp,
            wasEnhanced = true,
            originalSize = originalSize,
            processedSize = enhancedBmp.width to enhancedBmp.height
        )
    }

    private fun rotate(bmp: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bmp
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    }

    private fun cropCentre(bmp: Bitmap, fraction: Float): Bitmap {
        val width = bmp.width
        val height = bmp.height
        val newWidth = (width * fraction).toInt()
        val newHeight = (height * fraction).toInt()
        val left = (width - newWidth) / 2
        val top = (height - newHeight) / 2
        return Bitmap.createBitmap(bmp, left, top, newWidth, newHeight)
    }

    private fun toHighContrastGrayscale(bmp: Bitmap): Bitmap {
        val width = bmp.width
        val height = bmp.height
        val dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        val paint = Paint()
        
        val cm = ColorMatrix().apply {
            setSaturation(0f) // Grayscale
            // Increase contrast: scale 1.5, translate -50
            val m = arrayListOf(
                1.5f, 0f, 0f, 0f, -50f,
                0f, 1.5f, 0f, 0f, -50f,
                0f, 0f, 1.5f, 0f, -50f,
                0f, 0f, 0f, 1f, 0f
            ).toFloatArray()
            postConcat(ColorMatrix(m))
        }
        
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bmp, 0f, 0f, paint)
        return dest
    }

    private fun resizeToTarget(bmp: Bitmap, maxW: Int): Bitmap {
        if (bmp.width <= maxW) return bmp
        val ratio = maxW.toFloat() / bmp.width
        val targetH = (bmp.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bmp, maxW, targetH, true)
    }
}
