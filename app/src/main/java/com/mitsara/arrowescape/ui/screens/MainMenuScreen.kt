package com.mitsara.arrowescape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.ui.components.AdBannerView
import com.mitsara.arrowescape.ui.theme.AccentCyan

import com.mitsara.arrowescape.ui.theme.ArrowNavyDark
import com.mitsara.arrowescape.ui.theme.GoldStar
import com.mitsara.arrowescape.ui.theme.HintGlowColor
import com.mitsara.arrowescape.ui.theme.PrimaryBlue
import com.mitsara.arrowescape.ui.theme.SurfaceLight
import com.mitsara.arrowescape.ui.theme.TextPrimary
import com.mitsara.arrowescape.ui.theme.TextSecondary

@Composable
fun MainMenuScreen(
    currentLevelId: Int,
    totalStars: Int,
    isPremium: Boolean,
    onPlayClick: () -> Unit,
    onLevelSelectClick: () -> Unit,
    onDailyChallengeClick: () -> Unit,
    onStatsClick: () -> Unit,
    onStoreClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar Stars & Premium
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldStar, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$totalStars Stars",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onPremiumClick,
                        modifier = Modifier.testTag("premium_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = if (isPremium) GoldStar else HintGlowColor
                        )
                    }
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("menu_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                }
            }

            // Center Branding & Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(ArrowNavyDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ARROW ESCAPE",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = ArrowNavyDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Tap clear arrows. Free the board.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play / Continue Button
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("play_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (currentLevelId > 1) "CONTINUE (LEVEL $currentLevelId)" else "PLAY GAME",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Level Select Button
                OutlinedButton(
                    onClick = onLevelSelectClick,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("level_select_button")
                ) {
                    Icon(imageVector = Icons.Default.GridOn, contentDescription = null, tint = TextPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LEVEL SELECTION",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }

                // Daily Challenge Button
                OutlinedButton(
                    onClick = onDailyChallengeClick,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("daily_challenge_button")
                ) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = HintGlowColor)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "DAILY CHALLENGE",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }

                // Statistics Button
                OutlinedButton(
                    onClick = onStatsClick,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("stats_button")
                ) {
                    Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "STATISTICS & PROFILE",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }

                // Cosmetic Store Button
                OutlinedButton(
                    onClick = onStoreClick,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("store_button")
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldStar)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "COSMETIC STORE",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
            }

            // Ad Banner View (Hidden if Premium)
            AdBannerView(
                isPremium = isPremium,
                onRemoveAdsClick = onPremiumClick
            )

            // Bottom Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAboutClick) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "About & Privacy", tint = TextSecondary)
                }
                Text(
                    text = "v1.0.0 • Offline Ready",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                    color = TextSecondary
                )
            }
        }
    }
}
