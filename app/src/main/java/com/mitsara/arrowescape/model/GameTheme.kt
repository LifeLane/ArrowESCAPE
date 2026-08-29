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

    val ZEN_WOOD = GameTheme(
        id = "ZEN_WOOD",
        displayName = "Zen Woodcraft",
        consoleStyleName = "Cedar Sanctuary",
        isPremiumOnly = true,
        palette = listOf(Color(0xFF422E22), Color(0xFFD97706), Color(0xFF92400E), Color(0xFF78350F), Color(0xFFB45309)),
        gridStyle = "Engraved Bamboo Grid",
        arrowStyle = "Carved Wood Chisel",
        backgroundShaderName = "Bamboo Paper Grain",
        animationSpeedMs = 300,
        boardCanvasColor = Color(0xFFF7F2E7),
        gridDotColor = Color(0xFFD4C3A3),
        arrowNormalColor = Color(0xFF422E22),
        arrowHighlightColor = Color(0xFFD97706),
        textPrimaryColor = Color(0xFF2C2018),
        surfaceBackgroundColor = Color(0xFFEFEAD8)
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

    val allThemes = listOf(RETRO_ARCADE, CYBER_TERMINAL, ZEN_WOOD, VAPORWAVE, QUANTUM_NEBULA)

    val entries = allThemes

    fun getTheme(id: String): GameTheme {
        return allThemes.find { it.id.equals(id, ignoreCase = true) } ?: RETRO_ARCADE
    }
}
