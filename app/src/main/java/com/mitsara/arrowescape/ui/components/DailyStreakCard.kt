package com.mitsara.arrowescape.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DailyRewardTier(
    val day: Int,
    val title: String,
    val coins: Int,
    val diamonds: Int = 0,
    val hints: Int = 0,
    val powerupIcon: String? = null,
    val powerupName: String? = null,
    val isGrandChest: Boolean = false
)

val DEFAULT_7_DAY_REWARDS = listOf(
    DailyRewardTier(day = 1, title = "Day 1", coins = 50, hints = 1),
    DailyRewardTier(day = 2, title = "Day 2", coins = 100, powerupIcon = "⚡", powerupName = "Laser"),
    DailyRewardTier(day = 3, title = "Day 3", coins = 150, powerupIcon = "🛡️", powerupName = "Shield"),
    DailyRewardTier(day = 4, title = "Day 4", coins = 200, diamonds = 5),
    DailyRewardTier(day = 5, title = "Day 5", coins = 250, powerupIcon = "🧲", powerupName = "Sonar"),
    DailyRewardTier(day = 6, title = "Day 6", coins = 300, diamonds = 10),
    DailyRewardTier(day = 7, title = "Day 7", coins = 500, diamonds = 25, hints = 5, powerupIcon = "👑", powerupName = "Grand Chest", isGrandChest = true)
)

/**
 * Visual Daily Streak component showing progress and upcoming milestone rewards on Home screen.
 */
@Composable
fun DailyStreakCard(
    currentStreak: Int,
    isClaimedToday: Boolean,
    onClaimReward: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDayInCycle = ((currentStreak - 1).coerceAtLeast(0) % 7) + 1
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFAF7EE),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2D6C0)),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_streak_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Flame, Streak Count, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFFEDD5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Daily Streak",
                            tint = Color(0xFFEA580C),
                            modifier = Modifier
                                .size(22.dp)
                                .scale(flameScale)
                        )
                    }

                    Column {
                        Text(
                            text = "$currentStreak DAY STREAK",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF4A3525)
                        )
                        Text(
                            text = if (isClaimedToday) "Streak active for today!" else "Claim today's milestone reward!",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (isClaimedToday) Color(0xFF16A34A) else Color(0xFFC2410C)
                        )
                    }
                }

                // Milestone Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Day $currentDayInCycle/7",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-Day Roadmap Track
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DEFAULT_7_DAY_REWARDS.forEach { tier ->
                    val isPast = tier.day < currentDayInCycle || (tier.day == currentDayInCycle && isClaimedToday)
                    val isToday = tier.day == currentDayInCycle && !isClaimedToday
                    val isUpcoming = tier.day > currentDayInCycle

                    DailyDayNode(
                        tier = tier,
                        isPast = isPast,
                        isToday = isToday,
                        isUpcoming = isUpcoming
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action / Status Bar
            if (!isClaimedToday) {
                Button(
                    onClick = onClaimReward,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("claim_daily_streak_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CLAIM DAY $currentDayInCycle REWARD",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDCFCE7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Next reward unlocked tomorrow in Day ${if (currentDayInCycle >= 7) 1 else currentDayInCycle + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyDayNode(
    tier: DailyRewardTier,
    isPast: Boolean,
    isToday: Boolean,
    isUpcoming: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = when {
            isToday -> Color(0xFFFFF7ED)
            isPast -> Color(0xFFF1F5F9)
            else -> Color(0xFFFAF7EE)
        },
        border = androidx.compose.foundation.BorderStroke(
            width = if (isToday) 2.dp else 1.dp,
            color = when {
                isToday -> Color(0xFFEA580C)
                isPast -> Color(0xFFCBD5E1)
                tier.isGrandChest -> Color(0xFFF59E0B)
                else -> Color(0xFFE2D6C0)
            }
        ),
        shadowElevation = if (isToday) 4.dp else 0.dp,
        modifier = Modifier.width(68.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Day label
            Text(
                text = "DAY ${tier.day}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = when {
                    isToday -> Color(0xFFC2410C)
                    isPast -> Color(0xFF64748B)
                    else -> Color(0xFF78716C)
                }
            )

            // Reward Visual Icon
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        when {
                            isPast -> Color(0xFFE2E8F0)
                            isToday -> Color(0xFFFFEDD5)
                            tier.isGrandChest -> Color(0xFFFEF3C7)
                            else -> Color(0xFFEDE8DC)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPast) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Claimed",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(16.dp)
                    )
                } else if (tier.powerupIcon != null) {
                    Text(text = tier.powerupIcon, fontSize = 14.sp)
                } else if (tier.diamonds > 0) {
                    Text(text = "💎", fontSize = 13.sp)
                } else {
                    Text(text = "🪙", fontSize = 13.sp)
                }
            }

            // Reward Value Text
            Text(
                text = when {
                    tier.isGrandChest -> "CHEST"
                    tier.diamonds > 0 -> "+${tier.diamonds}💎"
                    tier.powerupName != null -> "+1 ${tier.powerupName}"
                    tier.hints > 0 -> "+${tier.hints} 💡"
                    else -> "+${tier.coins}🪙"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isToday) Color(0xFFEA580C) else Color(0xFF4A3525),
                maxLines = 1
            )
        }
    }
}
