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
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val hapticLevel: String = "MEDIUM", // OFF, LIGHT, MEDIUM, HEAVY
    val cloudSyncEnabled: Boolean = false,
    val autoFirstMoveSuggestion: Boolean = false,
    val isPremium: Boolean = false,
    val totalStars: Int = 0,
    val dailyStreak: Int = 1,
    val lastDailyCompletedDate: String = "",
    val selectedTheme: String = "RETRO_ARCADE",
    val unlockedSkins: String = "CLASSIC",
    val selectedSkin: String = "CLASSIC",
    val selectedArrow: String = "ARROW_CYBER_NEON",
    val selectedBackground: String = "BG_DEEP_COSMOS",
    val selectedBoard: String = "BOARD_OBSIDIAN",
    val selectedGrid: String = "GRID_NEON_LATTICE",
    val selectedFrame: String = "FRAME_CYBER_BRACKETS",
    val unlockedCosmetics: String = "ARROW_CYBER_NEON,BG_DEEP_COSMOS,BOARD_OBSIDIAN,GRID_NEON_LATTICE,FRAME_CYBER_BRACKETS"
)
