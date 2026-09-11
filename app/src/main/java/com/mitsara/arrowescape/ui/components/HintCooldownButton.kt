package com.mitsara.arrowescape.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
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

/**
 * Visual Hint Button with Radial Cooldown Timer & Combo Acceleration Cue.
 */
@Composable
fun HintCooldownButton(
    hintsAvailable: Int,
    cooldownSeconds: Int,
    maxCooldownSeconds: Int = 5,
    isPremium: Boolean = false,
    activeColor: Color = Color(0xFFF59E0B),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCoolingDown = cooldownSeconds > 0
    val hasCharges = isPremium || hintsAvailable > 0
    val isUsable = !isCoolingDown && hasCharges

    val cooldownFraction = if (maxCooldownSeconds > 0) {
        (cooldownSeconds.toFloat() / maxCooldownSeconds).coerceIn(0f, 1f)
    } else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "hint_ready")
    val readyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ready_scale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Main Circle Button
        Surface(
            shape = CircleShape,
            color = when {
                isCoolingDown -> Color(0xFF1E293B).copy(alpha = 0.9f)
                hasCharges -> activeColor
                else -> Color(0xFF64748B)
            },
            shadowElevation = if (isUsable) 6.dp else 2.dp,
            modifier = Modifier
                .size(54.dp)
                .scale(if (isUsable) readyScale else 1f)
                .clip(CircleShape)
                .clickable(enabled = isUsable, onClick = onClick)
                .testTag("hint_cooldown_button")
        ) {
            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                // Radial Cooldown Ring Sweep
                if (isCoolingDown) {
                    Canvas(modifier = Modifier.size(54.dp)) {
                        val strokeWidth = 3.5.dp.toPx()
                        // Track background
                        drawCircle(
                            color = Color(0xFF334155),
                            radius = (size.minDimension - strokeWidth) / 2,
                            style = Stroke(width = strokeWidth)
                        )
                        // Remaining cooldown arc
                        drawArc(
                            color = activeColor,
                            startAngle = -90f,
                            sweepAngle = 360f * (1f - cooldownFraction),
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Countdown text overlay in center
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${cooldownSeconds}s",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "COOLING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF94A3B8)
                        )
                    }
                } else {
                    // Ready Lightbulb Icon
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Hint",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Hints Count Badge (Top-Right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(22.dp)
                .background(
                    if (isPremium) Color(0xFFF59E0B) else Color(0xFFEF4444),
                    CircleShape
                )
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPremium) "∞" else "$hintsAvailable",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
