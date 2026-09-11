package com.mitsara.arrowescape.model

import androidx.compose.ui.graphics.Color

data class GameTheme(
    val id: String,
    val displayName: String,
    val consoleStyleName: String,
    val isPremiumOnly: Boolean,
    val palette: List<Color>,
    val gridStyle: String,
    val arrowStyle: String,
    val backgroundShaderName: String,
    val animationSpeedMs: Int,
    val boardCanvasColor: Color,
    val gridDotColor: Color,
    val arrowNormalColor: Color,
    val arrowHighlightColor: Color,
    val textPrimaryColor: Color,
    val surfaceBackgroundColor: Color
)

object ThemeManager {
    val EYE_COMFORT = GameTheme(
        id = "EYE_COMFORT",
        displayName = "Eye Comfort Parchment",
        consoleStyleName = "Warm Sepia Ink",
        isPremiumOnly = false,
        palette = listOf(Color(0xFF4A3525), Color(0xFF3E2E23), Color(0xFF5C4033), Color(0xFF2C2018), Color(0xFF6B4423)),
        gridStyle = "Subtle Tan Dot Matrix",
        arrowStyle = "Clean Ink Contour",
        backgroundShaderName = "Soft Parchment Paper",
        animationSpeedMs = 180,
        boardCanvasColor = Color(0xFFFAF7EE),
        gridDotColor = Color(0xFFE2D6C0),
        arrowNormalColor = Color(0xFF4A3525),
        arrowHighlightColor = Color(0xFF00A8FF),
        textPrimaryColor = Color(0xFFC5953C),
        surfaceBackgroundColor = Color(0xFFFAF7EE)
    )

    val MINIMAL_WHITE = GameTheme(
        id = "MINIMAL_WHITE",
        displayName = "Minimal Studio White",
        consoleStyleName = "Crisp Dark Navy Ink",
        isPremiumOnly = false,
        palette = listOf(Color(0xFF1E2238), Color(0xFF181A2A), Color(0xFF2D2B3D), Color(0xFF1A1D2E), Color(0xFF0F172A)),
        gridStyle = "Clean Slate Dot Matrix",
        arrowStyle = "Solid Navy Stroke",
        backgroundShaderName = "Crisp White Studio",
        animationSpeedMs = 180,
        boardCanvasColor = Color(0xFFFFFFFF),
        gridDotColor = Color(0xFFE2E8F0),
        arrowNormalColor = Color(0xFF1E2238),
        arrowHighlightColor = Color(0xFF0284C7),
        textPrimaryColor = Color(0xFF1E293B),
        surfaceBackgroundColor = Color(0xFFFFFFFF)
    )

    val ZEN_WOOD = GameTheme(
        id = "ZEN_WOOD",
        displayName = "Zen Cedar Wood",
        consoleStyleName = "Warm Earth Ink",
        isPremiumOnly = false,
        palette = listOf(Color(0xFF3E2723), Color(0xFF4E342E), Color(0xFF5D4037), Color(0xFF271C19), Color(0xFF795548)),
        gridStyle = "Soft Woodgrain Matrix",
        arrowStyle = "Solid Walnut Contour",
        backgroundShaderName = "Natural Bamboo Paper",
        animationSpeedMs = 180,
        boardCanvasColor = Color(0xFFF5EBE1),
        gridDotColor = Color(0xFFD7CCC8),
        arrowNormalColor = Color(0xFF3E2723),
        arrowHighlightColor = Color(0xFF10B981),
        textPrimaryColor = Color(0xFF5D4037),
        surfaceBackgroundColor = Color(0xFFF5EBE1)
    )

