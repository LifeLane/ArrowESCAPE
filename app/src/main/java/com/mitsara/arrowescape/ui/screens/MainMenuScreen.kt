package com.mitsara.arrowescape.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.ui.components.AdBannerView
import com.mitsara.arrowescape.ui.motion.AnimatedAtmosphericBackground
import com.mitsara.arrowescape.ui.motion.AnimatedHeroButton
import com.mitsara.arrowescape.ui.motion.AnimatedMenuCard
import com.mitsara.arrowescape.ui.motion.AppThemeTokens
import com.mitsara.arrowescape.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MainMenuScreen(
    currentLevelId: Int,
    totalStars: Int,
    isPremium: Boolean,
    selectedTheme: String = "LIGHT",
    onToggleGooglyTheme: (() -> Unit)? = null,
    onPlayClick: () -> Unit,
    onLevelSelectClick: () -> Unit,
    onDailyChallengeClick: () -> Unit,
    onStatsClick: () -> Unit,
    onStoreClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val isGooglyMode = selectedTheme.equals("GOOGLY", ignoreCase = true)

    AnimatedAtmosphericBackground(
        isGooglyMode = isGooglyMode,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==========================================
            // 1. TOP BAR (Stars, Googly Toggle, Premium, Settings)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Frosted Glass Star Pill
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isGooglyMode) Color(0xFFFFB800).copy(alpha = 0.6f) else Color(0xFF334155)
                    ),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldStar,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$totalStars Stars",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // Action Buttons Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Googly Theme Mode Quick Toggle Pill
                    Surface(
                        color = if (isGooglyMode) Color(0xFFFF007F).copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isGooglyMode) Color(0xFFFF007F) else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onToggleGooglyTheme?.invoke() }
                            .testTag("theme_mode_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Theme Mode",
                                tint = if (isGooglyMode) Color(0xFFFF00CC) else Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGooglyMode) "GOOGLY" else "THEME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isGooglyMode) Color(0xFFFF77DD) else Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Premium Crown / Star
                    Surface(
                        color = if (isPremium) GoldStar.copy(alpha = 0.2f) else Color(0xFF1E293B).copy(alpha = 0.85f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPremium) GoldStar else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onPremiumClick() }
                            .testTag("premium_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Premium",
                                tint = if (isPremium) GoldStar else HintGlowColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Settings
                    Surface(
                        color = Color(0xFF1E293B).copy(alpha = 0.85f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onSettingsClick() }
                            .testTag("menu_settings_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFFE2E8F0),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 2. CENTER HERO LOGO & TITLE
            // ==========================================
            HeroBrandingLogo(isGooglyMode = isGooglyMode)

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 3. PRIMARY CTA: CONTINUE / PLAY BUTTON
            // ==========================================
            AnimatedHeroButton(
                text = if (currentLevelId > 1) "CONTINUE LEVEL $currentLevelId" else "PLAY GAME",
                onClick = onPlayClick,
                isGooglyMode = isGooglyMode,
                accentColor = Color(0xFF2563EB),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                testTag = "play_button"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 4. POLISHED INTERACTIVE MENU CARDS
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Level Roadmap / Selection
                AnimatedMenuCard(
                    title = "LEVEL ROADMAP",
                    subtitle = "10 Worlds • 500 Unique Stages",
                    icon = Icons.Default.GridOn,
                    iconTint = if (isGooglyMode) Color(0xFF00E5FF) else Color(0xFF0EA5E9),
                    onClick = onLevelSelectClick,
                    isGooglyMode = isGooglyMode,
                    badgeText = "500 LVLS",
                    testTag = "level_select_button"
                )

                // Daily Challenge
                AnimatedMenuCard(
                    title = "DAILY PUZZLE",
                    subtitle = "Daily escape modifier with bonus stars",
                    icon = Icons.Default.DateRange,
                    iconTint = if (isGooglyMode) Color(0xFFFFB800) else Color(0xFFF59E0B),
                    onClick = onDailyChallengeClick,
                    isGooglyMode = isGooglyMode,
                    badgeText = "STREAK",
                    testTag = "daily_challenge_button"
                )

                // Statistics & Profile
                AnimatedMenuCard(
                    title = "STATISTICS & PROFILE",
                    subtitle = "Escaped arrows, accuracy & best times",
                    icon = Icons.Default.Leaderboard,
                    iconTint = if (isGooglyMode) Color(0xFF00FF66) else Color(0xFF10B981),
                    onClick = onStatsClick,
                    isGooglyMode = isGooglyMode,
                    testTag = "stats_button"
                )

                // Cosmetic Store
                AnimatedMenuCard(
                    title = "ARROW SKINS & STORE",
                    subtitle = "Unlock laser darts, woodcraft & neon themes",
                    icon = Icons.Default.ShoppingBag,
                    iconTint = if (isGooglyMode) Color(0xFFFF007F) else Color(0xFFEC4899),
                    onClick = onStoreClick,
                    isGooglyMode = isGooglyMode,
                    badgeText = "NEW",
                    testTag = "store_button"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ad Banner View (Hidden if Premium)
            AdBannerView(
                isPremium = isPremium,
                onRemoveAdsClick = onPremiumClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // 5. FOOTER
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAboutClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About & Privacy",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "Arrow Escape v1.0.0 • Offline Ready",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

/**
 * Center Branding Component with Breathing Glow, Orbiting Arrow Lights, and Shimmer Title
 */
@Composable
private fun HeroBrandingLogo(isGooglyMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "LogoMotion")

    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreatheScale"
    )

    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitAngle"
    )

    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTitle"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Glowing Center Emblem
        Box(
            modifier = Modifier
                .size(110.dp)
                .scale(breatheScale),
            contentAlignment = Alignment.Center
        ) {
            // Orbiting Mini Laser Arrow Particle
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val orbitRadius = 52.dp.toPx()

                // Outer ambient glow ring
                val glowBrush = if (isGooglyMode) {
                    Brush.sweepGradient(AppThemeTokens.GooglyRainbowPalette, center = center)
                } else {
                    Brush.sweepGradient(
                        listOf(Color(0xFF00E5FF), Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFF00E5FF)),
                        center = center
                    )
                }

                drawCircle(
                    brush = glowBrush,
                    radius = orbitRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), orbitAngle * 2f))
                )

                // Orbiting Arrowhead
                val rad = orbitAngle * PI.toFloat() / 180f
                val ox = center.x + cos(rad) * orbitRadius
                val oy = center.y + sin(rad) * orbitRadius
                val arrowHeadColor = if (isGooglyMode) Color(0xFFFF007F) else Color(0xFF00E5FF)

                rotate(degrees = orbitAngle + 90f, pivot = Offset(ox, oy)) {
                    val p = Path().apply {
                        moveTo(ox, oy - 6.dp.toPx())
                        lineTo(ox + 5.dp.toPx(), oy + 4.dp.toPx())
                        lineTo(ox, oy + 2.dp.toPx())
                        lineTo(ox - 5.dp.toPx(), oy + 4.dp.toPx())
                        close()
                    }
                    drawPath(path = p, color = Color.White)
                    drawCircle(color = arrowHeadColor.copy(alpha = 0.7f), radius = 8.dp.toPx(), center = Offset(ox, oy))
                }
            }

            // Core Emblem Badge
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (isGooglyMode) {
                                listOf(Color(0xFF2E1065), Color(0xFF0F0728))
                            } else {
                                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            }
                        )
                    )
                    .shadow(12.dp, CircleShape, ambientColor = Color(0xFF00E5FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = if (isGooglyMode) Color(0xFFFF00CC) else Color(0xFF38BDF8),
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title: ARROW ESCAPE with Shimmer / Layered Typography
        Box(contentAlignment = Alignment.Center) {
            val titleGradient = if (isGooglyMode) {
                AppThemeTokens.getAnimatedRainbowGradient(shimmerProgress)
            } else {
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFE2E8F0),
                        Color(0xFF38BDF8),
                        Color.White
                    ),
                    startX = shimmerProgress * 800f - 400f,
                    endX = shimmerProgress * 800f + 400f
                )
            }

            Text(
                text = "ARROW ESCAPE",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    brush = titleGradient
                ),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle: Tap clear arrows. Free the board.
        Surface(
            color = Color(0xFF1E293B).copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Tap clear arrows. Free the board.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp
                ),
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )
        }
    }
}
