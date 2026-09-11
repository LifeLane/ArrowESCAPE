package com.mitsara.arrowescape.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelId: Int,
    val stars: Int,
    val isCompleted: Boolean,
    val moveCount: Int,
    val completedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val currentLevelId: Int = 1,
    val hintsCount: Int = 5,
    val coins: Int = 150,
    val diamonds: Int = 15,
    val laserCharges: Int = 3,
    val shieldCharges: Int = 3,
    val magnetCharges: Int = 3,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val hapticLevel: String = "MEDIUM", // OFF, LIGHT, MEDIUM, HEAVY
    val cloudSyncEnabled: Boolean = false,
    val autoFirstMoveSuggestion: Boolean = false,
    val isPremium: Boolean = false,
    val totalStars: Int = 0,
    val dailyStreak: Int = 1,
    val lastDailyCompletedDate: String = "",
    val selectedTheme: String = "EYE_COMFORT",
    val unlockedSkins: String = "CLASSIC",
    val selectedSkin: String = "CLASSIC",
    val selectedArrow: String = "ARROW_INK_CONTOUR",
    val selectedBackground: String = "BG_CALM_PARCHMENT",
    val selectedBoard: String = "BOARD_PARCHMENT",
    val selectedGrid: String = "GRID_DOT_MATRIX",
    val selectedFrame: String = "FRAME_CLEAN_MINIMAL",
    val unlockedCosmetics: String = "ARROW_INK_CONTOUR,ARROW_CYBER_NEON,BG_CALM_PARCHMENT,BOARD_PARCHMENT,GRID_DOT_MATRIX,FRAME_CLEAN_MINIMAL",
    val chronoHighScore: Int = 0,
    val zenTotalEscapes: Int = 0,
    val claimedAchievements: String = "",
    val customLevelsJson: String = "[]"
)
