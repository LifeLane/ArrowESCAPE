package com.mitsara.arrowescape.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mitsara.arrowescape.audio.SoundManager
import com.mitsara.arrowescape.data.AppDatabase
import com.mitsara.arrowescape.data.GameRepository
import com.mitsara.arrowescape.data.UserSettingsEntity
import com.mitsara.arrowescape.engine.LevelGenerator
import com.mitsara.arrowescape.engine.PuzzleSolver
import com.mitsara.arrowescape.model.GamePlayState
import com.mitsara.arrowescape.model.PuzzleLevel
import com.mitsara.arrowescape.monetization.AdsManager
import com.mitsara.arrowescape.monetization.SubscriptionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = GameRepository(db.gameDao())
    val soundManager = SoundManager(application)

    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettingsEntity()
        )

    val completedLevels: StateFlow<Set<Int>> = repository.completedLevels
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val levelProgressMap: StateFlow<Map<Int, com.mitsara.arrowescape.data.LevelProgressEntity>> = repository.levelProgressMap
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val _gameState = MutableStateFlow<GamePlayState?>(null)
    val gameState: StateFlow<GamePlayState?> = _gameState.asStateFlow()

    init {
        viewModelScope.launch {
            userSettings.collect { settings ->
                soundManager.soundEnabled = settings.soundEnabled
                soundManager.vibrationEnabled = settings.vibrationEnabled
                soundManager.hapticLevel = settings.hapticLevel
                SubscriptionManager.updatePremiumState(settings.isPremium)
            }
        }
        viewModelScope.launch {
            repository.checkDailyStreak()
        }
    }

    private var timerJob: kotlinx.coroutines.Job? = null

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _gameState.update { state ->
                    if (state == null || state.isCompleted || state.isFailed) return@update state
                    val newElapsed = state.elapsedSeconds + 1
                    val newCooldown = if (state.hintCooldown > 0) state.hintCooldown - 1 else 0
                    state.copy(elapsedSeconds = newElapsed, hintCooldown = newCooldown)
                }
            }
        }
    }

    fun startLevel(levelId: Int) {
        val level = LevelGenerator.getLevel(levelId)
        val initialHints = userSettings.value.hintsCount
        val autoSuggest = userSettings.value.autoFirstMoveSuggestion
        val settings = userSettings.value

        val initialHintId = if (autoSuggest) {
            PuzzleSolver.getHintArrowId(
                activeArrows = level.arrows,
                gridWidth = level.gridWidth,
                gridHeight = level.gridHeight,
                obstacles = level.obstacles
            )
        } else null

        _gameState.value = GamePlayState(
            level = level,
            activeArrows = level.arrows,
            remainingLives = level.startingLives,
            hintsAvailable = if (settings.isPremium) 999 else initialHints,
            hintArrowId = initialHintId,
            laserCharges = settings.laserCharges,
            shieldCharges = settings.shieldCharges,
            magnetCharges = settings.magnetCharges
        )
        startTimer()
    }

    fun activatePowerUp(type: com.mitsara.arrowescape.model.PowerUpType) {
        val state = _gameState.value ?: return
        if (state.isCompleted || state.isFailed) return

        when (type) {
            com.mitsara.arrowescape.model.PowerUpType.LASER_VAPORIZER -> {
                val hasCharges = userSettings.value.isPremium || state.laserCharges > 0
                if (hasCharges) {
                    soundManager.playHintSound()
                    _gameState.update {
                        it?.copy(
                            isPowerupActive = !it.isPowerupActive,
                            selectedPowerUp = if (!it.isPowerupActive) type else null
                        )
                    }
                }
            }
            com.mitsara.arrowescape.model.PowerUpType.ZEN_SHIELD -> {
                val hasCharges = userSettings.value.isPremium || state.shieldCharges > 0
                if (hasCharges && state.activeShieldTaps <= 0) {
                    viewModelScope.launch {
                        repository.consumePowerUp(type)
                        soundManager.playVictorySound()
                        _gameState.update {
                            it?.copy(
                                activeShieldTaps = 2,
                                shieldCharges = maxOf(0, it.shieldCharges - 1),
                                shieldTriggeredMessage = "🛡️ ZEN SHIELD ACTIVE (2 Taps)"
                            )
                        }
                        delay(2000)
                        _gameState.update { it?.copy(shieldTriggeredMessage = null) }
                    }
                }
            }
            com.mitsara.arrowescape.model.PowerUpType.SONAR_MAGNET -> {
                val hasCharges = userSettings.value.isPremium || state.magnetCharges > 0
                if (hasCharges) {
                    viewModelScope.launch {
                        repository.consumePowerUp(type)
                        soundManager.playPowerupBlastSound()
                        _gameState.update {
                            it?.copy(
                                magnetCharges = maxOf(0, it.magnetCharges - 1),
                                activeComboMessage = "🧲 SONAR BURST ENGAGED!"
                            )
                        }
                        triggerSonarBurstCascade()
                    }
                }
            }
        }
    }

    private suspend fun triggerSonarBurstCascade() {
        val countToEscape = 3
        for (i in 1..countToEscape) {
            val state = _gameState.value ?: break
            if (state.isCompleted || state.isFailed || state.activeArrows.isEmpty()) break

            // Find an unobstructed arrow
            val target = state.activeArrows.firstOrNull { arrow ->
                PuzzleSolver.isArrowUnobstructed(
                    arrow = arrow,
                    activeArrows = state.activeArrows,
                    gridWidth = state.level.gridWidth,
                    gridHeight = state.level.gridHeight,
                    obstacles = state.level.obstacles
                )
            } ?: state.activeArrows.firstOrNull() ?: break

            onArrowTapped(target.id)
            delay(280)
        }
    }

    fun togglePowerup() {
        activatePowerUp(com.mitsara.arrowescape.model.PowerUpType.LASER_VAPORIZER)
    }

    fun onArrowTapped(arrowId: Int) {
        val currentState = _gameState.value ?: return
        if (currentState.isCompleted || currentState.isFailed || currentState.animatingArrowId != null) return

        val themeId = userSettings.value.selectedTheme
        soundManager.playTapSound(themeId)

        val arrow = currentState.activeArrows.find { it.id == arrowId } ?: return
        val isUnobstructed = PuzzleSolver.isArrowUnobstructed(
            arrow = arrow,
            activeArrows = currentState.activeArrows,
            gridWidth = currentState.level.gridWidth,
            gridHeight = currentState.level.gridHeight,
            obstacles = currentState.level.obstacles
        )

        val isLaserActive = currentState.isPowerupActive && currentState.selectedPowerUp == com.mitsara.arrowescape.model.PowerUpType.LASER_VAPORIZER
        val canEscape = isUnobstructed || isLaserActive

        if (canEscape) {
            if (isLaserActive) {
                soundManager.playPowerupBlastSound()
                viewModelScope.launch {
                    repository.consumePowerUp(com.mitsara.arrowescape.model.PowerUpType.LASER_VAPORIZER)
                }
            } else {
                soundManager.playEscapeSound(themeId)
            }

            val historyEntry = com.mitsara.arrowescape.model.MoveHistoryEntry(
                activeArrows = currentState.activeArrows,
                escapedArrowIds = currentState.escapedArrowIds,
                moveCount = currentState.moveCount
            )

            val now = System.currentTimeMillis()
            val timeSinceLast = now - currentState.lastEscapeTimestamp
            val newCombo = if (timeSinceLast < 3500L && currentState.lastEscapeTimestamp > 0L) {
                minOf(5, currentState.comboMultiplier + 1)
            } else {
                1
            }

            // Award bonus coins at 3x combo
            if (newCombo >= 3 && currentState.comboMultiplier < newCombo) {
                viewModelScope.launch {
                    repository.addCoins(10)
                }
            }

            // Award free power-up at 5x combo
            if (newCombo == 5 && currentState.comboMultiplier < 5) {
                viewModelScope.launch {
                    repository.addPowerUp(com.mitsara.arrowescape.model.PowerUpType.LASER_VAPORIZER, 1)
                }
            }

            val comboMsg = when (newCombo) {
                5 -> "5x MAX COMBO! +POWERUP! ⚡"
                4 -> "4x SUPER COMBO! 🔥"
                3 -> "3x COMBO! +10 🪙"
                2 -> "2x COMBO!"
                else -> null
            }
            val moveScore = 150 * newCombo

            val newLaserCharges = if (isLaserActive) maxOf(0, currentState.laserCharges - 1) else currentState.laserCharges
            val finalLaserCharges = if (newCombo >= 5) newLaserCharges + 1 else newLaserCharges

            val updatedObstacles = if (isLaserActive) {
                val ray = arrow.getExitRay(currentState.level.gridWidth, currentState.level.gridHeight)
                currentState.level.obstacles.filter { obs -> !ray.contains(obs) }.toSet()
            } else {
                currentState.level.obstacles
            }

            val animDurationMs = com.mitsara.arrowescape.engine.EscapePathEngine.calculateEscapeDurationMs(
                arrow = arrow,
                gridWidth = currentState.level.gridWidth,
                gridHeight = currentState.level.gridHeight
            )

            viewModelScope.launch {
                // Set animation state
                _gameState.update { state ->
                    state?.copy(
                        animatingArrowId = arrowId,
                        animatingDirection = arrow.direction,
                        hintArrowId = null,
                        moveCount = (state?.moveCount ?: 0) + 1,
                        flowCount = (state?.flowCount ?: 0) + 1,
                        moveHistory = (state?.moveHistory ?: emptyList()) + historyEntry,
                        comboMultiplier = newCombo,
                        comboCharge = minOf(5, (state?.comboCharge ?: 0) + 1),
                        lastEscapeTimestamp = now,
                        activeComboMessage = comboMsg,
                        score = (state?.score ?: 0) + moveScore,
                        laserCharges = finalLaserCharges,
                        isPowerupActive = false,
                        selectedPowerUp = null,
                        level = state.level.copy(obstacles = updatedObstacles)
                    )
                }

                delay(animDurationMs.toLong()) // Path length based animation duration

                // Commit escape
                val updatedState = _gameState.value ?: return@launch
                val remainingArrows = updatedState.activeArrows.filter { it.id != arrowId }
                val newEscaped = updatedState.escapedArrowIds + arrowId
                val isLevelCleared = remainingArrows.isEmpty()

                val finalScore = if (isLevelCleared) {
                    val timeBonus = maxOf(0, 4000 - updatedState.elapsedSeconds * 30)
                    val lifeBonus = updatedState.remainingLives * 1500
                    updatedState.score + timeBonus + lifeBonus
                } else {
                    updatedState.score
                }

                var earnedRewards = Pair(0, 0)
                if (isLevelCleared) {
                    soundManager.playVictorySound()
                    val stars = when {
                        updatedState.remainingLives >= 3 -> 3
                        updatedState.remainingLives == 2 -> 2
                        else -> 1
                    }
                    earnedRewards = repository.markLevelCompleted(
                        levelId = updatedState.level.id,
                        stars = stars,
                        moveCount = updatedState.moveCount
                    )
                }

                _gameState.update { state ->
                    state?.copy(
                        activeArrows = remainingArrows,
                        escapedArrowIds = newEscaped,
                        animatingArrowId = null,
                        animatingDirection = null,
                        isCompleted = isLevelCleared,
                        score = finalScore,
                        earnedCoins = earnedRewards.first,
                        earnedDiamonds = earnedRewards.second
                    )
                }
            }
        } else {
            // MISTAKE! CHECK IF ZEN SHIELD IS ACTIVE
            if (currentState.activeShieldTaps > 0) {
                soundManager.playHintSound()
                val remainingShields = currentState.activeShieldTaps - 1
                viewModelScope.launch {
                    _gameState.update { state ->
                        state?.copy(
                            activeShieldTaps = remainingShields,
                            shieldTriggeredMessage = "🛡️ ZEN SHIELD PROTECTED MISTAKE! ($remainingShields Left)"
                        )
                    }
                    delay(1200)
                    _gameState.update { state ->
                        state?.copy(shieldTriggeredMessage = null)
                    }
                }
                return
            }

            // Normal Mistake! Lost a life
            soundManager.playMistakeSound()
            val newLives = currentState.remainingLives - 1

            viewModelScope.launch {
                _gameState.update { state ->
                    state?.copy(
                        remainingLives = newLives,
                        isMistakeShake = true,
                        isFailed = newLives <= 0,
                        flowCount = 0,
                        inspectedArrowId = arrowId,
                        comboMultiplier = 1,
                        comboCharge = 0,
                        activeComboMessage = null,
                        isPowerupActive = false,
                        selectedPowerUp = null
                    )
                }

                delay(600)

                _gameState.update { state ->
                    state?.copy(isMistakeShake = false, inspectedArrowId = null)
                }
            }
        }
    }

    fun buyPowerUp(type: com.mitsara.arrowescape.model.PowerUpType, useDiamonds: Boolean) {
        viewModelScope.launch {
            val success = repository.buyPowerUp(type, useDiamonds)
            if (success) {
                soundManager.playVictorySound()
                _gameState.update { state ->
                    when (type) {
                        com.mitsara.arrowescape.model.PowerUpType.LASER_VAPORIZER -> state?.copy(laserCharges = state.laserCharges + if (useDiamonds) 2 else 1)
                        com.mitsara.arrowescape.model.PowerUpType.ZEN_SHIELD -> state?.copy(shieldCharges = state.shieldCharges + if (useDiamonds) 2 else 1)
                        com.mitsara.arrowescape.model.PowerUpType.SONAR_MAGNET -> state?.copy(magnetCharges = state.magnetCharges + if (useDiamonds) 2 else 1)
                    }
                }
            }
        }
    }

    fun unlockCosmeticWithCurrency(cosmeticId: String, currencyType: String, cost: Int) {
        viewModelScope.launch {
            val success = repository.unlockCosmeticWithCurrency(cosmeticId, currencyType, cost)
            if (success) {
                soundManager.playVictorySound()
            }
        }
    }

    fun addBonusCoins(amount: Int) {
        viewModelScope.launch {
            repository.addCoins(amount)
        }
    }

    fun addBonusDiamonds(amount: Int) {
        viewModelScope.launch {
            repository.addDiamonds(amount)
        }
    }

    fun undoMove() {
        val currentState = _gameState.value ?: return
        if (!currentState.canUndo) return

        val lastEntry = currentState.moveHistory.lastOrNull() ?: return
        val updatedHistory = currentState.moveHistory.dropLast(1)

        soundManager.playTapSound()
        _gameState.update { state ->
            state?.copy(
                activeArrows = lastEntry.activeArrows,
                escapedArrowIds = lastEntry.escapedArrowIds,
                moveCount = lastEntry.moveCount,
                flowCount = 0,
                moveHistory = updatedHistory,
                hintArrowId = null,
                animatingArrowId = null,
                isCompleted = false
            )
        }
    }

    fun requestHint() {
        val currentState = _gameState.value ?: return
        if (currentState.isCompleted || currentState.isFailed || currentState.hintCooldown > 0) return

        viewModelScope.launch {
            val hasHint = repository.consumeHint()
            if (hasHint || userSettings.value.isPremium) {
                val hintId = PuzzleSolver.getHintArrowId(
                    activeArrows = currentState.activeArrows,
                    gridWidth = currentState.level.gridWidth,
                    gridHeight = currentState.level.gridHeight,
                    obstacles = currentState.level.obstacles
                )

                if (hintId != null) {
                    soundManager.playHintSound()
                    _gameState.update { state ->
                        state?.copy(
                            hintArrowId = hintId,
                            hintsAvailable = if (userSettings.value.isPremium) 999 else maxOf(0, state.hintsAvailable - 1),
                            hintCooldown = 5
                        )
                    }
                }
            }
        }
    }

    fun selectTheme(themeId: String) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(selectedTheme = themeId) }
        }
    }

    fun updateCosmetic(category: String, cosmeticId: String) {
        viewModelScope.launch {
            repository.updateSettings { current ->
                when (category) {
                    "ARROW" -> current.copy(selectedArrow = cosmeticId)
                    "BACKGROUND" -> current.copy(selectedBackground = cosmeticId)
                    "BOARD" -> current.copy(selectedBoard = cosmeticId)
                    "GRID" -> current.copy(selectedGrid = cosmeticId)
                    "FRAME" -> current.copy(selectedFrame = cosmeticId)
                    else -> current
                }
            }
        }
    }

    fun equipCosmetic(category: com.mitsara.arrowescape.model.CosmeticCategory, cosmeticId: String) {
        viewModelScope.launch {
            soundManager.playTapSound()
            repository.equipCosmetic(category, cosmeticId)
        }
    }

    fun unlockCosmetic(cosmeticId: String, cost: Int) {
        viewModelScope.launch {
            val success = repository.unlockCosmetic(cosmeticId, cost)
            if (success) {
                soundManager.playVictorySound()
            }
        }
    }

    fun equipPreset(preset: com.mitsara.arrowescape.model.CosmeticPreset) {
        viewModelScope.launch {
            soundManager.playVictorySound()
            repository.equipPreset(preset)
        }
    }

    fun unlockSkin(skinId: String, cost: Int) {
        viewModelScope.launch {
            repository.unlockSkin(skinId, cost)
        }
    }

    fun selectSkin(skinId: String) {
        viewModelScope.launch {
            repository.selectSkin(skinId)
        }
    }

    fun retryLevel() {
        val currentLevelId = _gameState.value?.level?.id ?: userSettings.value.currentLevelId
        startLevel(currentLevelId)
    }

    fun nextLevel() {
        val currentLevelId = _gameState.value?.level?.id ?: 1
        val nextLevelId = currentLevelId + 1
        startLevel(nextLevelId)
    }

    fun toggleSound() {
        viewModelScope.launch {
            repository.updateSettings { it.copy(soundEnabled = !it.soundEnabled) }
        }
    }

    fun toggleVibration() {
        viewModelScope.launch {
            repository.updateSettings { it.copy(vibrationEnabled = !it.vibrationEnabled) }
        }
    }

    fun toggleAutoFirstMoveSuggestion() {
        viewModelScope.launch {
            repository.updateSettings { it.copy(autoFirstMoveSuggestion = !it.autoFirstMoveSuggestion) }
        }
    }

    fun addRewardHints(count: Int) {
        viewModelScope.launch {
            repository.addHints(count)
            _gameState.update { state ->
                state?.copy(hintsAvailable = (state.hintsAvailable + count))
            }
        }
    }

    fun setHapticLevel(level: String) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(hapticLevel = level) }
        }
    }

    fun setCloudSync(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(cloudSyncEnabled = enabled) }
        }
    }

    fun setPremiumStatus(isPremium: Boolean) {
        viewModelScope.launch {
            repository.setPremium(isPremium)
        }
    }

    fun claimAchievement(achievementId: String, rewardDiamonds: Int) {
        viewModelScope.launch {
            val success = repository.claimAchievement(achievementId, rewardDiamonds)
            if (success) {
                soundManager.playAchievementClaimSound()
            }
        }
    }

    fun saveChronoResult(score: Int, earnedCoins: Int, earnedDiamonds: Int) {
        viewModelScope.launch {
            repository.saveChronoHighScore(score, earnedCoins, earnedDiamonds)
        }
    }

    fun addZenEscapes(count: Int) {
        viewModelScope.launch {
            repository.addZenEscapes(count)
        }
    }
}
