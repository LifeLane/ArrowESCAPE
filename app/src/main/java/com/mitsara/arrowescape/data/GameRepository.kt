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

    suspend fun unlockSkin(skinId: String, cost: Int): Boolean {
        val current = dao.getUserSettingsDirect() ?: UserSettingsEntity()
        val unlockedList = current.unlockedSkins.split(",").toMutableSet()
        if (unlockedList.contains(skinId)) return true
        if (current.totalStars >= cost) {
            unlockedList.add(skinId)
            dao.saveUserSettings(
                current.copy(
                    totalStars = current.totalStars - cost,
                    unlockedSkins = unlockedList.joinToString(","),
                    selectedSkin = skinId
                )
            )
            return true
        }
        return false
    }

    suspend fun selectSkin(skinId: String) {
        val current = dao.getUserSettingsDirect() ?: UserSettingsEntity()
        val unlockedList = current.unlockedSkins.split(",")
        if (unlockedList.contains(skinId)) {
            dao.saveUserSettings(current.copy(selectedSkin = skinId))
        }
    }

    suspend fun equipCosmetic(category: com.mitsara.arrowescape.model.CosmeticCategory, cosmeticId: String) {
        val current = dao.getUserSettingsDirect() ?: UserSettingsEntity()
        val updated = when (category) {
            com.mitsara.arrowescape.model.CosmeticCategory.ARROW -> current.copy(selectedArrow = cosmeticId)
            com.mitsara.arrowescape.model.CosmeticCategory.BACKGROUND -> current.copy(selectedBackground = cosmeticId)
            com.mitsara.arrowescape.model.CosmeticCategory.BOARD -> current.copy(selectedBoard = cosmeticId)
            com.mitsara.arrowescape.model.CosmeticCategory.GRID -> current.copy(selectedGrid = cosmeticId)
            com.mitsara.arrowescape.model.CosmeticCategory.FRAME -> current.copy(selectedFrame = cosmeticId)
            com.mitsara.arrowescape.model.CosmeticCategory.PRESET -> current
        }
        dao.saveUserSettings(updated)
    }

    suspend fun unlockCosmetic(cosmeticId: String, cost: Int): Boolean {
        val current = dao.getUserSettingsDirect() ?: UserSettingsEntity()
        val unlockedList = current.unlockedCosmetics.split(",").toMutableSet()
        if (unlockedList.contains(cosmeticId)) return true
        if (cost == 0 || current.totalStars >= cost || current.isPremium) {
            unlockedList.add(cosmeticId)
            val newStars = if (current.isPremium || cost == 0) current.totalStars else maxOf(0, current.totalStars - cost)
            val cosmetic = com.mitsara.arrowescape.model.CosmeticsCatalog.getCosmetic(cosmeticId)
            var updated = current.copy(
                totalStars = newStars,
                unlockedCosmetics = unlockedList.joinToString(",")
            )
            if (cosmetic != null) {
                updated = when (cosmetic.category) {
                    com.mitsara.arrowescape.model.CosmeticCategory.ARROW -> updated.copy(selectedArrow = cosmeticId)
                    com.mitsara.arrowescape.model.CosmeticCategory.BACKGROUND -> updated.copy(selectedBackground = cosmeticId)
                    com.mitsara.arrowescape.model.CosmeticCategory.BOARD -> updated.copy(selectedBoard = cosmeticId)
                    com.mitsara.arrowescape.model.CosmeticCategory.GRID -> updated.copy(selectedGrid = cosmeticId)
                    com.mitsara.arrowescape.model.CosmeticCategory.FRAME -> updated.copy(selectedFrame = cosmeticId)
                    com.mitsara.arrowescape.model.CosmeticCategory.PRESET -> updated
                }
            }
            dao.saveUserSettings(updated)
            return true
        }
        return false
    }

    suspend fun equipPreset(preset: com.mitsara.arrowescape.model.CosmeticPreset) {
        val current = dao.getUserSettingsDirect() ?: UserSettingsEntity()
        val unlockedList = current.unlockedCosmetics.split(",").toMutableSet()
        unlockedList.add(preset.arrowId)
        unlockedList.add(preset.backgroundId)
        unlockedList.add(preset.boardId)
        unlockedList.add(preset.gridId)
        unlockedList.add(preset.frameId)
        val updated = current.copy(
            selectedArrow = preset.arrowId,
            selectedBackground = preset.backgroundId,
            selectedBoard = preset.boardId,
            selectedGrid = preset.gridId,
            selectedFrame = preset.frameId,
            unlockedCosmetics = unlockedList.joinToString(",")
        )
        dao.saveUserSettings(updated)
    }

    suspend fun checkDailyStreak() {
        val current = dao.getUserSettingsDirect() ?: UserSettingsEntity()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val calendar = java.util.Calendar.getInstance()
        val today = dateFormat.format(calendar.time)
        if (current.lastDailyCompletedDate == today) return

        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormat.format(calendar.time)
        val newStreak = if (current.lastDailyCompletedDate == yesterday) current.dailyStreak + 1 else 1
        val bonusHints = if (newStreak % 3 == 0) 3 else 1

        dao.saveUserSettings(
            current.copy(
                dailyStreak = newStreak,
                lastDailyCompletedDate = today,
                hintsCount = current.hintsCount + bonusHints,
                totalStars = current.totalStars + 5
            )
        )
    }
}
