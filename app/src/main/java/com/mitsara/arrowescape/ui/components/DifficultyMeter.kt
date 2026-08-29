package com.mitsara.arrowescape.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.model.Difficulty
import com.mitsara.arrowescape.model.PuzzleLevel

@Composable
fun DifficultyMeter(level: PuzzleLevel, modifier: Modifier = Modifier) {
    val difficultyScore = when (level.difficulty) {
        Difficulty.EASY -> 1
        Difficulty.NORMAL -> 2
        Difficulty.HARD -> 3
        Difficulty.EXPERT -> 4
    }
    val obstacleDensity = level.obstacles.size
    val totalElements = level.arrows.size + obstacleDensity

    val difficultyColor = when (level.difficulty) {
        Difficulty.EASY -> Color(0xFF10B981)
        Difficulty.NORMAL -> Color(0xFF3B82F6)
        Difficulty.HARD -> Color(0xFF8B5CF6)
        Difficulty.EXPERT -> Color(0xFFF59E0B)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Complexity: ${level.difficulty.name}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = difficultyColor
                )
            )
            Text(
                text = "Grid ${level.gridWidth}x${level.gridHeight} • ${level.arrows.size} Arrows • ${obstacleDensity} Obstacles",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            val fraction = (difficultyScore / 4f).coerceIn(0.1f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(difficultyColor)
            )
        }
    }
}
