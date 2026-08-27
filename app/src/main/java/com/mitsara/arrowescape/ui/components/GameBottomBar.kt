package com.mitsara.arrowescape.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.ui.theme.PrimaryBlue

@Composable
fun GameBottomBar(
    hintsAvailable: Int,
    isPremium: Boolean,
    canUndo: Boolean,
    onUndoClick: () -> Unit,
    onHintClick: () -> Unit,
    onRetryClick: () -> Unit,
    onGetMoreHintsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Floating Circular Button: Hint (Matching Screenshots 1, 2, 3, 4)
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (hintsAvailable > 0 || isPremium) {
                            onHintClick()
                        } else {
                            onGetMoreHintsClick()
                        }
                    }
                    .testTag("hint_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Hint",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Top-Right Badge (e.g. 2)
            Surface(
                shape = CircleShape,
                color = PrimaryBlue,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("hint_badge")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isPremium) "∞" else "$hintsAvailable",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right Floating Circular Button: Grid / Retry / Undo (Matching Screenshots 1, 2, 3, 4)
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .clickable {
                    if (canUndo) onUndoClick() else onRetryClick()
                }
                .testTag("undo_retry_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (canUndo) Icons.AutoMirrored.Filled.Undo else Icons.Default.GridOn,
                    contentDescription = "Undo or Reset",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
