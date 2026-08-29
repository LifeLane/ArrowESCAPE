package com.mitsara.arrowescape.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.mitsara.arrowescape.data.UserSettingsEntity
import com.mitsara.arrowescape.model.GameTheme
import com.mitsara.arrowescape.model.ThemeManager
import com.mitsara.arrowescape.monetization.ConsentManager
import com.mitsara.arrowescape.monetization.ConsentStatus
import com.mitsara.arrowescape.ui.components.AdBannerView
import com.mitsara.arrowescape.ui.theme.PrimaryBlue
import com.mitsara.arrowescape.ui.theme.SurfaceCard
import com.mitsara.arrowescape.ui.theme.SurfaceLight
import com.mitsara.arrowescape.ui.theme.TextPrimary
import com.mitsara.arrowescape.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userSettings: UserSettingsEntity,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    onChangeHapticLevel: (String) -> Unit = {},
    onToggleCloudSync: () -> Unit = {},
    onToggleAutoFirstMoveSuggestion: () -> Unit = {},
    onSelectTheme: (String) -> Unit = {},
    onRestorePurchases: () -> Unit,
    onPremiumClick: () -> Unit = {},
    onAboutClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val isPersonalizedAdsEnabled by ConsentManager.isPersonalizedAdsEnabled.collectAsState()
    val consentStatus by ConsentManager.consentStatus.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = SurfaceLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Audio & Feedback Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "AUDIO & FEEDBACK", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Sound Effects", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = userSettings.soundEnabled,
                            onCheckedChange = { onToggleSound() },
                            modifier = Modifier.testTag("sound_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Haptic Vibration", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = userSettings.vibrationEnabled,
                            onCheckedChange = { onToggleVibration() },
                            modifier = Modifier.testTag("vibration_switch")
                        )
                    }

                    if (userSettings.vibrationEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Haptic Intensity", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("OFF", "LIGHT", "MEDIUM", "HEAVY").forEach { level ->
                                val isSelected = userSettings.hapticLevel.equals(level, ignoreCase = true)
                                Button(
                                    onClick = { onChangeHapticLevel(level) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = level,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cloud Sync & Account Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "CLOUD SYNC & BACKUP", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Google Play Cloud Sync", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (userSettings.cloudSyncEnabled) "Status: Synced securely with cloud" else "Status: Local offline save only",
                                    color = if (userSettings.cloudSyncEnabled) Color(0xFF4CAF50) else TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Switch(
                            checked = userSettings.cloudSyncEnabled,
                            onCheckedChange = { onToggleCloudSync() },
                            modifier = Modifier.testTag("cloud_sync_switch")
                        )
                    }
                }
            }

            // Gameplay Assist Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "GAMEPLAY ASSIST", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Auto First-Move Hint", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(text = "Highlight optimal first arrow on level start", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                        Switch(
                            checked = userSettings.autoFirstMoveSuggestion,
                            onCheckedChange = { onToggleAutoFirstMoveSuggestion() },
                            modifier = Modifier.testTag("auto_hint_switch")
                        )
                    }
                }
            }

            // Visual Themes Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "VISUAL THEMES", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                        if (!userSettings.isPremium) {
                            Surface(
                                color = com.mitsara.arrowescape.ui.theme.GoldStar.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "PREMIUM ONLY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = com.mitsara.arrowescape.ui.theme.GoldStar,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ThemeManager.allThemes.forEach { theme ->
                        val isSelected = userSettings.selectedTheme.equals(theme.id, ignoreCase = true)
                        val isLocked = theme.isPremiumOnly && !userSettings.isPremium

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) PrimaryBlue else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isLocked) {
                                        onPremiumClick()
                                    } else {
                                        onSelectTheme(theme.id)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = theme.boardCanvasColor,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, theme.gridDotColor),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Surface(
                                                color = theme.arrowNormalColor,
                                                shape = CircleShape,
                                                modifier = Modifier.size(8.dp)
                                            ) {}
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = theme.displayName,
                                        color = TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                if (isLocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Ads & Privacy Preferences Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "ADS & PRIVACY PREFERENCES", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Personalized Ads (GDPR)", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(text = "Status: ${if (consentStatus == ConsentStatus.OBTAINED) "Consent Granted" else "Standard"}", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isPersonalizedAdsEnabled,
                            onCheckedChange = { ConsentManager.setPersonalizedAdsEnabled(it) },
                            modifier = Modifier.testTag("personalized_ads_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onPremiumClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (userSettings.isPremium) "Status: Premium (Ad-Free)" else "Remove Ads / Upgrade to Premium",
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Purchases & Support Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "MONETIZATION & PURCHASES", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onRestorePurchases,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Restore Purchases", color = TextPrimary)
                        }
                    }
                }
            }

            // About & Legal
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "ABOUT & LEGAL", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onAboutClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "About & Privacy Policy", color = TextPrimary)
                        }
                    }
                }
            }

            AdBannerView(
                isPremium = userSettings.isPremium,
                onRemoveAdsClick = onPremiumClick
            )

            Text(
                text = "Arrow Escape v1.0.0 (Build 1)\nPackage: com.mitsara.arrowescape",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = TextSecondary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
