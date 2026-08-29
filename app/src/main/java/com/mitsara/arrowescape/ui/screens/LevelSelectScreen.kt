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
    val difficultyTabs = listOf("Easy (1-50)", "Normal (51-150)", "Hard (151-300)", "Expert (301-500)")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val levelRange = when (selectedTabIndex) {
        0 -> 1..50
        1 -> 51..150
        2 -> 151..300
        else -> 301..500
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Select Level",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(28.dp))
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
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SurfaceLight,
                contentColor = PrimaryBlue,
                divider = { Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFFE2E8F0))) }
            ) {
                difficultyTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title.substringBefore(" "),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                                )
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
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            isCurrent -> PrimaryBlue
                            isCompleted -> Color(0xFFE0F7FA) // Light Cyan for completed
                            isUnlocked -> Color.White
                            else -> Color(0xFFF1F5F9)
                        },
                        shadowElevation = if (isUnlocked && !isCurrent && !isCompleted) 4.dp else if(isCurrent) 8.dp else 0.dp,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
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
                                    tint = TextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$levelId",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = when {
                                            isCurrent -> Color.White
                                            isCompleted -> Color(0xFF00796B)
                                            else -> TextPrimary
                                        }
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
                                                    modifier = Modifier.size(14.dp)
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

