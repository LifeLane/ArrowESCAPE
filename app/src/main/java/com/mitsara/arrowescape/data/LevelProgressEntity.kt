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
    val autoFirstMoveSuggestion: Boolean = false,
    val isPremium: Boolean = false,
    val totalStars: Int = 0,
    val dailyStreak: Int = 1,
    val lastDailyCompletedDate: String = "",
    val selectedTheme: String = "LIGHT",
    val unlockedSkins: String = "CLASSIC",
    val selectedSkin: String = "CLASSIC"
)
