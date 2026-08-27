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
                SubscriptionManager.updatePremiumState(settings.isPremium)
            }
        }
    }

    fun startLevel(levelId: Int) {
        val level = LevelGenerator.getLevel(levelId)
        val initialHints = userSettings.value.hintsCount
        val autoSuggest = userSettings.value.autoFirstMoveSuggestion

        val initialHintId = if (autoSuggest) {
            PuzzleSolver.getHintArrowId(
                activeArrows = level.arrows,
                gridWidth = level.gridWidth,
                gridHeight = level.gridHeight
            )
        } else null

        _gameState.value = GamePlayState(
            level = level,
            activeArrows = level.arrows,
            remainingLives = level.startingLives,
            hintsAvailable = if (userSettings.value.isPremium) 999 else initialHints,
            hintArrowId = initialHintId
        )
    }

    fun onArrowTapped(arrowId: Int) {
        val currentState = _gameState.value ?: return
        if (currentState.isCompleted || currentState.isFailed || currentState.animatingArrowId != null) return

        soundManager.playTapSound()

        val arrow = currentState.activeArrows.find { it.id == arrowId } ?: return
        val isUnobstructed = PuzzleSolver.isArrowUnobstructed(
            arrow = arrow,
            activeArrows = currentState.activeArrows,
            gridWidth = currentState.level.gridWidth,
            gridHeight = currentState.level.gridHeight
        )

        if (isUnobstructed) {
            // SUCCESSFUL ARROW ESCAPE!
            soundManager.playEscapeSound()

            val historyEntry = com.mitsara.arrowescape.model.MoveHistoryEntry(
                activeArrows = currentState.activeArrows,
                escapedArrowIds = currentState.escapedArrowIds,
                moveCount = currentState.moveCount
            )

            viewModelScope.launch {
                // Set animation state
                _gameState.update { state ->
                    state?.copy(
                        animatingArrowId = arrowId,
                        animatingDirection = arrow.direction,
                        hintArrowId = null,
                        moveCount = state.moveCount + 1,
                        moveHistory = state.moveHistory + historyEntry
                    )
                }

                delay(220) // Animation duration

                // Commit escape
                val remainingArrows = currentState.activeArrows.filter { it.id != arrowId }
                val newEscaped = currentState.escapedArrowIds + arrowId
                val isLevelCleared = remainingArrows.isEmpty()

                _gameState.update { state ->
                    state?.copy(
                        activeArrows = remainingArrows,
                        escapedArrowIds = newEscaped,
                        animatingArrowId = null,
                        animatingDirection = null,
                        isCompleted = isLevelCleared
                    )
                }

                if (isLevelCleared) {
                    soundManager.playVictorySound()
                    val stars = when {
                        currentState.remainingLives >= 3 -> 3
                        currentState.remainingLives == 2 -> 2
                        else -> 1
                    }
                    repository.markLevelCompleted(
                        levelId = currentState.level.id,
                        stars = stars,
                        moveCount = currentState.moveCount + 1
                    )
                }
            }
        } else {
            // MISTAKE! ARROW IS BLOCKED!
            soundManager.playMistakeSound()
            val newLives = currentState.remainingLives - 1

            viewModelScope.launch {
                _gameState.update { state ->
                    state?.copy(
                        remainingLives = newLives,
                        isMistakeShake = true,
                        isFailed = newLives <= 0
                    )
                }

                delay(400)

                _gameState.update { state ->
                    state?.copy(isMistakeShake = false)
                }
            }
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
                moveHistory = updatedHistory,
                hintArrowId = null,
                animatingArrowId = null,
                isCompleted = false
            )
        }
    }

    fun requestHint() {
        val currentState = _gameState.value ?: return
        if (currentState.isCompleted || currentState.isFailed) return

        viewModelScope.launch {
            val hasHint = repository.consumeHint()
            if (hasHint) {
                val hintId = PuzzleSolver.getHintArrowId(
                    activeArrows = currentState.activeArrows,
                    gridWidth = currentState.level.gridWidth,
                    gridHeight = currentState.level.gridHeight
                )

                if (hintId != null) {
                    soundManager.playHintSound()
                    _gameState.update { state ->
                        state?.copy(
                            hintArrowId = hintId,
                            hintsAvailable = if (userSettings.value.isPremium) 999 else maxOf(0, state.hintsAvailable - 1)
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

    fun setPremiumStatus(isPremium: Boolean) {
        viewModelScope.launch {
            repository.setPremium(isPremium)
        }
    }
}
