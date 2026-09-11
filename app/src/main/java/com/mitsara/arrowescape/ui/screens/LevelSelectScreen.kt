package com.mitsara.arrowescape.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.data.LevelProgressEntity
import com.mitsara.arrowescape.ui.components.AdBannerView
import com.mitsara.arrowescape.ui.theme.GoldStar

import com.mitsara.arrowescape.ui.theme.HeartEmptyGray
import com.mitsara.arrowescape.ui.theme.PrimaryBlue
import com.mitsara.arrowescape.ui.theme.SurfaceCard
import com.mitsara.arrowescape.ui.theme.SurfaceLight
import com.mitsara.arrowescape.ui.theme.TextPrimary
import com.mitsara.arrowescape.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectScreen(
    currentLevelId: Int,
    completedLevels: Set<Int>,
    levelProgressMap: Map<Int, LevelProgressEntity> = emptyMap(),
    isPremium: Boolean = false,
    onPremiumClick: () -> Unit = {},
    onLevelSelected: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val chapterTabs = listOf(
        "Ch 1: Everyday (1-50)",
        "Ch 2: Botany & Arts (51-100)",
        "Ch 3: Maritime (101-150)",
        "Ch 4: Relics (151-200)",
        "Ch 5: Velocity (201-250)",
        "Ch 6: Sorcery (251-300)",
        "Ch 7: Crafts (301-350)",
        "Ch 8: Geometry (351-400)",
        "Ch 9: Cosmos (401-450)",
        "Ch 10: Apex (451-500)"
    )
    var selectedTabIndex by remember { mutableIntStateOf((currentLevelId - 1) / 50) }

    val levelRange = remember(selectedTabIndex) {
        val start = selectedTabIndex * 50 + 1
        val end = minOf(500, (selectedTabIndex + 1) * 50)
        start..end
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "500 Level Roadmap",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "10 Worlds • Handcrafted Silhouettes",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B), modifier = Modifier.size(24.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF7EE))
            )
        },
        containerColor = Color(0xFFFAF7EE)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            androidx.compose.material3.ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFFFAF7EE),
                contentColor = Color(0xFFC5953C),
                edgePadding = 16.dp,
                divider = { Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFFE2D6C0))) }
            ) {
                chapterTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selectedTabIndex == index) Color(0xFF4A3525) else Color(0xFF78716C)
                            )
                        }
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(levelRange.count()) { idx ->
                    val levelId = levelRange.first + idx
                    val progress = levelProgressMap[levelId]
                    val isCompleted = completedLevels.contains(levelId) || (progress?.isCompleted == true)
                    val starsEarned = progress?.stars ?: 0
                    val isCurrent = levelId == currentLevelId
                    val maxUnlocked = maxOf(currentLevelId, (completedLevels.maxOrNull() ?: 1))
                    val isUnlocked = isPremium || levelId <= maxUnlocked || isCompleted || levelId == 1

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = when {
                            isCurrent -> Color(0xFFC5953C)
                            isCompleted -> Color(0xFFE8F5E9)
                            isUnlocked -> Color.White
                            else -> Color(0xFFEDE8DC)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                isCurrent -> Color(0xFFB4852F)
                                isCompleted -> Color(0xFFA5D6A7)
                                isUnlocked -> Color(0xFFE2D6C0)
                                else -> Color.Transparent
                            }
                        ),
                        shadowElevation = if (isUnlocked && !isCurrent && !isCompleted) 2.dp else if (isCurrent) 4.dp else 0.dp,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                if (isUnlocked) {
                                    onLevelSelected(levelId)
                                } else {
                                    onPremiumClick()
                                }
                            }
                            .testTag("level_item_$levelId")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (!isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked Level",
                                    tint = Color(0xFFA8A29E),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    val levelDef = remember(levelId) { com.mitsara.arrowescape.engine.SilhouetteShapeRegistry.getLevelDef(levelId) }
                                    Text(
                                        text = "$levelId",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp
                                        ),
                                        color = when {
                                            isCurrent -> Color.White
                                            isCompleted -> Color(0xFF2E7D32)
                                            else -> Color(0xFF4A3525)
                                        }
                                    )
                                    Text(
                                        text = levelDef.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = when {
                                            isCurrent -> Color.White.copy(alpha = 0.9f)
                                            isCompleted -> Color(0xFF388E3C)
                                            else -> Color(0xFF78716C)
                                        },
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    if (isCompleted) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            for (s in 1..3) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (s <= starsEarned) GoldStar else HeartEmptyGray,
                                                    modifier = Modifier.size(13.dp)
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

            AdBannerView(
                isPremium = isPremium,
                onRemoveAdsClick = onPremiumClick
            )
        }
    }
}

