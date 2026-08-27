package com.mitsara.arrowescape.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.ui.theme.SurfaceLight
import com.mitsara.arrowescape.ui.theme.TextPrimary
import com.mitsara.arrowescape.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPrivacyScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About & Privacy Policy", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
        ) {
            Text(
                text = "Arrow Escape",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "How to Play",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "1. Find an arrow whose entire exit line is unobstructed.\n2. Tap the free arrow to launch it off the board.\n3. The board space clears and unblocks other arrows.\n4. Repeat until all arrows escape!",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Arrow Escape is built offline-first. Your gameplay progress, unlocked levels, and preferences are stored locally on your device. We do not collect personal data, email addresses, or location tracking. For optional monetization, standard Google AdMob and RevenueCat SDKs process anonymized diagnostic telemetry.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Terms of Service",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "By downloading and playing Arrow Escape, you agree to play fairly and respect app copyrights. All branding, arrow mechanics, and visual design belong to Mitsara / Arrow Escape.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                color = TextSecondary
            )
        }
    }
}
