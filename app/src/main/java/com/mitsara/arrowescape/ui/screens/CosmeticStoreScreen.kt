package com.mitsara.arrowescape.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitsara.arrowescape.model.*
import com.mitsara.arrowescape.ui.components.*
import com.mitsara.arrowescape.ui.theme.GoldStar
import com.mitsara.arrowescape.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticStoreScreen(
    viewModel: GameViewModel,
    onBackClick: () -> Unit,
    onPremiumClick: () -> Unit = {}
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val isPremium = userSettings.isPremium
    val unlockedSet = remember(userSettings.unlockedCosmetics) {
        userSettings.unlockedCosmetics.split(",").filter { it.isNotBlank() }.toSet()
    }

    var selectedTab by remember { mutableStateOf(CosmeticCategory.ARROW) }

    // Live Fitting Room Preview State (defaults to currently equipped)
    var previewArrow by remember(userSettings.selectedArrow) { mutableStateOf(userSettings.selectedArrow) }
    var previewBackground by remember(userSettings.selectedBackground) { mutableStateOf(userSettings.selectedBackground) }
    var previewBoard by remember(userSettings.selectedBoard) { mutableStateOf(userSettings.selectedBoard) }
    var previewGrid by remember(userSettings.selectedGrid) { mutableStateOf(userSettings.selectedGrid) }
    var previewFrame by remember(userSettings.selectedFrame) { mutableStateOf(userSettings.selectedFrame) }

    // Sample arrows for live interactive fitting room stage
    val testArrows = remember {
        listOf(
            Arrow(id = 1, startX = 0, startY = 1, length = 2, direction = Direction.RIGHT),
            Arrow(id = 2, startX = 2, startY = 0, length = 1, direction = Direction.UP),
            Arrow(id = 3, startX = 3, startY = 2, length = 2, direction = Direction.DOWN),
            Arrow(id = 4, startX = 1, startY = 3, length = 1, direction = Direction.LEFT)
        )
    }
    var activeTestArrows by remember { mutableStateOf(testArrows) }
    var animatingArrowId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cosmetics & Power-Ups",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Customization Vault & Shop",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF38BDF8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Coins Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🪙", fontSize = 12.sp)
                                Text(
                                    text = "${userSettings.coins}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = Color(0xFFFBBF24)
                                )
                            }
                        }

                        // Diamonds Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0EA5E9).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("💎", fontSize = 12.sp)
                                Text(
                                    text = "${userSettings.diamonds}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0A0E17)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // =================================================================
            // TOP SECTION: INTERACTIVE LIVE FITTING ROOM STAGE
            // =================================================================
            if (selectedTab != CosmeticCategory.POWERUP) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                Text(
                                    text = "LIVE FITTING ROOM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFF00E5FF)
                                )
                            }

                            TextButton(
                                onClick = {
                                    previewArrow = userSettings.selectedArrow
                                    previewBackground = userSettings.selectedBackground
                                    previewBoard = userSettings.selectedBoard
                                    previewGrid = userSettings.selectedGrid
                                    previewFrame = userSettings.selectedFrame
                                    activeTestArrows = testArrows
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reset", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        // Mini Board Stage with active background
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            CosmeticBackgroundCanvas(
                                backgroundId = previewBackground,
                                modifier = Modifier.fillMaxSize()
                            )

                            PuzzleBoardView(
                                gridWidth = 4,
                                gridHeight = 4,
                                activeArrows = activeTestArrows,
                                animatingArrowId = animatingArrowId,
                                animatingDirection = null,
                                hintArrowId = null,
                                isMistakeShake = false,
                                onArrowClick = { tappedId ->
                                    animatingArrowId = tappedId
                                    scope.launch {
                                        kotlinx.coroutines.delay(450)
                                        animatingArrowId = null
                                        activeTestArrows = activeTestArrows.filter { it.id != tappedId }
                                        if (activeTestArrows.isEmpty()) {
                                            kotlinx.coroutines.delay(300)
                                            activeTestArrows = testArrows
                                        }
                                    }
                                },
                                selectedArrowId = previewArrow,
                                selectedBoardId = previewBoard,
                                selectedGridId = previewGrid,
                                selectedFrameId = previewFrame,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            )
                        }

                        // Active Preview Loadout Indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val arrowName = CosmeticsCatalog.getCosmetic(previewArrow)?.name ?: "Arrow"
                            val bgName = CosmeticsCatalog.getCosmetic(previewBackground)?.name ?: "Background"
                            val boardName = CosmeticsCatalog.getCosmetic(previewBoard)?.name ?: "Board"

                            FittingRoomPill(label = "🏹 $arrowName")
                            FittingRoomPill(label = "🌌 $bgName")
                            FittingRoomPill(label = "🔲 $boardName")
                        }
                    }
                }
            }

            // =================================================================
            // CATEGORY SELECTOR TABS
            // =================================================================
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CosmeticCategory.values()) { cat ->
                    val isSelected = selectedTab == cat
                    val tabColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155),
                        label = "TabColor"
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF0F2B48) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, tabColor),
                        modifier = Modifier.clickable { selectedTab = cat }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = cat.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // =================================================================
            // POWER-UP STORE / COSMETIC ITEM GRID & CARDS
            // =================================================================
            if (selectedTab == CosmeticCategory.POWERUP) {
                PowerUpStoreSection(
                    userSettings = userSettings,
                    onBuyPowerUp = { type, useDiamonds ->
                        viewModel.buyPowerUp(type, useDiamonds)
                    }
                )
            } else if (selectedTab == CosmeticCategory.PRESET) {
                // Complete Sets Tab
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(CosmeticsCatalog.allPresets) { preset ->
                        PresetThemeCard(
                            preset = preset,
                            isEquipped = userSettings.selectedArrow == preset.arrowId &&
                                    userSettings.selectedBackground == preset.backgroundId &&
                                    userSettings.selectedBoard == preset.boardId &&
                                    userSettings.selectedGrid == preset.gridId &&
                                    userSettings.selectedFrame == preset.frameId,
                            isPremium = isPremium,
                            onPreview = {
                                previewArrow = preset.arrowId
                                previewBackground = preset.backgroundId
                                previewBoard = preset.boardId
                                previewGrid = preset.gridId
                                previewFrame = preset.frameId
                            },
                            onEquip = {
                                viewModel.equipPreset(preset)
                                previewArrow = preset.arrowId
                                previewBackground = preset.backgroundId
                                previewBoard = preset.boardId
                                previewGrid = preset.gridId
                                previewFrame = preset.frameId
                            },
                            onUnlockPremium = onPremiumClick
                        )
                    }
                }
            } else {
                // Individual Category Items (10 Items per category)
                val items = CosmeticsCatalog.getCosmetics(selectedTab)
                val equippedId = when (selectedTab) {
                    CosmeticCategory.ARROW -> userSettings.selectedArrow
                    CosmeticCategory.BACKGROUND -> userSettings.selectedBackground
                    CosmeticCategory.BOARD -> userSettings.selectedBoard
                    CosmeticCategory.GRID -> userSettings.selectedGrid
                    CosmeticCategory.FRAME -> userSettings.selectedFrame
                    CosmeticCategory.PRESET, CosmeticCategory.POWERUP -> ""
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items) { cosmetic ->
                        val isEquipped = equippedId.equals(cosmetic.id, ignoreCase = true)
                        val isUnlocked = CosmeticsCatalog.isDefaultUnlocked(cosmetic.id) ||
                                unlockedSet.contains(cosmetic.id) ||
                                (isPremium && !cosmetic.isPremiumOnly) ||
                                cosmetic.costStars == 0

                        CosmeticItemCard(
                            cosmetic = cosmetic,
                            isEquipped = isEquipped,
                            isUnlocked = isUnlocked,
                            userStars = userSettings.totalStars,
                            userCoins = userSettings.coins,
                            userDiamonds = userSettings.diamonds,
                            isPremium = isPremium,
                            onPreview = {
                                when (cosmetic.category) {
                                    CosmeticCategory.ARROW -> previewArrow = cosmetic.id
                                    CosmeticCategory.BACKGROUND -> previewBackground = cosmetic.id
                                    CosmeticCategory.BOARD -> previewBoard = cosmetic.id
                                    CosmeticCategory.GRID -> previewGrid = cosmetic.id
                                    CosmeticCategory.FRAME -> previewFrame = cosmetic.id
                                    CosmeticCategory.PRESET, CosmeticCategory.POWERUP -> {}
                                }
                            },
                            onEquip = {
                                viewModel.equipCosmetic(cosmetic.category, cosmetic.id)
                                when (cosmetic.category) {
                                    CosmeticCategory.ARROW -> previewArrow = cosmetic.id
                                    CosmeticCategory.BACKGROUND -> previewBackground = cosmetic.id
                                    CosmeticCategory.BOARD -> previewBoard = cosmetic.id
                                    CosmeticCategory.GRID -> previewGrid = cosmetic.id
                                    CosmeticCategory.FRAME -> previewFrame = cosmetic.id
                                    CosmeticCategory.PRESET, CosmeticCategory.POWERUP -> {}
                                }
                            },
                            onUnlockWithStars = {
                                viewModel.unlockCosmetic(cosmetic.id, cosmetic.costStars)
                            },
                            onUnlockWithCoins = {
                                viewModel.unlockCosmeticWithCurrency(cosmetic.id, "COINS", cosmetic.costCoins)
                            },
                            onUnlockPremium = onPremiumClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PowerUpStoreSection(
    userSettings: com.mitsara.arrowescape.data.UserSettingsEntity,
    onBuyPowerUp: (PowerUpType, Boolean) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(PowerUpType.values()) { powerUp ->
            val chargesOwned = when (powerUp) {
                PowerUpType.LASER_VAPORIZER -> userSettings.laserCharges
                PowerUpType.ZEN_SHIELD -> userSettings.shieldCharges
                PowerUpType.SONAR_MAGNET -> userSettings.magnetCharges
            }
            val canAffordCoins = userSettings.coins >= powerUp.coinCost
            val canAffordDiamonds = userSettings.diamonds >= powerUp.diamondCost

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F2B48)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(powerUp.icon, fontSize = 22.sp)
                            }
                            Column {
                                Text(
                                    text = powerUp.displayName,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Inventory: $chargesOwned Charges",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = powerUp.description,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Buy 1 Charge with Coins
                        Button(
                            onClick = { onBuyPowerUp(powerUp, false) },
                            enabled = canAffordCoins,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B),
                                disabledContainerColor = Color(0xFF334155)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("🪙 +1 for ${powerUp.coinCost}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (canAffordCoins) Color.Black else Color.Gray)
                        }

                        // Buy 2 Charges with Diamonds
                        Button(
                            onClick = { onBuyPowerUp(powerUp, true) },
                            enabled = canAffordDiamonds,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7),
                                disabledContainerColor = Color(0xFF334155)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("💎 +2 for ${powerUp.diamondCost}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FittingRoomPill(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE2E8F0),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CosmeticItemCard(
    cosmetic: CosmeticItem,
    isEquipped: Boolean,
    isUnlocked: Boolean,
    userStars: Int,
    userCoins: Int,
    userDiamonds: Int,
    isPremium: Boolean,
    onPreview: () -> Unit,
    onEquip: () -> Unit,
    onUnlockWithStars: () -> Unit,
    onUnlockWithCoins: () -> Unit,
    onUnlockPremium: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isEquipped -> Color(0xFF10B981)
            isUnlocked -> Color(0xFF38BDF8).copy(alpha = 0.5f)
            else -> cosmetic.rarity.color.copy(alpha = 0.35f)
        },
        label = "CardBorder"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
        border = androidx.compose.foundation.BorderStroke(if (isEquipped) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEquipped) 6.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() }
            .testTag("cosmetic_${cosmetic.id}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Rarity Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = cosmetic.rarity.color.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cosmetic.rarity.color.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = cosmetic.rarity.label.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = cosmetic.rarity.color,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                if (isEquipped) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Equipped", tint = Color.Black, modifier = Modifier.size(14.dp).padding(2.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Procedural Visual Thumbnail Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090D16))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                CosmeticThumbnailRenderer(cosmetic = cosmetic)
            }

            Spacer(Modifier.height(8.dp))

            // Item Name & Tagline
            Text(
                text = cosmetic.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = cosmetic.tagline,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Action Button (Equip / Unlock / Premium)
            when {
                isEquipped -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                            disabledContentColor = Color(0xFF10B981)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    ) {
                        Text("EQUIPPED", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
                isUnlocked -> {
                    Button(
                        onClick = onEquip,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    ) {
                        Text("EQUIP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                cosmetic.isPremiumOnly && !isPremium -> {
                    Button(
                        onClick = onUnlockPremium,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("PREMIUM", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
                else -> {
                    val canAffordCoins = userCoins >= cosmetic.costCoins
                    val canAffordStars = userStars >= cosmetic.costStars
                    if (cosmetic.costCoins > 0) {
                        Button(
                            onClick = onUnlockWithCoins,
                            enabled = canAffordCoins,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B),
                                disabledContainerColor = Color(0xFF334155)
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth().height(32.dp)
                        ) {
                            Text("🪙 ${cosmetic.costCoins}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (canAffordCoins) Color.Black else Color.Gray)
                        }
                    } else {
                        Button(
                            onClick = onUnlockWithStars,
                            enabled = canAffordStars,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B),
                                disabledContainerColor = Color(0xFF334155)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth().height(32.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = if (canAffordStars) Color.Black else Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = "${cosmetic.costStars}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (canAffordStars) Color.Black else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetThemeCard(
    preset: CosmeticPreset,
    isEquipped: Boolean,
    isPremium: Boolean,
    onPreview: () -> Unit,
    onEquip: () -> Unit,
    onUnlockPremium: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
        border = androidx.compose.foundation.BorderStroke(
            if (isEquipped) 2.dp else 1.dp,
            if (isEquipped) Color(0xFF10B981) else preset.rarity.color.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview thumbnail
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = preset.previewColors + listOf(Color(0xFF0F172A))
                        )
                    )
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = preset.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = preset.rarity.color.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = preset.rarity.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = preset.rarity.color,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                if (isEquipped) {
                    Text("✓ FULL SET EQUIPPED", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                } else if (preset.isPremiumOnly && !isPremium) {
                    Button(
                        onClick = onUnlockPremium,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("UNLOCK WITH PREMIUM", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onEquip,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("EQUIP FULL SET", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * High-fidelity procedural thumbnail renderer for each item in the store
 */
@Composable
fun CosmeticThumbnailRenderer(cosmetic: CosmeticItem) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        when (cosmetic.category) {
            CosmeticCategory.ARROW -> {
                // Render sample arrow pointer
                val sampleArrow = Arrow(id = 1, startX = 0, startY = 0, length = 2, direction = Direction.UP)
                val dummyTheme = ThemeManager.RETRO_ARCADE
                drawCosmeticArrowhead(
                    selectedArrowId = cosmetic.id,
                    tipPos = Offset(cx, cy),
                    headLength = 34f,
                    headWidth = 38f,
                    arrowColor = cosmetic.previewColors.firstOrNull() ?: Color(0xFF00E5FF),
                    alpha = 1f
                )
            }
            CosmeticCategory.BACKGROUND -> {
                // Procedural mini background pattern
                drawRect(
                    brush = Brush.linearGradient(
                        colors = cosmetic.previewColors + listOf(Color(0xFF050912)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )
                drawCircle(
                    color = cosmetic.glowColor.copy(alpha = 0.5f),
                    radius = 16.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }
            CosmeticCategory.BOARD -> {
                drawCosmeticBoardSurface(
                    boardId = cosmetic.id,
                    size = Size(w * 0.8f, h * 0.7f),
                    baseColor = cosmetic.previewColors.firstOrNull() ?: Color(0xFF0F172A)
                )
            }
            CosmeticCategory.GRID -> {
                drawCosmeticGridSlots(
                    gridId = cosmetic.id,
                    gridWidth = 3,
                    gridHeight = 2,
                    cellW = w / 3,
                    cellH = h / 2,
                    validCells = null,
                    themeDotColor = cosmetic.previewColors.firstOrNull() ?: Color(0xFF38BDF8)
                )
            }
            CosmeticCategory.FRAME -> {
                drawCosmeticFrameBorder(
                    frameId = cosmetic.id,
                    size = Size(w - 12.dp.toPx(), h - 12.dp.toPx()),
                    accentColor = cosmetic.previewColors.firstOrNull() ?: Color(0xFF00E5FF)
                )
            }
            CosmeticCategory.PRESET, CosmeticCategory.POWERUP -> {
                drawCircle(
                    brush = Brush.radialGradient(colors = cosmetic.previewColors),
                    radius = 24.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }
        }
    }
}
