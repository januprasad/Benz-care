package com.toyota.demo.core.model

sealed class AiResponse {
    data class TextResponse(val text: String) : AiResponse()
    data class ErrorResponse(val message: String, val cause: Throwable? = null) : AiResponse()
    // Reduced for brevity as user mainly wants vision chat
}