    val DARK_SLATE = GameTheme(
        id = "DARK_SLATE",
        displayName = "Dark Slate Minimal",
        consoleStyleName = "Chalk Stroke Minimal",
        isPremiumOnly = false,
        palette = listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFF94A3B8), Color(0xFF64748B)),
        gridStyle = "Subtle Dark Matrix",
        arrowStyle = "Crisp Chalk Contour",
        backgroundShaderName = "Deep Slate Canvas",
        animationSpeedMs = 180,
        boardCanvasColor = Color(0xFF0F172A),
        gridDotColor = Color(0xFF334155),
        arrowNormalColor = Color(0xFFF8FAFC),
        arrowHighlightColor = Color(0xFF38BDF8),
        textPrimaryColor = Color(0xFFF8FAFC),
        surfaceBackgroundColor = Color(0xFF0F172A)
    )

    val SOFT_MINT = GameTheme(
        id = "SOFT_MINT",
        displayName = "Soft Sage Mint",
        consoleStyleName = "Forest Ink Minimal",
        isPremiumOnly = false,
        palette = listOf(Color(0xFF134E4A), Color(0xFF042F2E), Color(0xFF115E59), Color(0xFF0F766E), Color(0xFF14B8A6)),
        gridStyle = "Subtle Mint Matrix",
        arrowStyle = "Deep Sage Contour",
        backgroundShaderName = "Soft Sage Paper",
        animationSpeedMs = 180,
        boardCanvasColor = Color(0xFFF0FDF4),
        gridDotColor = Color(0xFFDCFCE7),
        arrowNormalColor = Color(0xFF134E4A),
        arrowHighlightColor = Color(0xFF059669),
        textPrimaryColor = Color(0xFF166534),
        surfaceBackgroundColor = Color(0xFFF0FDF4)
    )

    val RETRO_ARCADE = GameTheme(
        id = "RETRO_ARCADE",
        displayName = "8-Bit Arcade Console",
        consoleStyleName = "Retro Pixel Cabinet",
        isPremiumOnly = false,
        palette = listOf(Color(0xFFFF3366), Color(0xFFFFB000), Color(0xFF00FF99), Color(0xFF00F0FF), Color(0xFF8B5CF6)),
        gridStyle = "Pixel Dot Matrix",
        arrowStyle = "Vector 8-Bit Chevron",
        backgroundShaderName = "CRT Scanlines",
        animationSpeedMs = 200,
        boardCanvasColor = Color(0xFF14141F),
        gridDotColor = Color(0xFFFFB000),
        arrowNormalColor = Color(0xFFFF3366),
        arrowHighlightColor = Color(0xFFFFEE55),
        textPrimaryColor = Color(0xFFFFB000),
        surfaceBackgroundColor = Color(0xFF07070B)
    )

    val CYBER_TERMINAL = GameTheme(
        id = "CYBER_TERMINAL",
        displayName = "Cyberpunk Hologram",
        consoleStyleName = "Neural Matrix Deck",
        isPremiumOnly = true,
        palette = listOf(Color(0xFF00FF99), Color(0xFF00F0FF), Color(0xFFFF007F), Color(0xFFFBBF24), Color(0xFF818CF8)),
        gridStyle = "Laser Grid Matrix",
        arrowStyle = "Neon Plasma Dart",
        backgroundShaderName = "Data Stream Matrix",
        animationSpeedMs = 150,
        boardCanvasColor = Color(0xFF0A0F1D),
        gridDotColor = Color(0xFF00F0FF),
        arrowNormalColor = Color(0xFF00FF99),
        arrowHighlightColor = Color(0xFFFF007F),
        textPrimaryColor = Color(0xFF00F0FF),
        surfaceBackgroundColor = Color(0xFF020408)
    )

    val VAPORWAVE = GameTheme(
        id = "VAPORWAVE",
        displayName = "Synthwave Neo-Memphis",
        consoleStyleName = "Neon Synth Deck",
        isPremiumOnly = true,
        palette = listOf(Color(0xFF01CDFE), Color(0xFFFF71CE), Color(0xFF05FFA1), Color(0xFFB967FF), Color(0xFFFFFB00)),
        gridStyle = "Perspective Horizon Grid",
        arrowStyle = "Retro Neon Shard",
        backgroundShaderName = "Sunset Horizon Sun",
        animationSpeedMs = 180,
        boardCanvasColor = Color(0xFF2B1C3F),
        gridDotColor = Color(0xFFFF71CE),
        arrowNormalColor = Color(0xFF01CDFE),
        arrowHighlightColor = Color(0xFFFFFB00),
        textPrimaryColor = Color(0xFFFF71CE),
        surfaceBackgroundColor = Color(0xFF180F24)
    )

    val QUANTUM_NEBULA = GameTheme(
        id = "QUANTUM_NEBULA",
        displayName = "Quantum Nebula",
        consoleStyleName = "Starlight Core",
        isPremiumOnly = true,
        palette = listOf(Color(0xFF818CF8), Color(0xFFFBBF24), Color(0xFFEC4899), Color(0xFF34D399), Color(0xFF60A5FA)),
        gridStyle = "Cosmic Starfield Grid",
        arrowStyle = "Crystalline Prism Dart",
        backgroundShaderName = "Swirling Nebula Dust",
        animationSpeedMs = 220,
        boardCanvasColor = Color(0xFF11102B),
        gridDotColor = Color(0xFFA5B4FC),
        arrowNormalColor = Color(0xFF818CF8),
        arrowHighlightColor = Color(0xFFFBBF24),
        textPrimaryColor = Color(0xFFEEF2FF),
        surfaceBackgroundColor = Color(0xFF080714)
    )

    val GOOGLY = GameTheme(
        id = "GOOGLY",
        displayName = "Googly Rainbow Party",
        consoleStyleName = "Rainbow Neon Carnival",
        isPremiumOnly = false,
        palette = listOf(Color(0xFFFF3366), Color(0xFFFFB800), Color(0xFF00E676), Color(0xFF00E5FF), Color(0xFFAA00FF)),
        gridStyle = "Rainbow Polka Dots",
        arrowStyle = "Googly Animated Dart",
        backgroundShaderName = "Prismatic Color Shift",
        animationSpeedMs = 160,
        boardCanvasColor = Color(0xFF0F172A),
        gridDotColor = Color(0xFFFF007F),
        arrowNormalColor = Color(0xFF00E5FF),
        arrowHighlightColor = Color(0xFFFFEE00),
        textPrimaryColor = Color(0xFFF8FAFC),
        surfaceBackgroundColor = Color(0xFF020617)
    )

    val allThemes = listOf(EYE_COMFORT, MINIMAL_WHITE, ZEN_WOOD, DARK_SLATE, SOFT_MINT, RETRO_ARCADE, CYBER_TERMINAL, VAPORWAVE, QUANTUM_NEBULA, GOOGLY)

    val entries = allThemes

    fun getTheme(id: String): GameTheme {
        return allThemes.find { it.id.equals(id, ignoreCase = true) } ?: EYE_COMFORT
    }
}
