package com.mitsara.arrowescape.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.data.LevelProgressEntity
import com.mitsara.arrowescape.ui.components.AdBannerView
import com.mitsara.arrowescape.ui.motion.StageEnvironmentCanvas
import com.mitsara.arrowescape.ui.motion.StageProfiles
import com.mitsara.arrowescape.ui.motion.StageVisualProfile
import com.mitsara.arrowescape.ui.theme.GoldStar

data class GamePhase(
    val phaseNumber: Int,
    val title: String,
    val subtitle: String,
    val startLevel: Int,
    val endLevel: Int,
    val primaryColor: Color,
    val secondaryColor: Color
)

val GamePhases = listOf(
    GamePhase(1, "The Awakening", "Classic Orthogonal Sliders", 1, 50, Color(0xFF3B82F6), Color(0xFF1D4ED8)),
    GamePhase(2, "Neon Lattice", "Multi-segment L-Arrows", 51, 100, Color(0xFF06B6D4), Color(0xFF0E7490)),
    GamePhase(3, "Cyber Labyrinth", "Stationary Steel Barriers", 101, 150, Color(0xFF8B5CF6), Color(0xFF6D28D9)),
    GamePhase(4, "Cosmic Drift", "Nebula Orbital Drift", 151, 200, Color(0xFFF43F5E), Color(0xFFBE185D)),
    GamePhase(5, "Plasma Circuit", "Electric Pulse Paths", 201, 250, Color(0xFFF59E0B), Color(0xFFB45309)),
    GamePhase(6, "Quantum Shift", "Spatial Ripple Flashes", 251, 300, Color(0xFF10B981), Color(0xFF047857)),
    GamePhase(7, "Gravity Core", "Gravitational Singularity", 301, 350, Color(0xFF6366F1), Color(0xFF4338CA)),
    GamePhase(8, "Laser Grid", "Security Defense Matrix", 351, 400, Color(0xFFEF4444), Color(0xFF991B1B)),
    GamePhase(9, "Void Escape", "Dark Dimensional Void", 401, 450, Color(0xFF9333EA), Color(0xFF581C87)),
    GamePhase(10, "Infinity Core", "Master-Tier Singularity Finale", 451, 500, Color(0xFFFFB800), Color(0xFFC2410C))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhaseRoadmapScreen(
    currentLevelId: Int,
    completedLevels: Set<Int>,
    levelProgressMap: Map<Int, LevelProgressEntity> = emptyMap(),
    selectedTheme: String = "LIGHT",
    isPremium: Boolean = false,
    onPremiumClick: () -> Unit = {},
    onLevelSelected: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val isGooglyMode = selectedTheme.equals("GOOGLY", ignoreCase = true)
    var selectedPhaseIndex by remember { mutableIntStateOf(((currentLevelId - 1) / 50).coerceIn(0, 9)) }
    var isPhaseDetailOpened by remember { mutableStateOf(false) }

    val currentPhase = GamePhases.getOrElse(selectedPhaseIndex) { GamePhases[0] }
    val profile = StageProfiles.getProfile(currentPhase.phaseNumber)

    // Calculate total stars collected across all levels
    val totalStars = remember(levelProgressMap) {
        levelProgressMap.values.sumOf { it.stars }
    }

    // Determine the overall next playable level across the game
    val activePlayableLevel = remember(currentLevelId, completedLevels) {
        val maxUnlocked = maxOf(currentLevelId, (completedLevels.maxOrNull() ?: 1))
        maxUnlocked
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070913))
    ) {
        // Dynamic Stage Environment Canvas in the background
        AnimatedContent(
            targetState = currentPhase.phaseNumber,
            transitionSpec = {
                fadeIn(animationSpec = tween(400, easing = LinearEasing)) togetherWith
                        fadeOut(animationSpec = tween(400, easing = LinearEasing))
            },
            label = "StageCanvasTransition"
        ) { stageNum ->
            StageEnvironmentCanvas(
                stageNumber = stageNum,
                isGooglyMode = isGooglyMode,
                modifier = Modifier.fillMaxSize()
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isPhaseDetailOpened) "PHASE ${currentPhase.phaseNumber}" else "LEVEL ROADMAP",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = profile.primaryColor.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isPhaseDetailOpened) "LVL ${currentPhase.startLevel}-${currentPhase.endLevel}" else "10 PHASES",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = profile.accentGlow,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = currentPhase.title,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = profile.accentGlow
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (isPhaseDetailOpened) {
                                    isPhaseDetailOpened = false
                                } else {
                                    onBackClick()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (isPhaseDetailOpened) {
                            IconButton(onClick = { isPhaseDetailOpened = false }) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "View Phase Grid",
                                    tint = profile.accentGlow
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1E293B).copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldStar.copy(alpha = 0.6f)),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Stars",
                                    tint = GoldStar,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$totalStars",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070913).copy(alpha = 0.85f))
                )
            },
            bottomBar = {
                // =========================================================================
                // BOTTOM BAR:
                // 1. In Matrix Mode -> Active Level CTA (e.g. "PLAY LEVEL X")
                // 2. In Phase Detail Mode -> Next / Back Buttons + Slider to slide phases!
                // =========================================================================
                Surface(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = Color(0xFF0B101D).copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isPhaseDetailOpened) {
                        // Matrix Mode: Active Level CTA at the bottom
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { onLevelSelected(activePlayableLevel) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF)
                                ),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("active_level_cta")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "CONTINUE: LEVEL $activePlayableLevel",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    } else {
                        // Opened Phase Mode: Back & Next buttons with Slider between them!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Phase Slider Indicator Label
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PHASE ${currentPhase.phaseNumber} / 10",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = profile.accentGlow
                                )
                                Text(
                                    text = currentPhase.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFCBD5E1)
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // BACK BUTTON (Previous Phase)
                                IconButton(
                                    onClick = {
                                        if (selectedPhaseIndex > 0) {
                                            selectedPhaseIndex--
                                        }
                                    },
                                    enabled = selectedPhaseIndex > 0,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedPhaseIndex > 0) Color(0xFF1E293B) else Color(0xFF0F172A)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous Phase",
                                        tint = if (selectedPhaseIndex > 0) Color.White else Color(0xFF475569),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // SLIDER TO SLIDE BETWEEN PHASES
                                Slider(
                                    value = selectedPhaseIndex.toFloat(),
                                    onValueChange = { newVal ->
                                        selectedPhaseIndex = newVal.toInt().coerceIn(0, 9)
                                    },
                                    valueRange = 0f..9f,
                                    steps = 8,
                                    colors = SliderDefaults.colors(
                                        thumbColor = profile.accentGlow,
                                        activeTrackColor = profile.primaryColor,
                                        inactiveTrackColor = Color(0xFF1E293B),
                                        activeTickColor = Color.Transparent,
                                        inactiveTickColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("phase_slider")
                                )

                                // NEXT BUTTON (Next Phase)
                                IconButton(
                                    onClick = {
                                        if (selectedPhaseIndex < 9) {
                                            selectedPhaseIndex++
                                        }
                                    },
                                    enabled = selectedPhaseIndex < 9,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedPhaseIndex < 9) Color(0xFF1E293B) else Color(0xFF0F172A)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next Phase",
                                        tint = if (selectedPhaseIndex < 9) Color.White else Color(0xFF475569),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (!isPhaseDetailOpened) {
                    // =========================================================================
                    // 1. ALL 10 PHASES STACKED ON SCREEN IN A 2×3×3×2 COMPACT MATRIX
                    // =========================================================================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // ROW 1: 2 Phases (Phase 1, Phase 2)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PhaseGridCard(
                                phase = GamePhases[0],
                                currentLevelId = currentLevelId,
                                completedLevels = completedLevels,
                                isPremium = isPremium,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedPhaseIndex = 0
                                    isPhaseDetailOpened = true
                                }
                            )
                            PhaseGridCard(
                                phase = GamePhases[1],
                                currentLevelId = currentLevelId,
                                completedLevels = completedLevels,
                                isPremium = isPremium,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedPhaseIndex = 1
                                    isPhaseDetailOpened = true
                                }
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // ROW 2: 3 Phases (Phase 3, Phase 4, Phase 5)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PhaseGridCard(
                                phase = GamePhases[2],
                                currentLevelId = currentLevelId,
                                completedLevels = completedLevels,
                                isPremium = isPremium,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedPhaseIndex = 2
                                    isPhaseDetailOpened = true
                                }
                            )
                            PhaseGridCard(
                                phase = GamePhases[3],
                                currentLevelId = currentLevelId,
                                completedLevels = completedLevels,
                                isPremium = isPremium,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedPhaseIndex = 3
                                    isPhaseDetailOpened = true
                                }
                            )
                            PhaseGridCard(
                                phase = GamePhases[4],
                                currentLevelId = currentLevelId,
                                completedLevels = completedLevels,
                                isPremium = isPremium,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedPhaseIndex = 4
                                    isPhaseDetailOpened = true
                                }
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // ROW 3: 3 Phases (Phase 6, Phase 7, Phase 8)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PhaseGridCard(
                                phase = GamePhases[5],
                                currentLevelId = currentLevelId,
                                completedLevels = completedLevels,
                                isPremium = isPremium,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedPhaseIndex = 5
                                    isPhaseDetailOpened = true
                                }
                            )
                            PhaseGridCard(
                                phase = GamePhases[6],
                                currentLevelId = currentLevelId,
                                completedLevels = completedLevels,
                                isPremium = isPremium,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedPhaseIndex = 6
                                    isPhaseDetailOpened = true
                                }
                            )
                            PhaseGridCard(
                                phase = GamePhases[7],
                                currentLevelId = currentLevelId,
                                completedLevels = completedLevels,
                                isPremium = isPremium,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedPhaseIndex = 7
                                    isPhaseDetailOpened = true
                                }
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // ROW 4: 2 Phases (Phase 9, Phase 10)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PhaseGridCard(
                                phase = GamePhases[8],
                                currentLevelId = currentLevelId,
                                completedLevels = completedLevels,
                                isPremium = isPremium,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedPhaseIndex = 8
                                    isPhaseDetailOpened = true
                                }
                            )
                            PhaseGridCard(
                                phase = GamePhases[9],
                                currentLevelId = currentLevelId,
                                completedLevels = completedLevels,
                                isPremium = isPremium,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedPhaseIndex = 9
                                    isPhaseDetailOpened = true
                                }
                            )
                        }
                    }
                } else {
                    // =========================================================================
                    // 2. OPENED PHASE: ENTIRE SECTION DISPLAYS ALL 50 LEVELS OF THIS PHASE
                    // =========================================================================
                    val phaseLevels = (currentPhase.startLevel..currentPhase.endLevel).toList()
                    val completedInPhase = phaseLevels.count { completedLevels.contains(it) || levelProgressMap[it]?.isCompleted == true }
                    val starsInPhase = phaseLevels.sumOf { levelProgressMap[it]?.stars ?: 0 }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        // Stage Info Card Header
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF111827).copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, profile.accentGlow.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(profile.iconSymbol, fontSize = 22.sp)
                                    Column {
                                        Text(
                                            text = currentPhase.title,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = currentPhase.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$completedInPhase/50 Done",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = profile.accentGlow
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldStar, modifier = Modifier.size(12.dp))
                                        Text(
                                            text = "$starsInPhase",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = GoldStar
                                        )
                                    }
                                }
                            }
                        }

                        // 50 Levels Grid (Full Screen Level Section)
                        AnimatedContent(
                            targetState = currentPhase.phaseNumber,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                                            slideOutHorizontally { width -> -width } + fadeOut()
                                } else {
                                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                            slideOutHorizontally { width -> width } + fadeOut()
                                }
                            },
                            label = "PhaseGridSectionTransition",
                            modifier = Modifier.fillMaxSize()
                        ) { _ ->
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(phaseLevels) { levelId ->
                                    val progress = levelProgressMap[levelId]
                                    val isCompleted = completedLevels.contains(levelId) || (progress?.isCompleted == true)
                                    val starsEarned = progress?.stars ?: 0
                                    val maxUnlocked = maxOf(currentLevelId, (completedLevels.maxOrNull() ?: 1))
                                    val isCurrent = levelId == maxUnlocked && !isCompleted
                                    val isUnlocked = isPremium || levelId <= maxUnlocked || isCompleted || levelId == 1

                                    FullSectionLevelNode(
                                        levelId = levelId,
                                        isCurrent = isCurrent,
                                        isCompleted = isCompleted,
                                        isUnlocked = isUnlocked,
                                        starsEarned = starsEarned,
                                        profile = profile,
                                        isGooglyMode = isGooglyMode,
                                        onClick = {
                                            if (isUnlocked) {
                                                onLevelSelected(levelId)
                                            } else {
                                                onPremiumClick()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Card for Phase in the 2×3×3×2 Matrix
 */
@Composable
private fun PhaseGridCard(
    phase: GamePhase,
    currentLevelId: Int,
    completedLevels: Set<Int>,
    isPremium: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val profile = StageProfiles.getProfile(phase.phaseNumber)
    val phaseLevels = phase.startLevel..phase.endLevel
    val completedCount = phaseLevels.count { completedLevels.contains(it) }
    val maxUnlocked = maxOf(currentLevelId, (completedLevels.maxOrNull() ?: 1))
    val isPhaseUnlocked = isPremium || maxUnlocked >= phase.startLevel || phase.phaseNumber == 1

    val borderGlow = if (isPhaseUnlocked) profile.primaryColor.copy(alpha = 0.5f) else Color(0xFF1E293B)

    val backgroundBrush = Brush.linearGradient(
        listOf(
            Color(0xFF131D2E).copy(alpha = 0.9f),
            Color(0xFF0A0F1D).copy(alpha = 0.9f)
        )
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderGlow),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundBrush)
            .clickable { onClick() }
            .testTag("phase_card_${phase.phaseNumber}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Phase Badge
                Surface(
                    shape = CircleShape,
                    color = profile.primaryColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${phase.phaseNumber}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = profile.accentGlow
                        )
                    }
                }

                if (!isPhaseUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(11.dp)
                    )
                } else if (completedCount >= 50) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Stage Glyphs Symbol & Title
            Text(
                text = profile.iconSymbol,
                fontSize = 16.sp
            )

            Text(
                text = phase.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${phase.startLevel}-${phase.endLevel}",
                fontSize = 8.5.sp,
                color = profile.accentGlow,
                textAlign = TextAlign.Center
            )

            // Mini completion bar
            LinearProgressIndicator(
                progress = { (completedCount / 50f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = profile.accentGlow,
                trackColor = Color(0xFF1E293B)
            )
        }
    }
}

/**
 * Full Section Level Node (In the 50 Levels Grid of opened Phase)
 */
@Composable
private fun FullSectionLevelNode(
    levelId: Int,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    starsEarned: Int,
    profile: StageVisualProfile,
    isGooglyMode: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "FullNodePressScale"
    )

    val nodeBackground = when {
        isCurrent -> {
            if (isGooglyMode) {
                Brush.radialGradient(listOf(Color(0xFFFF007F), Color(0xFF7000FF)))
            } else {
                Brush.radialGradient(listOf(profile.primaryColor, profile.secondaryColor))
            }
        }
        isCompleted -> {
            Brush.radialGradient(listOf(profile.primaryColor.copy(alpha = 0.35f), Color(0xFF1E293B)))
        }
        isUnlocked -> {
            Brush.radialGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
        }
        else -> {
            Brush.radialGradient(listOf(Color(0xFF0F172A).copy(alpha = 0.5f), Color(0xFF0B1120)))
        }
    }

    val borderStroke = when {
        isCurrent -> androidx.compose.foundation.BorderStroke(2.dp, Color.White)
        isCompleted -> androidx.compose.foundation.BorderStroke(1.dp, profile.accentGlow.copy(alpha = 0.7f))
        isUnlocked -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        else -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = borderStroke,
        shadowElevation = if (isCurrent) 6.dp else 1.dp,
        modifier = Modifier
            .aspectRatio(1f)
            .scale(pressScale)
            .clip(RoundedCornerShape(12.dp))
            .background(nodeBackground)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .testTag("level_node_$levelId")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$levelId",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        ),
                        color = if (isCurrent) Color.White else if (isCompleted) profile.accentGlow else Color.White
                    )

                    // Stars Earned
                    if (isCompleted && starsEarned > 0) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            for (s in 1..minOf(starsEarned, 3)) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = GoldStar,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
