package com.toyota.demo.ai.prompts

import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptRouter @Inject constructor() {
    fun getSystemPrompt(userMessage: String = ""): String {
        if (userMessage.lowercase().contains("accident report")) {
            return PromptTemplates.buildAccidentReportPrompt()
        }
        return PromptTemplates.buildSystemPrompt()
    }

    /**
     * Converts USD amounts in text to INR.
     * Uses an approximate exchange rate of 1 USD = 83 INR.
     */
    fun convertUsdToInr(text: String): String {
        val usdRegex = Regex("\\$\\s?(\\d{1,3}(,\\d{3})*(\\.\\d+)?)\\b")
        return usdRegex.replace(text) { matchResult ->
            val usdValueStr = matchResult.groupValues[1].replace(",", "")
            val usdValue = usdValueStr.toDoubleOrNull()
            if (usdValue != null) {
                val inrValue = usdValue * 83
                val formattedInr = String.format(Locale.US, "%,.0f", inrValue)
                "₹$formattedInr"
            } else {
                matchResult.value
            }
        }
    }

    /**
     * Checks if the message is related to both vehicle and damage.
     */
    fun isMessageRelevant(message: String): Boolean {
        val lower = message.lowercase()
        
        // Always relevant if asking for an accident report or repair analysis
        if (lower.contains("accident report") || lower.contains("repair analysis")) return true
        
        // Comprehensive vehicle parts and types
        val vehicleKeywords = listOf(
            "vehicle", "car", "truck", "suv", "toyota", "auto", "motor", "sedan",
            "bumper", "fender", "hood", "door", "mirror", "glass", "windshield",
            "tire", "wheel", "light", "headlight", "tail light", "trunk", "roof"
        )
        
        // Damage and repair related terms
        val damageKeywords = listOf(
            "damage", "dent", "scratch", "crack", "broken", "accident", "crash",
            "collision", "repair", "fix", "estimate", "paint", "smashed", "hit", "bent"
        )
        
        val hasVehicle = vehicleKeywords.any { lower.contains(it) }
        val hasDamage = damageKeywords.any { lower.contains(it) }
        
        // If it's a simple greeting or completely unrelated, it's not relevant
        if (lower.length < 3) return false
        
        return hasVehicle && hasDamage
    }
}
