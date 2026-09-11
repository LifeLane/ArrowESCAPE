package com.mitsara.arrowescape.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Real-time Combo Multiplier HUD Element.
 * Tracks and displays real-time combo multipliers with active time-decay gauge,
 * encouraging players to find the most efficient board-clearing sequences.
 */
@Composable
fun RealTimeComboHUD(
    comboMultiplier: Int,
    lastEscapeTimestamp: Long,
    comboWindowMs: Long = 3500L,
    activeComboMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val decayProgress = remember { Animatable(1f) }

    // When a new escape happens with a combo > 1, animate the decay gauge
    LaunchedEffect(lastEscapeTimestamp, comboMultiplier) {
        if (comboMultiplier > 1 && lastEscapeTimestamp > 0L) {
            val elapsed = System.currentTimeMillis() - lastEscapeTimestamp
            val remaining = (comboWindowMs - elapsed).coerceAtLeast(0L)
            val startFraction = (remaining.toFloat() / comboWindowMs).coerceIn(0f, 1f)

            decayProgress.snapTo(startFraction)
            decayProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = remaining.toInt(),
                    easing = LinearEasing
                )
            )
        } else {
            decayProgress.snapTo(0f)
        }
    }

    val isComboActive = comboMultiplier > 1 && decayProgress.value > 0.02f

    val infiniteTransition = rememberInfiniteTransition(label = "combo_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (comboMultiplier >= 4) 1.08f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val (badgeColors, badgeTitle, bonusText) = when (comboMultiplier) {
        5 -> Triple(
            listOf(Color(0xFFFF0055), Color(0xFFFF5500), Color(0xFFFFCC00)),
            "5x FRENZY",
            "+POWERUP • +300% PTS"
        )
        4 -> Triple(
            listOf(Color(0xFFF97316), Color(0xFFFB923C), Color(0xFFFDE047)),
            "4x SURGE",
            "+200% SCORE BOOST"
        )
        3 -> Triple(
            listOf(Color(0xFFEAB308), Color(0xFFFACC15), Color(0xFFFEF08A)),
            "3x STREAK",
            "+10 🪙 BONUS COINS"
        )
        2 -> Triple(
            listOf(Color(0xFF38BDF8), Color(0xFF0EA5E9), Color(0xFF0284C7)),
            "2x FLOW",
            "+50% SCORE BOOST"
        )
        else -> Triple(
            listOf(Color(0xFF64748B), Color(0xFF94A3B8)),
            "1x READY",
            "CHAIN RAPID ESCAPES"
        )
    }

    AnimatedVisibility(
        visible = isComboActive,
        enter = fadeIn(tween(200)) + scaleIn(tween(250)),
        exit = fadeOut(tween(300)) + scaleOut(tween(250)),
        modifier = modifier.testTag("real_time_combo_hud")
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.92f),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(badgeColors)
            ),
            modifier = Modifier
                .scale(if (comboMultiplier >= 3) pulseScale else 1f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Circular Timer Decay Gauge with Combo Multiplier Center
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(34.dp)) {
                        val strokeWidth = 3.dp.toPx()
                        // Track background
                        drawCircle(
                            color = Color(0xFF334155),
                            radius = (size.minDimension - strokeWidth) / 2,
                            style = Stroke(width = strokeWidth)
                        )
                        // Active decay arc
                        drawArc(
                            brush = Brush.sweepGradient(badgeColors),
                            startAngle = -90f,
                            sweepAngle = 360f * decayProgress.value,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Icon(
                        imageVector = if (comboMultiplier >= 4) Icons.Default.Bolt else Icons.Default.LocalFireDepartment,
                        contentDescription = "Combo Flame",
                        tint = badgeColors.first(),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Combo Text Details
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = badgeTitle,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${(decayProgress.value * (comboWindowMs / 1000f)).let { String.format("%.1fs", it) }}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = badgeColors.first()
                        )
                    }
                    Text(
                        text = bonusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }
    }
}
