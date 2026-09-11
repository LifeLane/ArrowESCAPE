package com.mitsara.arrowescape.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitsara.arrowescape.engine.GameLogicManager
import com.mitsara.arrowescape.engine.LevelGenerator
import com.mitsara.arrowescape.engine.endless.EndlessLevelGenerator
import com.mitsara.arrowescape.model.Arrow
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.Direction
import com.mitsara.arrowescape.model.PuzzleLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EndlessGameState(
    val level: PuzzleLevel, // Represents the bounds and obstacles (though empty)
    val activeArrows: List<Arrow> = emptyList(),
    val score: Int = 0,
    val wave: Int = 1,
    val remainingLives: Int = 3,
    val hintsAvailable: Int = 3,
    val animatingArrowId: Int? = null,
    val animatingDirection: Direction? = null,
    val isMistakeShake: Boolean = false,
    val hintArrowId: Int? = null,
    val isGameOver: Boolean = false
)

class EndlessGameViewModel : ViewModel() {
    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<EndlessGameState> = _state.asStateFlow()

    private var logicManager: GameLogicManager
    private val gridSize = 10
    private val restockThreshold = 15 // When active arrows drop below this, restock
    private val restockAmount = 30
    private var nextArrowId = 1

    init {
        logicManager = GameLogicManager(_state.value.level)
    }

    private fun createInitialState(): EndlessGameState {
        // Start with a clean slate level object, but populate it
        val baseLevel = PuzzleLevel(
            id = 0,
            title = "Endless Mode",
            difficulty = Difficulty.HARD,
            gridWidth = gridSize,
            gridHeight = gridSize,
            arrows = emptyList(),
            startingLives = 3,
            maxHints = 3
        )
        
        val initialArrows = EndlessLevelGenerator.generateRestockWave(gridSize, emptyList(), restockAmount + 10, nextArrowId)
        nextArrowId += initialArrows.size
        
        val startingLevel = baseLevel.copy(arrows = initialArrows)
        
        return EndlessGameState(
            level = startingLevel,
            activeArrows = initialArrows
        )
    }

    fun onArrowTapped(arrowId: Int) {
        if (_state.value.isGameOver || _state.value.animatingArrowId != null) return

        val result = logicManager.processArrowTap(arrowId)
        
        when (result) {
            is GameLogicManager.TapResult.Success -> {
                val arrow = _state.value.activeArrows.find { it.id == arrowId }
                if (arrow != null) {
                    _state.update { it.copy(animatingArrowId = arrowId, animatingDirection = arrow.direction, hintArrowId = null) }
                    
                    viewModelScope.launch {
                        delay(200) // Wait for animation
                        _state.update { 
                            it.copy(
                                animatingArrowId = null, 
                                animatingDirection = null,
                                activeArrows = logicManager.currentActiveArrows,
                                score = it.score + 10
                            )
                        }
                        checkRestock()
                    }
                }
            }
            is GameLogicManager.TapResult.LevelCompleted -> {
                // Should technically never happen if we restock correctly, but just in case
                val arrow = _state.value.activeArrows.find { it.id == arrowId }
                if (arrow != null) {
                    _state.update { it.copy(animatingArrowId = arrowId, animatingDirection = arrow.direction, hintArrowId = null) }
                    viewModelScope.launch {
                        delay(200)
                        _state.update { 
                            it.copy(
                                animatingArrowId = null, 
                                animatingDirection = null,
                                activeArrows = logicManager.currentActiveArrows,
                                score = it.score + 10
                            )
                        }
                        checkRestock()
                    }
                }
            }
            is GameLogicManager.TapResult.Mistake -> {
                _state.update { it.copy(isMistakeShake = true, remainingLives = result.remainingLives) }
                viewModelScope.launch {
                    delay(400)
                    _state.update { it.copy(isMistakeShake = false) }
                }
            }
            is GameLogicManager.TapResult.LevelFailed -> {
                _state.update { it.copy(isGameOver = true, remainingLives = 0) }
            }
            else -> {}
        }
    }

    private fun checkRestock() {
        val currentArrows = logicManager.currentActiveArrows
        if (currentArrows.size <= restockThreshold) {
            // Trigger restock wave
            val newArrows = EndlessLevelGenerator.generateRestockWave(
                gridSize = gridSize,
                existingArrows = currentArrows,
                targetNewArrows = restockAmount,
                startId = nextArrowId
            )
            nextArrowId += newArrows.size
            
            val combinedArrows = currentArrows + newArrows
            
            // Re-initialize logic manager with new full list
            val newLevel = _state.value.level.copy(arrows = combinedArrows)
            logicManager = GameLogicManager(newLevel)
            
            _state.update { 
                it.copy(
                    level = newLevel,
                    activeArrows = combinedArrows,
                    wave = it.wave + 1
                ) 
            }
        }
    }

    fun requestHint() {
        if (_state.value.hintsAvailable > 0 && _state.value.hintArrowId == null) {
            val hintId = logicManager.getNextHintArrowId()
            if (hintId != null) {
                _state.update { it.copy(hintsAvailable = it.hintsAvailable - 1, hintArrowId = hintId) }
            }
        }
    }

    fun retryEndless() {
        _state.value = createInitialState()
        logicManager = GameLogicManager(_state.value.level)
    }
}
