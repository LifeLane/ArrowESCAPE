package com.mitsara.arrowescape.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mitsara.arrowescape.data.LevelProgressEntity
import com.mitsara.arrowescape.engine.SilhouetteShapeRegistry
import com.mitsara.arrowescape.ui.motion.AnimatedAtmosphericBackground
import com.mitsara.arrowescape.ui.theme.GoldStar

val TIER_TITLES = listOf(
    "1. Natural Archetypes",
    "2. Cyber Geometric",
    "3. Ancient Relics",
    "4. Celestial Zodiac",
    "5. Oceanic Depths",
    "6. Contraptions",
    "7. Flora Sanctuary",
    "8. Mythic Beasts",
    "9. Cosmic Matrix",
    "10. Apex Grandmaster"
)

val TIER_LORE = listOf(
    "Sacred beasts, avians, and primordial totems etched in clean directional lines.",
    "Neon-infused silicon shapes and algorithmic network circuits.",
    "Ancient monoliths, pharaoh crowns, and Hellenic pottery preserved across millennia.",
    "Constellations and astrological coordinates charted by astral navigators.",
    "Submerged leviathans, coral clusters, and deep trench abyssal fauna.",
    "Clockwork chronometers, brass linkages, and Victorian automaton gears.",
    "Lotus blooms, sprawling canopy leaves, and spiraling botanical vines.",
    "Dragons, phoenix plumage, hydras, and legendary chimera crests.",
    "Quantum manifolds, tesseract projections, and hyper-dimensional matrices.",
    "The ultimate trial of topological deduction created for grandmaster tacticians."
)

@Composable
fun SilhouetteExhibitionScreen(
    completedLevels: Set<Int>,
    levelProgressMap: Map<Int, LevelProgressEntity>,
    onPlayLevel: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedTier by remember { mutableIntStateOf(1) }
    var inspectedLevelDef by remember { mutableStateOf<SilhouetteShapeRegistry.SilhouetteLevelDef?>(null) }

    val startLevel = (selectedTier - 1) * 50 + 1
    val endLevel = selectedTier * 50
    val tierLevels = remember(selectedTier) {
        (startLevel..endLevel).map { SilhouetteShapeRegistry.getLevelDef(it) }
    }

    val unlockedCountInTier = remember(selectedTier, completedLevels) {
        (startLevel..endLevel).count { completedLevels.contains(it) }
    }

    AnimatedAtmosphericBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                        .testTag("exhibition_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "EXHIBITION HALL",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "500 Silhouette Artworks (${completedLevels.size}/500 Cleared)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF94A3B8)
                    )
                }

                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${unlockedCountInTier}/50",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            // Tier Selector Tabs (LazyRow)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TIER_TITLES.indices.toList()) { index ->
                    val tierNum = index + 1
                    val isSelected = selectedTier == tierNum
                    val tierCompleted = (1..50).count { completedLevels.contains((tierNum - 1) * 50 + it) }

                    Surface(
                        onClick = { selectedTier = tierNum },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B).copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF60A5FA) else Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("tier_tab_$tierNum")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = TIER_TITLES[index],
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$tierCompleted/50",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isSelected) Color(0xFFBAE6FD) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Tier Lore Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.65f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Text(
                    text = TIER_LORE.getOrElse(selectedTier - 1) { "" },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            // Silhouette Grid Display
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tierLevels) { def ->
                    val isUnlocked = completedLevels.contains(def.levelNumber)
                    val progress = levelProgressMap[def.levelNumber]
                    val stars = progress?.stars ?: 0

                    SilhouetteCard(
                        levelDef = def,
                        isUnlocked = isUnlocked,
                        stars = stars,
                        onClick = { inspectedLevelDef = def }
                    )
                }
            }
        }
    }

    // Modal Diorama Inspector Dialog
    inspectedLevelDef?.let { def ->
        val isUnlocked = completedLevels.contains(def.levelNumber)
        val progress = levelProgressMap[def.levelNumber]

        DioramaInspectorDialog(
            levelDef = def,
            isUnlocked = isUnlocked,
            stars = progress?.stars ?: 0,
            bestMoves = progress?.moveCount ?: 0,
            onDismiss = { inspectedLevelDef = null },
            onPlay = {
                inspectedLevelDef = null
                onPlayLevel(def.levelNumber)
            }
        )
    }
}

