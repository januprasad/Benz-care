package com.toyota.demo.analysis

import com.toyota.demo.core.AiProcessor
import com.toyota.demo.core.model.AiRequest
import com.toyota.demo.core.model.AiResponse
import javax.inject.Inject
import javax.inject.Singleton

// ─────────────────────────────────────────────────────────────────
// Severity
// ─────────────────────────────────────────────────────────────────

enum class Severity { HIGH, MEDIUM, LOW, NONE, UNKNOWN;
    companion object {
        fun from(value: String) = entries.find { it.name == value.uppercase() } ?: UNKNOWN
    }
}

// ─────────────────────────────────────────────────────────────────
// SafeToDriver
// ─────────────────────────────────────────────────────────────────

enum class SafeToDriver {
    YES, NO, CONDITIONALLY;

    companion object {
        fun from(value: String): SafeToDriver = when (value.uppercase().trim()) {
            "YES"           -> YES
            "NO"            -> NO
            "CONDITIONALLY" -> CONDITIONALLY
            else            -> CONDITIONALLY
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Urgency
// ─────────────────────────────────────────────────────────────────

enum class Urgency {
    IMMEDIATE,      
    THIS_WEEK,      
    NEXT_SERVICE,   
    NO_ACTION;      

    companion object {
        fun from(value: String): Urgency = entries.find { it.name == value.uppercase().trim() } ?: THIS_WEEK
    }
}

// ─────────────────────────────────────────────────────────────────
// CodeSystem
// ─────────────────────────────────────────────────────────────────

enum class CodeSystem { POWERTRAIN, BODY, CHASSIS, NETWORK, UNKNOWN;
    companion object {
        fun from(value: String) = entries.find { it.name == value.uppercase() } ?: UNKNOWN
    }
}

// ─────────────────────────────────────────────────────────────────
// ErrorAnalysis
// ─────────────────────────────────────────────────────────────────

data class DetectedLight(
    val name: String,
    val color: String
)

data class ErrorAnalysis(
    val rawCode: String = "",
    val system: CodeSystem,
    val severity: Severity,
    val title: String,
    val plainExplanation: String,
    val likelyCauses: List<String>,
    val driverAction: String,
    val safeToDriver: SafeToDriver,
    val urgency: Urgency,
    val estimatedCostRange: String,
    val detectedLights: List<DetectedLight> = emptyList()
) {
    val safeToDrive: Boolean get() = safeToDriver == SafeToDriver.YES
}

// ─────────────────────────────────────────────────────────────────
// GemmaResult
// ─────────────────────────────────────────────────────────────────

sealed class GemmaResult {
    data class Success(val analysis: ErrorAnalysis) : GemmaResult()
    data class Error(val message: String) : GemmaResult()
}

// ─────────────────────────────────────────────────────────────────
// GemmaAnalyser
// ─────────────────────────────────────────────────────────────────

@Singleton
class GemmaAnalyser @Inject constructor(
    private val aiProcessor: AiProcessor
) {

    fun buildPrompt(
        language: String = "English",
        vehicleHint: String? = null
    ): String = """
        You are a car mechanic assistant for Indian drivers understanding dashboard warning lights.
        ${vehicleHint?.let { "The car is: $it" } ?: ""}

        Analyze the illuminated symbols in this dashboard image.
        
        Use the following technical reference for specific lights:
        - Check Engine: engine management/emissions problem. If blinking, check immediately.
        - Battery/Alternator: not charging. electrical systems may fail. safe for a few miles to garage.
        - Low Oil Pressure: internal engine risk. STOP IMMEDIATELY.
        - Temperature: overheating. check coolant/fan. don't drive long distances.
        - Brake: parking brake on, system fault, or low fluid.
        - ABS: system fault. braking remains manual. check asap.
        - Low Fuel: orange/red indicator. approx 20-30km range left.
        - Door Ajar: door or boot not closed properly.
        - Power Steering: component fault or low fluid. steering will be heavy.
        - Powertrain Malfunction: detected fault. SHUT DOWN engine immediately.
        - Hazard Light: illuminated with other lights or error messages.
        - Airbag: fault in SRS system. professional repair required.
        - Washer Fluid: low reservoir level. windshield symbol cap.
        - TPMS: low tyre pressure in one or more wheels.
        - Traction Control: system active or slippery conditions.
        - High Beam: active high beam headlights.
        - Parking Sensor: system off or sensor fault.
        - Seat Belt: remains active/flashing until belt is fastened.
        - Cruise Control: system is activated.
        - Key not in vehicle: missing key, prevents ignition.
        - Green check (✓): car self-diagnostic PASSED — all systems healthy.

        Respond ONLY in $language using EXACTLY this XML format. No extra text:

        <title>Primary warning name — max 6 words</title>
        <severity>HIGH or MEDIUM or LOW or NONE</severity>
        <safe_to_drive>YES or NO or CONDITIONALLY</safe_to_drive>
        <system>POWERTRAIN or BODY or CHASSIS or NETWORK</system>
        <explanation>2 sentences in simple language. India-specific context if relevant.</explanation>
        <detected_lights>
        - Light Name: Color (e.g., Check Engine: RED)
        - Light Name: Color
        </detected_lights>
        <causes>
        - Most likely cause
        - Second cause
        - Third cause
        </causes>
        <action>One sentence: what the driver should do RIGHT NOW.</action>
        <cost>₹X,XXX – ₹X,XXX including labour. Say FREE if no repair needed.</cost>
        <urgency>IMMEDIATE or THIS_WEEK or NEXT_SERVICE or NO_ACTION</urgency>
    """.trimIndent()

    suspend fun analyse(
        imageBytes: ByteArray,
        language: String = "English",
        vehicleHint: String? = null
    ): GemmaResult {
        val prompt = buildPrompt(language, vehicleHint)
        val request = AiRequest.VisionChat(
            message = prompt,
            imageBytes = imageBytes,
            conversationId = "dash_analysis_${System.currentTimeMillis()}",
            history = emptyList()
        )

        return when (val response = aiProcessor.process(request)) {
            is AiResponse.TextResponse -> parseResponse(response.text)
            is AiResponse.ErrorResponse -> GemmaResult.Error(response.message)
        }
    }

    private fun parseResponse(raw: String): GemmaResult {
        fun tag(name: String) = Regex(
            "<$name>\\s*(.*?)\\s*</$name>", RegexOption.DOT_MATCHES_ALL
        ).find(raw)?.groupValues?.get(1)?.trim() ?: ""

        val causes = tag("causes")
            .lines()
            .map { it.trimStart('-', ' ', '•').trim() }
            .filter { it.isNotBlank() }

        val detectedLights = tag("detected_lights")
            .lines()
            .map { it.trimStart('-', ' ', '•').trim() }
            .filter { it.contains(":") }
            .map { line ->
                val parts = line.split(":", limit = 2)
                DetectedLight(parts[0].trim(), parts[1].trim())
            }

        val analysis = ErrorAnalysis(
            title              = tag("title").ifBlank { "Dashboard Analysis" },
            severity           = Severity.from(tag("severity")),
            safeToDriver       = SafeToDriver.from(tag("safe_to_drive")),
            system             = CodeSystem.from(tag("system")),
            plainExplanation   = tag("explanation").ifBlank { raw.take(200) },
            likelyCauses       = causes.ifEmpty { listOf("Consult authorized service center") },
            driverAction       = tag("action").ifBlank { "Check user manual or visit a mechanic." },
            estimatedCostRange = tag("cost").ifBlank { "₹500 – ₹5,000 (estimate)" },
            urgency            = Urgency.from(tag("urgency")),
            detectedLights     = detectedLights
        )
        return GemmaResult.Success(analysis)
    }
}
