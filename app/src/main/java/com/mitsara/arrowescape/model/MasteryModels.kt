package com.mitsara.arrowescape.model

import androidx.compose.runtime.Immutable

@Immutable
data class MasteryAchievement(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val targetValue: Int,
    val diamondReward: Int,
    val iconEmoji: String
)

enum class AchievementCategory(val title: String) {
    DEDUCTION("Logic & Deduction"),
    SILHOUETTES("Silhouette Gallery"),
    CHRONO_RUSH("Chrono Rush"),
    ZEN_FLOW("Zen Flow State")
}

object MasteryRegistry {
    val ACHIEVEMENTS = listOf(
        MasteryAchievement(
            id = "ACH_PERFECTIONIST_1",
            title = "Flawless Deduction",
            description = "Clear 5 silhouette levels with zero mistake taps",
            category = AchievementCategory.DEDUCTION,
            targetValue = 5,
            diamondReward = 15,
            iconEmoji = "🎯"
        ),
        MasteryAchievement(
            id = "ACH_PERFECTIONIST_2",
            title = "Pure Intuition",
            description = "Clear 20 silhouette levels with zero mistake taps",
            category = AchievementCategory.DEDUCTION,
            targetValue = 20,
            diamondReward = 35,
            iconEmoji = "✨"
        ),
        MasteryAchievement(
            id = "ACH_CASCADE_MASTER",
            title = "Cascade Virtuoso",
            description = "Trigger a 6-arrow chain reaction in under 3 seconds",
            category = AchievementCategory.DEDUCTION,
            targetValue = 6,
            diamondReward = 20,
            iconEmoji = "⚡"
        ),
        MasteryAchievement(
            id = "ACH_BLINDFOLD",
            title = "No Guiding Hand",
            description = "Complete 15 levels without using any hints",
            category = AchievementCategory.DEDUCTION,
            targetValue = 15,
            diamondReward = 25,
            iconEmoji = "👁️"
        ),
        MasteryAchievement(
            id = "ACH_DEPTH_CHAMPION",
            title = "Master of Complexity",
            description = "Solve a puzzle with dependency depth of 20 or higher",
            category = AchievementCategory.DEDUCTION,
            targetValue = 20,
            diamondReward = 30,
            iconEmoji = "🧩"
        ),

        // Silhouette Gallery
        MasteryAchievement(
            id = "ACH_TIER_1_MASTER",
            title = "Natural Archetypes Master",
            description = "Clear all 50 silhouette levels in Tier 1",
            category = AchievementCategory.SILHOUETTES,
            targetValue = 50,
            diamondReward = 40,
            iconEmoji = "🌿"
        ),
        MasteryAchievement(
            id = "ACH_TIER_2_MASTER",
            title = "Cyber Horizon Master",
            description = "Clear all 50 silhouette levels in Tier 2",
            category = AchievementCategory.SILHOUETTES,
            targetValue = 50,
            diamondReward = 40,
            iconEmoji = "🔷"
        ),
        MasteryAchievement(
            id = "ACH_SILHOUETTE_100",
            title = "Centurion Curator",
            description = "Unlock 100 unique silhouette artworks in the Exhibition Hall",
            category = AchievementCategory.SILHOUETTES,
            targetValue = 100,
            diamondReward = 60,
            iconEmoji = "🏛️"
        ),
        MasteryAchievement(
            id = "ACH_SILHOUETTE_250",
            title = "Pantheon Explorer",
            description = "Unlock 250 unique silhouette artworks",
            category = AchievementCategory.SILHOUETTES,
            targetValue = 250,
            diamondReward = 100,
            iconEmoji = "👑"
        ),
        MasteryAchievement(
            id = "ACH_GRANDMASTER_500",
            title = "Apex Grandmaster",
            description = "Complete all 500 handcrafted silhouette stages",
            category = AchievementCategory.SILHOUETTES,
            targetValue = 500,
            diamondReward = 250,
            iconEmoji = "🏆"
        ),

        // Chrono Rush
        MasteryAchievement(
            id = "ACH_CHRONO_NOVICE",
            title = "Quick Silver",
            description = "Score 25+ points in Chrono Rush Mode",
            category = AchievementCategory.CHRONO_RUSH,
            targetValue = 25,
            diamondReward = 15,
            iconEmoji = "⏱️"
        ),
        MasteryAchievement(
            id = "ACH_CHRONO_PRO",
            title = "Time Warp Sprint",
            description = "Score 60+ points in Chrono Rush Mode",
            category = AchievementCategory.CHRONO_RUSH,
            targetValue = 60,
            diamondReward = 35,
            iconEmoji = "🔥"
        ),
        MasteryAchievement(
            id = "ACH_CHRONO_FRENZY",
            title = "Frenzy Overdrive",
            description = "Reach 8x Combo Multiplier in Chrono Rush Mode",
            category = AchievementCategory.CHRONO_RUSH,
            targetValue = 8,
            diamondReward = 25,
            iconEmoji = "💥"
        ),

        // Zen Flow
        MasteryAchievement(
            id = "ACH_ZEN_FLOW_TRANSCENDENCE",
            title = "Transcendence",
            description = "Reach Level 4 Flow State in Zen Flow Mode",
            category = AchievementCategory.ZEN_FLOW,
            targetValue = 4,
            diamondReward = 20,
            iconEmoji = "🌊"
        ),
        MasteryAchievement(
            id = "ACH_ZEN_ESCAPE_100",
            title = "Infinite Calm",
            description = "Escape 100 arrows in Zen Flow Mode without a single mistake",
            category = AchievementCategory.ZEN_FLOW,
            targetValue = 100,
            diamondReward = 30,
            iconEmoji = "🧘"
        )
    )
}

@Immutable
data class CustomLevelData(
    val id: String,
    val title: String,
    val author: String = "Player",
    val shareCode: String,
    val gridSize: Int,
    val arrows: List<Arrow>,
    val obstacles: Set<GridPoint> = emptySet(),
    val isSolvable: Boolean = true,
    val dependencyDepth: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