@Composable
fun SilhouetteCard(
    levelDef: SilhouetteShapeRegistry.SilhouetteLevelDef,
    isUnlocked: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF1E293B).copy(alpha = 0.85f) else Color(0xFF0F172A).copy(alpha = 0.7f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUnlocked) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF334155).copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .testTag("silhouette_card_${levelDef.levelNumber}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Stage Number Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${levelDef.levelNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isUnlocked) Color(0xFF38BDF8) else Color(0xFF64748B)
                )

                if (isUnlocked && stars > 0) {
                    Row {
                        repeat(stars) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldStar,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                } else if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Silhouette Mini Canvas Preview
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0B0F17)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val mask = SilhouetteShapeRegistry.generateShapeMask(levelDef.levelNumber, 10, 10)
                    val cellW = size.width / 10f
                    val cellH = size.height / 10f

                    for (x in 0 until 10) {
                        for (y in 0 until 10) {
                            if (mask[x][y]) {
                                drawRect(
                                    color = if (isUnlocked) Color(0xFF38BDF8).copy(alpha = 0.85f) else Color(0xFF334155).copy(alpha = 0.4f),
                                    topLeft = Offset(x * cellW + 0.5f, y * cellH + 0.5f),
                                    size = Size(cellW - 1f, cellH - 1f)
                                )
                            }
                        }
                    }
                }
            }

            // Shape Name
            Text(
                text = levelDef.name,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                color = if (isUnlocked) Color.White else Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DioramaInspectorDialog(
    levelDef: SilhouetteShapeRegistry.SilhouetteLevelDef,
    isUnlocked: Boolean,
    stars: Int,
    bestMoves: Int,
    onDismiss: () -> Unit,
    onPlay: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HoloPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close button & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STAGE #${levelDef.levelNumber}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF38BDF8)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Text(
                    text = levelDef.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // High-Fidelity Holographic Canvas Diorama
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1E3A8A).copy(alpha = 0.35f),
                                    Color(0xFF020617)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        val gridSize = 12
                        val mask = SilhouetteShapeRegistry.generateShapeMask(levelDef.levelNumber, gridSize, gridSize)
                        val cW = size.width / gridSize
                        val cH = size.height / gridSize

                        for (x in 0 until gridSize) {
                            for (y in 0 until gridSize) {
                                if (mask[x][y]) {
                                    val cellCenter = Offset(x * cW + cW / 2, y * cH + cH / 2)
                                    if (isUnlocked) {
                                        drawRoundRect(
                                            color = Color(0xFF38BDF8).copy(alpha = pulseAlpha),
                                            topLeft = Offset(x * cW + 1f, y * cH + 1f),
                                            size = Size(cW - 2f, cH - 2f),
                                            cornerRadius = CornerRadius(4f, 4f)
                                        )
                                        drawRoundRect(
                                            color = Color.White.copy(alpha = 0.8f),
                                            topLeft = Offset(x * cW + 1f, y * cH + 1f),
                                            size = Size(cW - 2f, cH - 2f),
                                            cornerRadius = CornerRadius(4f, 4f),
                                            style = Stroke(width = 1f)
                                        )
                                    } else {
                                        drawRoundRect(
                                            color = Color(0xFF334155).copy(alpha = 0.45f),
                                            topLeft = Offset(x * cW + 1f, y * cH + 1f),
                                            size = Size(cW - 2f, cH - 2f),
                                            cornerRadius = CornerRadius(4f, 4f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats & Inscription
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Status", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        Text(
                            text = if (isUnlocked) "CLEAR" else "UNSOLVED",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isUnlocked) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Stars", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        Text(
                            text = "$stars / 3 ★",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = GoldStar
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Best Moves", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        Text(
                            text = if (isUnlocked && bestMoves > 0) "$bestMoves" else "--",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Play / Replay Button
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("play_silhouette_level_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUnlocked) "REPLAY SILHOUETTE" else "SOLVE SILHOUETTE",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
