package com.mitsara.arrowescape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.mitsara.arrowescape.data.UserSettingsEntity
import com.mitsara.arrowescape.model.AchievementCategory
import com.mitsara.arrowescape.model.MasteryAchievement
import com.mitsara.arrowescape.model.MasteryRegistry
import com.mitsara.arrowescape.ui.motion.AnimatedAtmosphericBackground
import com.mitsara.arrowescape.ui.viewmodel.GameViewModel

@Composable
fun MasteryBadgesScreen(
    viewModel: GameViewModel,
    userSettings: UserSettingsEntity,
    completedLevelsCount: Int,
    onBackClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(AchievementCategory.DEDUCTION) }
    val claimedSet = remember(userSettings.claimedAchievements) {
        userSettings.claimedAchievements.split(",").filter { it.isNotBlank() }.toSet()
    }

    val filteredAchievements = remember(selectedCategory) {
        MasteryRegistry.ACHIEVEMENTS.filter { it.category == selectedCategory }
    }

    fun calculateProgress(achievement: MasteryAchievement): Pair<Int, Boolean> {
        val currentValue = when (achievement.id) {
            "ACH_PERFECTIONIST_1", "ACH_PERFECTIONIST_2" -> (completedLevelsCount / 2).coerceAtMost(achievement.targetValue)
            "ACH_CASCADE_MASTER" -> if (completedLevelsCount > 5) 6 else 2
            "ACH_BLINDFOLD" -> (completedLevelsCount / 3).coerceAtMost(achievement.targetValue)
            "ACH_DEPTH_CHAMPION" -> if (completedLevelsCount > 20) 20 else 8
            "ACH_TIER_1_MASTER" -> completedLevelsCount.coerceAtMost(50)
            "ACH_TIER_2_MASTER" -> maxOf(0, completedLevelsCount - 50).coerceAtMost(50)
            "ACH_SILHOUETTE_100" -> completedLevelsCount.coerceAtMost(100)
            "ACH_SILHOUETTE_250" -> completedLevelsCount.coerceAtMost(250)
            "ACH_GRANDMASTER_500" -> completedLevelsCount.coerceAtMost(500)
            "ACH_CHRONO_NOVICE", "ACH_CHRONO_PRO" -> userSettings.chronoHighScore.coerceAtMost(achievement.targetValue)
            "ACH_CHRONO_FRENZY" -> if (userSettings.chronoHighScore > 50) 8 else 3
            "ACH_ZEN_FLOW_TRANSCENDENCE" -> if (userSettings.zenTotalEscapes > 30) 4 else 2
            "ACH_ZEN_ESCAPE_100" -> userSettings.zenTotalEscapes.coerceAtMost(100)
            else -> 0
        }
        val isCompleted = currentValue >= achievement.targetValue
        return Pair(currentValue, isCompleted)
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
                        .testTag("mastery_back_button")
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
                        text = "MASTERY & TROPHIES",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "${claimedSet.size}/${MasteryRegistry.ACHIEVEMENTS.size} Badges Claimed",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF94A3B8)
                    )
                }

                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userSettings.diamonds}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF00E5FF)
                        )
                    }
                }
            }

            // Categories Selector Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AchievementCategory.entries) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B).copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF60A5FA) else Color(0xFF334155)
                        )
                    ) {
                        Text(
                            text = cat.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Achievements List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredAchievements) { achievement ->
                    val (currentValue, isComplete) = calculateProgress(achievement)
                    val isClaimed = claimedSet.contains(achievement.id)

                    AchievementCard(
                        achievement = achievement,
                        currentProgress = currentValue,
                        isComplete = isComplete,
                        isClaimed = isClaimed,
                        onClaim = {
                            viewModel.claimAchievement(achievement.id, achievement.diamondReward)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: MasteryAchievement,
    currentProgress: Int,
    isComplete: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isClaimed) Color(0xFF1E293B).copy(alpha = 0.6f) else Color(0xFF0F172A).copy(alpha = 0.85f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isClaimed) Color(0xFFFFD700).copy(alpha = 0.4f)
            else if (isComplete) Color(0xFF00E5FF).copy(alpha = 0.6f)
            else Color(0xFF334155).copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Emoji Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = achievement.iconEmoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info & Progress Bar
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Linear Progress Indicator
                val progressFraction = (currentProgress.toFloat() / achievement.targetValue.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (isComplete) Color(0xFF00E5FF) else Color(0xFF2563EB),
                    trackColor = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "$currentProgress / ${achievement.targetValue}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action: Claim / Complete / Claimed Status
            if (isClaimed) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CLAIMED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            } else if (isComplete) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${achievement.diamondReward}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0F172A)
                    )
                }
            } else {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+${achievement.diamondReward}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
