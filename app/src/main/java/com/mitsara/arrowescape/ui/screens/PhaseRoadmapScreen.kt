package com.mitsara.arrowescape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.data.LevelProgressEntity
import com.mitsara.arrowescape.ui.components.AdBannerView
import com.mitsara.arrowescape.ui.theme.*

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
    GamePhase(4, "Quantum Core", "Directional Gate Locks", 151, 200, Color(0xFFEC4899), Color(0xFFBE185D)),
    GamePhase(5, "Zenith Spire", "Cascading Multi-directional Paths", 201, 250, Color(0xFFF59E0B), Color(0xFFB45309)),
    GamePhase(6, "Synthwave Horizon", "Synchronized Switches", 251, 300, Color(0xFF10B981), Color(0xFF047857)),
    GamePhase(7, "Amethyst Void", "High-density Obstacles", 301, 350, Color(0xFF6366F1), Color(0xFF4338CA)),
    GamePhase(8, "Starlight Nexus", "Serpent Arrows & Modifiers", 351, 400, Color(0xFF14B8A6), Color(0xFF0F766E)),
    GamePhase(9, "Astral Dimension", "Maximum Occlusion Thresholds", 401, 450, Color(0xFFF97316), Color(0xFFC2410C)),
    GamePhase(10, "Nirvana Singularity", "Master-Tier Ultimate Escape", 451, 500, Color(0xFF8B5CF6), Color(0xFF4C1D95))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhaseRoadmapScreen(
    currentLevelId: Int,
    completedLevels: Set<Int>,
    levelProgressMap: Map<Int, LevelProgressEntity> = emptyMap(),
    isPremium: Boolean = false,
    onPremiumClick: () -> Unit = {},
    onLevelSelected: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedPhaseIndex by remember { mutableIntStateOf((currentLevelId - 1) / 50) }
    val currentPhase = GamePhases.getOrElse(selectedPhaseIndex) { GamePhases[0] }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Level Roadmap (500 Levels)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Phase ${currentPhase.phaseNumber}: ${currentPhase.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = SurfaceLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Horizontal Phase Selector Bar (Candy Crush Style World Selector)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GamePhases.forEachIndexed { index, phase ->
                    val isSelected = selectedPhaseIndex == index
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) phase.primaryColor else SurfaceLight,
                        shadowElevation = if (isSelected) 4.dp else 0.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedPhaseIndex = index }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(if (isSelected) Color.White.copy(alpha = 0.3f) else phase.primaryColor.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${phase.phaseNumber}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else phase.primaryColor
                                )
                            }
                            Column {
                                Text(
                                    text = phase.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                                Text(
                                    text = "Lvl ${phase.startLevel}-${phase.endLevel}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Winding Path Level Nodes Grid / List (Candy Crush Roadmap Layout)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Phase Header Card
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Brush.horizontalGradient(listOf(currentPhase.primaryColor, currentPhase.secondaryColor)), RoundedCornerShape(24.dp))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text(
                                    text = "PHASE ${currentPhase.phaseNumber}",
                                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentPhase.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentPhase.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 50 Levels winding nodes in pairs or triplets
                val levels = (currentPhase.startLevel..currentPhase.endLevel).toList()
                items(levels.chunked(3)) { rowLevels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowLevels.forEach { levelId ->
                            val progress = levelProgressMap[levelId]
                            val isCompleted = completedLevels.contains(levelId) || (progress?.isCompleted == true)
                            val starsEarned = progress?.stars ?: 0
                            val isCurrent = levelId == currentLevelId
                            val maxUnlocked = maxOf(currentLevelId, (completedLevels.maxOrNull() ?: 1))
                            val isUnlocked = isPremium || levelId <= maxUnlocked || isCompleted || levelId == 1

                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> currentPhase.primaryColor
                                            isCompleted -> currentPhase.primaryColor.copy(alpha = 0.2f)
                                            isUnlocked -> Color.White
                                            else -> Color(0xFFE2E8F0)
                                        }
                                    )
                                    .clickable {
                                        if (isUnlocked) {
                                            onLevelSelected(levelId)
                                        } else {
                                            onPremiumClick()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(24.dp)
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
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "$levelId",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isCompleted) currentPhase.secondaryColor else TextPrimary
                                            )
                                        }

                                        if (isCompleted) {
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                for (s in 1..minOf(starsEarned, 3)) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = GoldStar,
                                                        modifier = Modifier.size(12.dp)
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
            }

            AdBannerView(
                isPremium = isPremium,
                onRemoveAdsClick = onPremiumClick
            )
        }
    }
}
