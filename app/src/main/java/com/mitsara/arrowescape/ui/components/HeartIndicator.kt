package com.mitsara.arrowescape.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mitsara.arrowescape.ui.theme.HeartEmptyGray
import com.mitsara.arrowescape.ui.theme.HeartRed

@Composable
fun HeartIndicator(
    remainingLives: Int,
    maxLives: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.testTag("heart_indicator")
    ) {
        for (i in 1..maxLives) {
            val isFull = i <= remainingLives

            val scale by animateFloatAsState(
                targetValue = if (isFull) 1.0f else 0.85f,
                animationSpec = spring(dampingRatio = 0.6f),
                label = "heartScale"
            )

            val tint by animateColorAsState(
                targetValue = if (isFull) HeartRed else HeartEmptyGray,
                label = "heartColor"
            )

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Heart $i",
                tint = tint,
                modifier = Modifier
                    .size(26.dp)
                    .scale(scale)
            )
            if (i < maxLives) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}
