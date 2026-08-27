package com.mitsara.arrowescape.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: GameDao) {

    val completedLevels: Flow<Set<Int>> = dao.getAllCompletedLevels().map { list ->
        list.filter { it.isCompleted }.map { it.levelId }.toSet()
    }

    val levelProgressMap: Flow<Map<Int, LevelProgressEntity>> = dao.getAllLevelProgress().map { list ->
        list.associateBy { it.levelId }
    }

    val userSettings: Flow<UserSettingsEntity> = dao.getUserSettings().map {
        it ?: UserSettingsEntity()
    }

    suspend fun markLevelCompleted(levelId: Int, stars: Int, moveCount: Int) {
        val existing = dao.getLevelProgress(levelId)
        val bestStars = maxOf(existing?.stars ?: 0, stars)
        dao.saveLevelProgress(
            LevelProgressEntity(
                levelId = levelId,
                stars = bestStars,
                isCompleted = true,
                moveCount = moveCount
            )
        )

        val current = dao.getUserSettingsDirect() ?: UserSettingsEntity()
        val newCurrentLevel = maxOf(current.currentLevelId, levelId + 1)
        val newTotalStars = current.totalStars + if (existing == null || !existing.isCompleted) stars else 0

        dao.saveUserSettings(
            current.copy(
                currentLevelId = newCurrentLevel,
                totalStars = newTotalStars
            )
        )
    }

    suspend fun updateSettings(transform: (UserSettingsEntity) -> UserSettingsEntity) {
        val current = dao.getUserSettingsDirect() ?: UserSettingsEntity()
        val updated = transform(current)
        dao.saveUserSettings(updated)
    }

    suspend fun addHints(amount: Int) {
        updateSettings { current ->
            current.copy(hintsCount = current.hintsCount + amount)
        }
    }

    suspend fun consumeHint(): Boolean {
        val current = dao.getUserSettingsDirect() ?: UserSettingsEntity()
        if (current.isPremium) return true // Unlimited hints for premium!
        if (current.hintsCount > 0) {
            dao.saveUserSettings(current.copy(hintsCount = current.hintsCount - 1))
            return true
        }
        return false
    }

    suspend fun setPremium(isPremium: Boolean) {
        updateSettings { it.copy(isPremium = isPremium) }
    }
}
