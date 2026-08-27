package com.mitsara.arrowescape.model

import androidx.compose.ui.graphics.Color

enum class GameTheme(
    val id: String,
    val displayName: String,
    val isPremiumOnly: Boolean,
    val boardCanvasColor: Color,
    val gridDotColor: Color,
    val arrowNormalColor: Color,
    val arrowHighlightColor: Color,
    val textPrimaryColor: Color,
    val surfaceBackgroundColor: Color
) {
    LIGHT(
        id = "LIGHT",
        displayName = "Classic Light",
        isPremiumOnly = false,
        boardCanvasColor = Color(0xFFFFFFFF),
        gridDotColor = Color(0xFFCBD5E1),
        arrowNormalColor = Color(0xFF0F172A),
        arrowHighlightColor = Color(0xFF0EA5E9),
        textPrimaryColor = Color(0xFF0F172A),
        surfaceBackgroundColor = Color(0xFFF8FAFC)
    ),
    DARK(
        id = "DARK",
        displayName = "Midnight Dark",
        isPremiumOnly = true,
        boardCanvasColor = Color(0xFF1E293B),
        gridDotColor = Color(0xFF475569),
        arrowNormalColor = Color(0xFFF8FAFC),
        arrowHighlightColor = Color(0xFF38BDF8),
        textPrimaryColor = Color(0xFFF8FAFC),
        surfaceBackgroundColor = Color(0xFF0F172A)
    ),
    CYBER(
        id = "CYBER",
        displayName = "Cyber Neon",
        isPremiumOnly = true,
        boardCanvasColor = Color(0xFF130924),
        gridDotColor = Color(0xFF4C1D95),
        arrowNormalColor = Color(0xFF00F0FF),
        arrowHighlightColor = Color(0xFFFF007F),
        textPrimaryColor = Color(0xFFF0FDF4),
        surfaceBackgroundColor = Color(0xFF0D041A)
    ),
    WOOD(
        id = "WOOD",
        displayName = "Warm Wood",
        isPremiumOnly = true,
        boardCanvasColor = Color(0xFFFDF6E3),
        gridDotColor = Color(0xFFD6C5A0),
        arrowNormalColor = Color(0xFF4A2E1A),
        arrowHighlightColor = Color(0xFFD97706),
        textPrimaryColor = Color(0xFF291D0F),
        surfaceBackgroundColor = Color(0xFFF5EBE0)
    );

    companion object {
        fun fromId(id: String): GameTheme {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: LIGHT
        }
    }
}
