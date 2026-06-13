package com.toyota.demo.ui.theme

import androidx.compose.ui.graphics.Color

// Pure Luxury Sophisticated Dark Palette
val SophisticatedBackground = Color(0xFF0A0A0B)
val SophisticatedInnerCard = Color(0xFF121214)   // Very dark card interior / footer bg
val SophisticatedInputBg = Color(0xFF1A1A1E)     // Interaction bar background
val SophisticatedUserBubble = Color(0xFF1F1F23)  // User message bubble & side buttons
val SophisticatedAIBubble = Color(0xFF252529)    // AI damage card outer container
val SophisticatedSecondaryButton = Color(0xFF2D2D33) // Muted controls

// Accent Colors
val SophisticatedEmerald = Color(0xFF34D399)     // Emerald-400 theme spotlight highlight
val SophisticatedEmeraldDot = Color(0xFF10B981)  // Emerald-500 status glowing indicator
val SophisticatedTextPrimary = Color(0xFFE1E1E6) // Bright white-silver text
val SophisticatedTextMuted = Color(0xFFD1D1D6)   // Subtle grey text body
val SophisticatedLabelMuted = Color(0x66FFFFFF)  // 40% white subtle uppercase headers
val SophisticatedBorderLight = Color(0x0DFFFFFF) // white/5 border
val SophisticatedBorderMedium = Color(0x1AFFFFFF)// white/10 border

// Base palette compatibility
val ToyotaDeepBlack = SophisticatedBackground
val ToyotaDarkCard = SophisticatedAIBubble
val ToyotaSilver = SophisticatedTextPrimary
val ToyotaMetallicGrey = SophisticatedTextMuted
val ToyotaAccentBlue = SophisticatedEmerald
val ToyotaAlertAmber = Color(0xFFFF9F0A)
val ToyotaMutedBorder = SophisticatedBorderLight

// Light variations fallback
val ToyotaWhiteSurface = Color(0xFFF2F2F7)
val ToyotaLightCard = Color(0xFFFFFFFF)
val ToyotaCharcoalText = Color(0xFF1C1C1E)
val ToyotaClassicBlue = Color(0xFF004080)
