package com.mitsara.arrowescape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mitsara.arrowescape.monetization.AdsManager
import com.mitsara.arrowescape.monetization.SubscriptionManager
import kotlinx.coroutines.launch
import com.mitsara.arrowescape.ui.screens.AboutPrivacyScreen
import com.mitsara.arrowescape.ui.screens.CosmeticStoreScreen
import com.mitsara.arrowescape.ui.screens.DailyChallengeScreen
import com.mitsara.arrowescape.ui.screens.GameplayScreen
import com.mitsara.arrowescape.ui.screens.PhaseRoadmapScreen
import com.mitsara.arrowescape.ui.screens.MainMenuScreen
import com.mitsara.arrowescape.ui.screens.PremiumScreen
import com.mitsara.arrowescape.ui.screens.SettingsScreen
import com.mitsara.arrowescape.ui.screens.SplashScreen
import com.mitsara.arrowescape.ui.screens.StatisticsScreen
import com.mitsara.arrowescape.ui.theme.ArrowEscapeTheme
import com.mitsara.arrowescape.ui.viewmodel.GameViewModel

sealed class Screen {
    object Splash : Screen()
    object MainMenu : Screen()
    object LevelSelect : Screen()
    data class Gameplay(val levelId: Int) : Screen()
    object DailyChallenge : Screen()
    object Statistics : Screen()
    object Store : Screen()
    object Premium : Screen()
    object Settings : Screen()
    object AboutPrivacy : Screen()
}

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AdsManager.initialize(applicationContext)
        SubscriptionManager.initialize(applicationContext)

        setContent {
            ArrowEscapeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ArrowEscapeApp(viewModel = gameViewModel)
                }
            }
        }
    }
}

@Composable
fun ArrowEscapeApp(viewModel: GameViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    val userSettings by viewModel.userSettings.collectAsState()
    val completedLevels by viewModel.completedLevels.collectAsState()
    val levelProgressMap by viewModel.levelProgressMap.collectAsState()

    when (val screen = currentScreen) {
        is Screen.Splash -> {
            SplashScreen(
                onSplashFinished = { currentScreen = Screen.MainMenu }
            )
        }
        is Screen.MainMenu -> {
            MainMenuScreen(
                currentLevelId = userSettings.currentLevelId,
                totalStars = userSettings.totalStars,
                isPremium = userSettings.isPremium,
                onPlayClick = {
                    currentScreen = Screen.Gameplay(userSettings.currentLevelId)
                },
                onLevelSelectClick = { currentScreen = Screen.LevelSelect },
                onDailyChallengeClick = { currentScreen = Screen.DailyChallenge },
                onStatsClick = { currentScreen = Screen.Statistics },
                onStoreClick = { currentScreen = Screen.Store },
                onPremiumClick = { currentScreen = Screen.Premium },
                onSettingsClick = { currentScreen = Screen.Settings },
                onAboutClick = { currentScreen = Screen.AboutPrivacy }
            )
        }
        is Screen.Store -> {
            CosmeticStoreScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.MainMenu }
            )
        }
        is Screen.Statistics -> {
            StatisticsScreen(
                currentLevelId = userSettings.currentLevelId,
                totalStars = userSettings.totalStars,
                completedLevelsCount = completedLevels.size,
                isPremium = userSettings.isPremium,
                onBackClick = { currentScreen = Screen.MainMenu }
            )
        }
        is Screen.LevelSelect -> {
            PhaseRoadmapScreen(
                currentLevelId = userSettings.currentLevelId,
                completedLevels = completedLevels,
                levelProgressMap = levelProgressMap,
                isPremium = userSettings.isPremium,
                onPremiumClick = { currentScreen = Screen.Premium },
                onLevelSelected = { levelId ->
                    currentScreen = Screen.Gameplay(levelId)
                },
                onBackClick = { currentScreen = Screen.MainMenu }
            )
        }
        is Screen.Gameplay -> {
            GameplayScreen(
                levelId = screen.levelId,
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.LevelSelect },
                onSettingsClick = { currentScreen = Screen.Settings },
                onMainMenuClick = { currentScreen = Screen.MainMenu },
                onPremiumUpgradeClick = { currentScreen = Screen.Premium }
            )
        }
        is Screen.DailyChallenge -> {
            DailyChallengeScreen(
                onStartDailyPuzzle = { seedLevel ->
                    currentScreen = Screen.Gameplay(seedLevel)
                },
                onBackClick = { currentScreen = Screen.MainMenu }
            )
        }
        is Screen.Premium -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
            PremiumScreen(
                isPremium = userSettings.isPremium,
                onUnlockPremiumClick = {
                    coroutineScope.launch {
                        SubscriptionManager.purchasePremium(context) { success, _ ->
                            if (success) {
                                viewModel.setPremiumStatus(true)
                            }
                        }
                    }
                },
                onRestorePurchasesClick = {
                    coroutineScope.launch {
                        SubscriptionManager.restorePurchases(context) { success, _ ->
                            if (success) {
                                viewModel.setPremiumStatus(true)
                            }
                        }
                    }
                },
                onPrivacyClick = { currentScreen = Screen.AboutPrivacy },
                onBackClick = { currentScreen = Screen.MainMenu }
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                userSettings = userSettings,
                onToggleSound = { viewModel.toggleSound() },
                onToggleVibration = { viewModel.toggleVibration() },
                onToggleAutoFirstMoveSuggestion = { viewModel.toggleAutoFirstMoveSuggestion() },
                onSelectTheme = { themeId -> viewModel.selectTheme(themeId) },
                onRestorePurchases = { viewModel.setPremiumStatus(true) },
                onPremiumClick = { currentScreen = Screen.Premium },
                onAboutClick = { currentScreen = Screen.AboutPrivacy },
                onBackClick = { currentScreen = Screen.MainMenu }
            )
        }
        is Screen.AboutPrivacy -> {
            AboutPrivacyScreen(
                onBackClick = { currentScreen = Screen.MainMenu }
            )
        }
    }
}
