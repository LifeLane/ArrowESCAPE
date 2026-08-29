package com.mitsara.arrowescape.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.data.LevelProgressEntity
import com.mitsara.arrowescape.ui.components.AdBannerView
import com.mitsara.arrowescape.ui.motion.AppThemeTokens
import com.mitsara.arrowescape.ui.motion.StageEnvironmentCanvas
import com.mitsara.arrowescape.ui.motion.StageProfiles
import com.mitsara.arrowescape.ui.motion.StageVisualProfile
import com.mitsara.arrowescape.ui.theme.GoldStar
import kotlin.math.PI
import kotlin.math.sin

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
    val currentPhase = GamePhases.getOrElse(selectedPhaseIndex) { GamePhases[0] }
    val profile = StageProfiles.getProfile(currentPhase.phaseNumber)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070913))
    ) {
        // 1. Stage Environmental Procedural Canvas (Smooth Crossfade on stage change)
        AnimatedContent(
            targetState = currentPhase.phaseNumber,
            transitionSpec = {
                fadeIn(animationSpec = tween(500, easing = LinearEasing)) togetherWith
                        fadeOut(animationSpec = tween(500, easing = LinearEasing))
            },
            label = "StageCanvasTransition"
        ) { stageNum ->
            StageEnvironmentCanvas(
                stageNumber = stageNum,
                isGooglyMode = isGooglyMode,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Foreground Roadmap Navigation & Level Grid
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "LEVEL ROADMAP",
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
                                        text = "500 LEVELS",
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
                                text = "Stage ${currentPhase.phaseNumber} • ${currentPhase.title}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = profile.accentGlow
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070913).copy(alpha = 0.85f))
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ==========================================
                // 3. HORIZONTAL STAGE SELECTOR TABS (Connected to Stage Identities)
                // ==========================================
                val scrollState = rememberScrollState()
                LaunchedEffect(selectedPhaseIndex) {
                    scrollState.animateScrollTo((selectedPhaseIndex * 110).dp.value.toInt())
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0E1322).copy(alpha = 0.9f))
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GamePhases.forEachIndexed { index, phase ->
                        val isSelected = selectedPhaseIndex == index
                        val pProfile = StageProfiles.getProfile(phase.phaseNumber)

                        val tabBg = if (isSelected) {
                            if (isGooglyMode) {
                                Brush.horizontalGradient(listOf(Color(0xFFFF007F), Color(0xFF00E5FF)))
                            } else {
                                Brush.horizontalGradient(listOf(pProfile.primaryColor, pProfile.secondaryColor))
                            }
                        } else {
                            Brush.linearGradient(listOf(Color(0xFF1E293B).copy(alpha = 0.6f), Color(0xFF1E293B).copy(alpha = 0.6f)))
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) pProfile.accentGlow else Color(0xFF334155).copy(alpha = 0.6f)
                            ),
                            shadowElevation = if (isSelected) 8.dp else 0.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(tabBg)
                                .clickable { selectedPhaseIndex = index }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (isSelected) Color.White.copy(alpha = 0.25f) else pProfile.primaryColor.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${phase.phaseNumber}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black, fontSize = 11.sp),
                                        color = if (isSelected) Color.White else pProfile.accentGlow
                                    )
                                }

                                Column {
                                    Text(
                                        text = phase.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = if (isSelected) Color.White else Color(0xFFE2E8F0)
                                    )
                                    Text(
                                        text = "Lvl ${phase.startLevel}-${phase.endLevel}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. WINDING ROADMAP LEVEL NODES (50 Levels per stage)
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Stage Hero Banner Card
                    item {
                        StageHeroBannerCard(
                            phase = currentPhase,
                            profile = profile,
                            isGooglyMode = isGooglyMode
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 50 Level Nodes laid out in staggered zigzag rows
                    val levels = (currentPhase.startLevel..currentPhase.endLevel).toList()
                    val rows = levels.chunked(3)

                    itemsIndexed(rows) { rowIndex, rowLevels ->
                        RoadmapRow(
                            rowIndex = rowIndex,
                            rowLevels = rowLevels,
                            currentLevelId = currentLevelId,
                            completedLevels = completedLevels,
                            levelProgressMap = levelProgressMap,
                            isPremium = isPremium,
                            profile = profile,
                            isGooglyMode = isGooglyMode,
                            onLevelSelected = onLevelSelected,
                            onPremiumClick = onPremiumClick
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Ad Banner View (Hidden if Premium)
                AdBannerView(
                    isPremium = isPremium,
                    onRemoveAdsClick = onPremiumClick
                )
            }
        }
    }
}

@Composable
private fun StageHeroBannerCard(
    phase: GamePhase,
    profile: StageVisualProfile,
    isGooglyMode: Boolean
) {
    val cardBrush = if (isGooglyMode) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFFF007F).copy(alpha = 0.85f),
                Color(0xFF00E5FF).copy(alpha = 0.85f),
                Color(0xFF7000FF).copy(alpha = 0.85f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                profile.primaryColor.copy(alpha = 0.85f),
                profile.secondaryColor.copy(alpha = 0.85f)
            )
        )
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, profile.accentGlow.copy(alpha = 0.5f)),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBrush)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "STAGE ${phase.phaseNumber} • ${profile.subtitle.uppercase()}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        ),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = phase.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = phase.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // Stage Glyphs Symbol
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = profile.iconSymbol,
                            fontSize = 26.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoadmapRow(
    rowIndex: Int,
    rowLevels: List<Int>,
    currentLevelId: Int,
    completedLevels: Set<Int>,
    levelProgressMap: Map<Int, LevelProgressEntity>,
    isPremium: Boolean,
    profile: StageVisualProfile,
    isGooglyMode: Boolean,
    onLevelSelected: (Int) -> Unit,
    onPremiumClick: () -> Unit
) {
    // Alternate row alignment for organic winding path feel
    val isReversed = rowIndex % 2 != 0
    val orderedLevels = if (isReversed) rowLevels.reversed() else rowLevels

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        orderedLevels.forEach { levelId ->
            val progress = levelProgressMap[levelId]
            val isCompleted = completedLevels.contains(levelId) || (progress?.isCompleted == true)
            val starsEarned = progress?.stars ?: 0
            val isCurrent = levelId == currentLevelId
            val maxUnlocked = maxOf(currentLevelId, (completedLevels.maxOrNull() ?: 1))
            val isUnlocked = isPremium || levelId <= maxUnlocked || isCompleted || levelId == 1

            AnimatedLevelNode(
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

/**
 * Individual Animated Level Node
 */
@Composable
private fun AnimatedLevelNode(
    levelId: Int,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    starsEarned: Int,
    profile: StageVisualProfile,
    isGooglyMode: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NodePulse_$levelId")

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlow"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "NodePressScale"
    )

    val nodeSize = 68.dp

    Box(
        modifier = Modifier
            .size(nodeSize + 16.dp)
            .scale(if (isCurrent) pulseGlow * pressScale else pressScale),
        contentAlignment = Alignment.Center
    ) {
        // 1. Radar Focus Ring for Current Level
        if (isCurrent) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val focusRadius = (nodeSize.toPx() / 2f) + 6.dp.toPx()
                val ringColor = if (isGooglyMode) Color(0xFFFF007F) else profile.accentGlow

                drawCircle(
                    color = ringColor.copy(alpha = 0.35f),
                    radius = focusRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // 2. Main Node Circle Body
        val nodeBackground = when {
            isCurrent -> {
                if (isGooglyMode) {
                    Brush.radialGradient(listOf(Color(0xFFFF007F), Color(0xFF7000FF)))
                } else {
                    Brush.radialGradient(listOf(profile.primaryColor, profile.secondaryColor))
                }
            }
            isCompleted -> {
                if (isGooglyMode) {
                    Brush.radialGradient(listOf(Color(0xFF00FF66).copy(alpha = 0.3f), Color(0xFF1E293B)))
                } else {
                    Brush.radialGradient(listOf(profile.primaryColor.copy(alpha = 0.35f), Color(0xFF1E293B)))
                }
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
            isCompleted -> androidx.compose.foundation.BorderStroke(1.5.dp, profile.accentGlow)
            isUnlocked -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
            else -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        }

        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            border = borderStroke,
            shadowElevation = if (isCurrent) 10.dp else if (isCompleted) 4.dp else 0.dp,
            modifier = Modifier
                .size(nodeSize)
                .clip(CircleShape)
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
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Current Level",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        } else {
                            Text(
                                text = "$levelId",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                ),
                                color = if (isCompleted) profile.accentGlow else Color.White
                            )
                        }

                        // Stars Earned Pill
                        if (isCompleted && starsEarned > 0) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                for (s in 1..minOf(starsEarned, 3)) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = GoldStar,
                                        modifier = Modifier.size(11.dp)
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
