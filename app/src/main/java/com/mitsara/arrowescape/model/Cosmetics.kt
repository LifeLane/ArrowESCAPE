package com.mitsara.arrowescape.model

import androidx.compose.ui.graphics.Color

enum class CosmeticCategory(val title: String, val iconName: String) {
    PRESET("Complete Sets", "AutoAwesome"),
    ARROW("Arrow Skins", "NearMe"),
    BACKGROUND("Backgrounds", "Wallpaper"),
    BOARD("Board Materials", "Dashboard"),
    GRID("Grid Matrix", "GridOn"),
    FRAME("Frames & Borders", "CropFree")
}

enum class CosmeticRarity(val label: String, val color: Color) {
    COMMON("Common", Color(0xFF94A3B8)),
    RARE("Rare", Color(0xFF38BDF8)),
    EPIC("Epic", Color(0xFFA855F7)),
    LEGENDARY("Legendary", Color(0xFFF59E0B)),
    MYTHIC("Mythic", Color(0xFFFF007F))
}

data class CosmeticItem(
    val id: String,
    val name: String,
    val category: CosmeticCategory,
    val rarity: CosmeticRarity,
    val costStars: Int,
    val isPremiumOnly: Boolean = false,
    val description: String,
    val tagline: String,
    val previewColors: List<Color>,
    val secondaryColor: Color = Color.White,
    val glowColor: Color = Color(0xFF00E5FF)
)

data class CosmeticPreset(
    val id: String,
    val name: String,
    val description: String,
    val rarity: CosmeticRarity,
    val isPremiumOnly: Boolean = false,
    val arrowId: String,
    val backgroundId: String,
    val boardId: String,
    val gridId: String,
    val frameId: String,
    val previewColors: List<Color>
)

object CosmeticsCatalog {

    // =========================================================================
    // 1. ARROW SKINS (10 Items)
    // =========================================================================
    val ARROW_CYBER_NEON = CosmeticItem(
        id = "ARROW_CYBER_NEON",
        name = "Cyber Neon Dart",
        category = CosmeticCategory.ARROW,
        rarity = CosmeticRarity.COMMON,
        costStars = 0,
        description = "High-voltage neon laser dart with dual-fiber luminous energy core.",
        tagline = "Default Cyber Standard",
        previewColors = listOf(Color(0xFF00E5FF), Color(0xFF3B82F6), Color(0xFFFFFFFF)),
        glowColor = Color(0xFF00E5FF)
    )

    val ARROW_CRYSTAL_PRISM = CosmeticItem(
        id = "ARROW_CRYSTAL_PRISM",
        name = "Crystal Prism Shard",
        category = CosmeticCategory.ARROW,
        rarity = CosmeticRarity.RARE,
        costStars = 15,
        description = "Faceted crystalline diamond arrow with rainbow spectral refraction along its edges.",
        tagline = "Prismatic Brilliance",
        previewColors = listOf(Color(0xFFA5B4FC), Color(0xFFE0E7FF), Color(0xFF38BDF8)),
        glowColor = Color(0xFF818CF8)
    )

    val ARROW_DRAGON_FLAME = CosmeticItem(
        id = "ARROW_DRAGON_FLAME",
        name = "Infernal Dragon Flame",
        category = CosmeticCategory.ARROW,
        rarity = CosmeticRarity.EPIC,
        costStars = 30,
        description = "Blazing molten spearhead trailing incandescent magma sparks and thermal wake.",
        tagline = "Born from Molten Fire",
        previewColors = listOf(Color(0xFFFF3D00), Color(0xFFFF9100), Color(0xFFFFD600)),
        glowColor = Color(0xFFFF5722)
    )

    val ARROW_PLASMA_BOLT = CosmeticItem(
        id = "ARROW_PLASMA_BOLT",
        name = "Quantum Plasma Bolt",
        category = CosmeticCategory.ARROW,
        rarity = CosmeticRarity.RARE,
        costStars = 20,
        description = "Supercharged violet plasma stream crackling with high-frequency arc lightning.",
        tagline = "Subatomic Voltage",
        previewColors = listOf(Color(0xFFD946EF), Color(0xFF8B5CF6), Color(0xFF6366F1)),
        glowColor = Color(0xFFC084FC)
    )

    val ARROW_STEAMPUNK_BRASS = CosmeticItem(
        id = "ARROW_STEAMPUNK_BRASS",
        name = "Steampunk Brass Pointer",
        category = CosmeticCategory.ARROW,
        rarity = CosmeticRarity.RARE,
        costStars = 25,
        description = "Handcrafted Victorian clockwork pointer made from burnished brass and copper gears.",
        tagline = "Artisanal Mechanism",
        previewColors = listOf(Color(0xFFD97706), Color(0xFFB45309), Color(0xFFFDE68A)),
        glowColor = Color(0xFFF59E0B)
    )

    val ARROW_RETRO_PIXEL = CosmeticItem(
        id = "ARROW_RETRO_PIXEL",
        name = "8-Bit Pixel Blade",
        category = CosmeticCategory.ARROW,
        rarity = CosmeticRarity.COMMON,
        costStars = 10,
        description = "Chunky stepped pixel art arrow ripped straight from a 1984 coin-op cabinet.",
        tagline = "Pure Nostalgia",
        previewColors = listOf(Color(0xFFFF0055), Color(0xFFFFEE00), Color(0xFF00FF99)),
        glowColor = Color(0xFFFF3366)
    )

    val ARROW_HOLOGRAM_AURA = CosmeticItem(
        id = "ARROW_HOLOGRAM_AURA",
        name = "Emerald Matrix Array",
        category = CosmeticCategory.ARROW,
        rarity = CosmeticRarity.EPIC,
        costStars = 35,
        description = "Translucent terminal hologram composed of glowing green vector scanline arrays.",
        tagline = "Neural Construct",
        previewColors = listOf(Color(0xFF10B981), Color(0xFF00FF99), Color(0xFF064E3B)),
        glowColor = Color(0xFF10B981)
    )

    val ARROW_GOOGLY_RAINBOW = CosmeticItem(
        id = "ARROW_GOOGLY_RAINBOW",
        name = "Googly Rainbow Comet",
        category = CosmeticCategory.ARROW,
        rarity = CosmeticRarity.LEGENDARY,
        costStars = 45,
        description = "Playful bouncy rainbow streamer with dancing eyes and sparkling starry comet tail.",
        tagline = "Whimsical & Radiant",
        previewColors = listOf(Color(0xFFFF007F), Color(0xFFFFEE00), Color(0xFF00E5FF), Color(0xFF00E676)),
        glowColor = Color(0xFFFF00CC)
    )

    val ARROW_GOLDEN_ROYAL = CosmeticItem(
        id = "ARROW_GOLDEN_ROYAL",
        name = "Imperial Golden Aegis",
        category = CosmeticCategory.ARROW,
        rarity = CosmeticRarity.LEGENDARY,
        costStars = 50,
        description = "24-karat gilded arrow adorned with imperial filigree and radiant divine halo.",
        tagline = "Crown of Champions",
        previewColors = listOf(Color(0xFFFBBF24), Color(0xFFFEF3C7), Color(0xFFD97706)),
        glowColor = Color(0xFFF59E0B)
    )

    val ARROW_VOID_SINGULARITY = CosmeticItem(
        id = "ARROW_VOID_SINGULARITY",
        name = "Void Singularity Stalker",
        category = CosmeticCategory.ARROW,
        rarity = CosmeticRarity.MYTHIC,
        costStars = 60,
        isPremiumOnly = true,
        description = "Obsidian dark matter blade ringed with a swirling ultraviolet gravitational accretion disk.",
        tagline = "Beyond Event Horizon",
        previewColors = listOf(Color(0xFF6B21A8), Color(0xFF9333EA), Color(0xFF0F172A)),
        glowColor = Color(0xFFA855F7)
    )

    val arrowCosmetics = listOf(
        ARROW_CYBER_NEON, ARROW_CRYSTAL_PRISM, ARROW_DRAGON_FLAME, ARROW_PLASMA_BOLT,
        ARROW_STEAMPUNK_BRASS, ARROW_RETRO_PIXEL, ARROW_HOLOGRAM_AURA, ARROW_GOOGLY_RAINBOW,
        ARROW_GOLDEN_ROYAL, ARROW_VOID_SINGULARITY
    )

    // =========================================================================
    // 2. BACKGROUND SHADERS & THEMES (10 Items)
    // =========================================================================
    val BG_DEEP_COSMOS = CosmeticItem(
        id = "BG_DEEP_COSMOS",
        name = "Deep Cosmos Nebula",
        category = CosmeticCategory.BACKGROUND,
        rarity = CosmeticRarity.COMMON,
        costStars = 0,
        description = "Starlit deep space environment with gently shifting cyan and purple nebula dust clouds.",
        tagline = "Infinite Space",
        previewColors = listOf(Color(0xFF0B1120), Color(0xFF1E1B4B), Color(0xFF00E5FF)),
        glowColor = Color(0xFF38BDF8)
    )

    val BG_CYBER_GRID_WARP = CosmeticItem(
        id = "BG_CYBER_GRID_WARP",
        name = "Cyber Synthwave Horizon",
        category = CosmeticCategory.BACKGROUND,
        rarity = CosmeticRarity.RARE,
        costStars = 15,
        description = "3D perspective neon wireframe grid receding into a radiant retro-futuristic horizon.",
        tagline = "Neon Matrix Highway",
        previewColors = listOf(Color(0xFF180828), Color(0xFFFF007F), Color(0xFF00E5FF)),
        glowColor = Color(0xFFFF007F)
    )

    val BG_AURORA_BOREALIS = CosmeticItem(
        id = "BG_AURORA_BOREALIS",
        name = "Celestial Aurora",
        category = CosmeticCategory.BACKGROUND,
        rarity = CosmeticRarity.EPIC,
        costStars = 30,
        description = "Silky undulating ribbons of emerald and sapphire light dancing across an Arctic night sky.",
        tagline = "Nature's Wonder",
        previewColors = listOf(Color(0xFF06202A), Color(0xFF059669), Color(0xFF06B6D4)),
        glowColor = Color(0xFF10B981)
    )

    val BG_MATRIX_CODE = CosmeticItem(
        id = "BG_MATRIX_CODE",
        name = "Neural Data Stream",
        category = CosmeticCategory.BACKGROUND,
        rarity = CosmeticRarity.RARE,
        costStars = 20,
        description = "Cascading digital luminescence streams with pulsating algorithmic circuit traces.",
        tagline = "Hacker Cyberspace",
        previewColors = listOf(Color(0xFF021B14), Color(0xFF00FF99), Color(0xFF052E16)),
        glowColor = Color(0xFF00FF99)
    )

    val BG_OBSIDIAN_CHASM = CosmeticItem(
        id = "BG_OBSIDIAN_CHASM",
        name = "Obsidian Crystal Chasm",
        category = CosmeticCategory.BACKGROUND,
        rarity = CosmeticRarity.RARE,
        costStars = 25,
        description = "Dark geometric crystalline facets illuminated by volcanic amber subterranean light.",
        tagline = "Deep Earth Forge",
        previewColors = listOf(Color(0xFF1C1917), Color(0xFF78350F), Color(0xFFF59E0B)),
        glowColor = Color(0xFFD97706)
    )

    val BG_RETRO_SYNTHWAVE = CosmeticItem(
        id = "BG_RETRO_SYNTHWAVE",
        name = "Miami Sunset 1986",
        category = CosmeticCategory.BACKGROUND,
        rarity = CosmeticRarity.EPIC,
        costStars = 35,
        description = "Nostalgic dual-tone dusk gradient with segmented solar disk and tropical neon dusk.",
        tagline = "Outrun Sunset",
        previewColors = listOf(Color(0xFF2E0854), Color(0xFFFF5722), Color(0xFFFFEE55)),
        glowColor = Color(0xFFFF71CE)
    )

    val BG_ZEN_BAMBOO_MIST = CosmeticItem(
        id = "BG_ZEN_BAMBOO_MIST",
        name = "Zen Misty Sanctuary",
        category = CosmeticCategory.BACKGROUND,
        rarity = CosmeticRarity.RARE,
        costStars = 20,
        description = "Soft organic parchment wash with drifting morning mountain mist and cedar tones.",
        tagline = "Peace of Mind",
        previewColors = listOf(Color(0xFF26201B), Color(0xFF785E4F), Color(0xFFD4C3A3)),
        glowColor = Color(0xFFD4C3A3)
    )

    val BG_LAVA_FORGE = CosmeticItem(
        id = "BG_LAVA_FORGE",
        name = "Molten Magma Chamber",
        category = CosmeticCategory.BACKGROUND,
        rarity = CosmeticRarity.LEGENDARY,
        costStars = 45,
        description = "Intense churning magma flows with incandescent rising thermal embers.",
        tagline = "Volcanic Energy",
        previewColors = listOf(Color(0xFF2A0800), Color(0xFFDC2626), Color(0xFFFF8A00)),
        glowColor = Color(0xFFFF3D00)
    )

    val BG_GOLDEN_PANTHEON = CosmeticItem(
        id = "BG_GOLDEN_PANTHEON",
        name = "Golden Celestial Temple",
        category = CosmeticCategory.BACKGROUND,
        rarity = CosmeticRarity.LEGENDARY,
        costStars = 50,
        description = "Divine radiant mandalas radiating sacred lightbeams and sparkling gold dust.",
        tagline = "Sanctuary of Light",
        previewColors = listOf(Color(0xFF1E1704), Color(0xFFB45309), Color(0xFFFDE047)),
        glowColor = Color(0xFFFBBF24)
    )

    val BG_PRISMATIC_CARNIVAL = CosmeticItem(
        id = "BG_PRISMATIC_CARNIVAL",
        name = "Prismatic Candy Carnival",
        category = CosmeticCategory.BACKGROUND,
        rarity = CosmeticRarity.MYTHIC,
        costStars = 60,
        isPremiumOnly = true,
        description = "Vibrant multi-chromatic spectrum wave pulsing with lively confetti particles.",
        tagline = "Carnival of Color",
        previewColors = listOf(Color(0xFF1F0B2E), Color(0xFFFF007F), Color(0xFF00E5FF), Color(0xFFFFB800)),
        glowColor = Color(0xFFFF00CC)
    )

    val backgroundCosmetics = listOf(
        BG_DEEP_COSMOS, BG_CYBER_GRID_WARP, BG_AURORA_BOREALIS, BG_MATRIX_CODE,
        BG_OBSIDIAN_CHASM, BG_RETRO_SYNTHWAVE, BG_ZEN_BAMBOO_MIST, BG_LAVA_FORGE,
        BG_GOLDEN_PANTHEON, BG_PRISMATIC_CARNIVAL
    )

    // =========================================================================
    // 3. BOARD MATERIALS & SURFACES (10 Items)
    // =========================================================================
    val BOARD_OBSIDIAN = CosmeticItem(
        id = "BOARD_OBSIDIAN",
        name = "Obsidian Glass Slate",
        category = CosmeticCategory.BOARD,
        rarity = CosmeticRarity.COMMON,
        costStars = 0,
        description = "High-polish volcanic obsidian with subtle dark reflections and rounded bevels.",
        tagline = "Sleek Dark Glass",
        previewColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155)),
        glowColor = Color(0xFF64748B)
    )

    val BOARD_CARBON_FIBER = CosmeticItem(
        id = "BOARD_CARBON_FIBER",
        name = "Matte Carbon Weave",
        category = CosmeticCategory.BOARD,
        rarity = CosmeticRarity.RARE,
        costStars = 15,
        description = "Aeronautical twill-woven carbon fiber with high-contrast matte texture.",
        tagline = "Ultra Lightweight Armor",
        previewColors = listOf(Color(0xFF111418), Color(0xFF20262E), Color(0xFF3B4856)),
        glowColor = Color(0xFF00E5FF)
    )

    val BOARD_HOLO_GLASS = CosmeticItem(
        id = "BOARD_HOLO_GLASS",
        name = "Frosted Aero Glass",
        category = CosmeticCategory.BOARD,
        rarity = CosmeticRarity.EPIC,
        costStars = 30,
        description = "Translucent frosted fluorite glass with chromatic dispersion along the inner rim.",
        tagline = "Luminescent Transparency",
        previewColors = listOf(Color(0xFF1E2640), Color(0xFF2E3A5F), Color(0xFF818CF8)),
        glowColor = Color(0xFF6366F1)
    )

    val BOARD_ROYAL_MARBLE = CosmeticItem(
        id = "BOARD_ROYAL_MARBLE",
        name = "Imperial White Marble",
        category = CosmeticCategory.BOARD,
        rarity = CosmeticRarity.RARE,
        costStars = 25,
        description = "Pure Carrara white marble embedded with polished golden quartz veins.",
        tagline = "Classical Elegance",
        previewColors = listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1), Color(0xFFD97706)),
        glowColor = Color(0xFFF59E0B)
    )

    val BOARD_RETRO_ARCADE = CosmeticItem(
        id = "BOARD_RETRO_ARCADE",
        name = "Arcade CRT Bezel",
        category = CosmeticCategory.BOARD,
        rarity = CosmeticRarity.COMMON,
        costStars = 10,
        description = "Curved dark phosphor cathode ray tube canvas with slight ambient glow.",
        tagline = "Classic Coin-Op",
        previewColors = listOf(Color(0xFF14141F), Color(0xFF1E1E2E), Color(0xFFFFB000)),
        glowColor = Color(0xFFFFB000)
    )

    val BOARD_DEEP_AMETHYST = CosmeticItem(
        id = "BOARD_DEEP_AMETHYST",
        name = "Deep Amethyst Geode",
        category = CosmeticCategory.BOARD,
        rarity = CosmeticRarity.EPIC,
        costStars = 35,
        description = "Rich royal violet crystalline quartz slab illuminated by internal purple radiance.",
        tagline = "Mystic Mineral",
        previewColors = listOf(Color(0xFF1A102F), Color(0xFF2D1A4E), Color(0xFFA855F7)),
        glowColor = Color(0xFFC084FC)
    )

    val BOARD_STEAMPUNK_COPPER = CosmeticItem(
        id = "BOARD_STEAMPUNK_COPPER",
        name = "Riveted Copper Plate",
        category = CosmeticCategory.BOARD,
        rarity = CosmeticRarity.RARE,
        costStars = 20,
        description = "Brushed industrial copper plate with brass edge studs and aged patina.",
        tagline = "Heavy Steamworks",
        previewColors = listOf(Color(0xFF331F14), Color(0xFF5C3826), Color(0xFFB45309)),
        glowColor = Color(0xFFF59E0B)
    )

    val BOARD_NEON_ACRYLIC = CosmeticItem(
        id = "BOARD_NEON_ACRYLIC",
        name = "Cyberpunk Smoked Acrylic",
        category = CosmeticCategory.BOARD,
        rarity = CosmeticRarity.LEGENDARY,
        costStars = 45,
        description = "Deep translucent navy acrylic sheet illuminated by edge-lit cyan laser diodes.",
        tagline = "Tokyo Neon Glow",
        previewColors = listOf(Color(0xFF081220), Color(0xFF0C243D), Color(0xFF00F0FF)),
        glowColor = Color(0xFF00F0FF)
    )

    val BOARD_ZEN_CEDAR = CosmeticItem(
        id = "BOARD_ZEN_CEDAR",
        name = "Ancient Cedar Wood",
        category = CosmeticCategory.BOARD,
        rarity = CosmeticRarity.RARE,
        costStars = 20,
        description = "Natural fine-grain Japanese cedar with smooth organic lacquer finish.",
        tagline = "Organic Harmony",
        previewColors = listOf(Color(0xFF3B2A1E), Color(0xFF5A4030), Color(0xFFD4C3A3)),
        glowColor = Color(0xFFD4C3A3)
    )

    val BOARD_COSMIC_MIRROR = CosmeticItem(
        id = "BOARD_COSMIC_MIRROR",
        name = "Cosmic Event Horizon",
        category = CosmeticCategory.BOARD,
        rarity = CosmeticRarity.MYTHIC,
        costStars = 60,
        isPremiumOnly = true,
        description = "Dark cosmic mirror absorbing surrounding photons with shimmering quantum particles.",
        tagline = "Quantum Singularity",
        previewColors = listOf(Color(0xFF080612), Color(0xFF140D2B), Color(0xFFE879F9)),
        glowColor = Color(0xFFF472B6)
    )

    val boardCosmetics = listOf(
        BOARD_OBSIDIAN, BOARD_CARBON_FIBER, BOARD_HOLO_GLASS, BOARD_ROYAL_MARBLE,
        BOARD_RETRO_ARCADE, BOARD_DEEP_AMETHYST, BOARD_STEAMPUNK_COPPER, BOARD_NEON_ACRYLIC,
        BOARD_ZEN_CEDAR, BOARD_COSMIC_MIRROR
    )

    // =========================================================================
    // 4. GRID MATRIX & LATTICE STYLES (10 Items)
    // =========================================================================
    val GRID_NEON_LATTICE = CosmeticItem(
        id = "GRID_NEON_LATTICE",
        name = "Neon Pulse Lattice",
        category = CosmeticCategory.GRID,
        rarity = CosmeticRarity.COMMON,
        costStars = 0,
        description = "Smooth rounded cell capsules with luminous center laser pinpoints.",
        tagline = "Clean Cyber Cells",
        previewColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0F172A)),
        glowColor = Color(0xFF38BDF8)
    )

    val GRID_HOLO_DOTS = CosmeticItem(
        id = "GRID_HOLO_DOTS",
        name = "Hologram Dot Matrix",
        category = CosmeticCategory.GRID,
        rarity = CosmeticRarity.RARE,
        costStars = 15,
        description = "Microscopic pulsing laser dots with crosshair alignment marks.",
        tagline = "Precision Matrix",
        previewColors = listOf(Color(0xFF00E5FF), Color(0xFF00B4D8), Color(0xFF1E293B)),
        glowColor = Color(0xFF00E5FF)
    )

    val GRID_CIRCUIT_TRACES = CosmeticItem(
        id = "GRID_CIRCUIT_TRACES",
        name = "Printed Circuit Traces",
        category = CosmeticCategory.GRID,
        rarity = CosmeticRarity.EPIC,
        costStars = 30,
        description = "Interconnecting gold motherboard bus lines and micro-transistor solder nodes.",
        tagline = "Motherboard Architecture",
        previewColors = listOf(Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF064E3B)),
        glowColor = Color(0xFF10B981)
    )

    val GRID_HONEYCOMB_HEX = CosmeticItem(
        id = "GRID_HONEYCOMB_HEX",
        name = "Hexagonal Nano-Mesh",
        category = CosmeticCategory.GRID,
        rarity = CosmeticRarity.RARE,
        costStars = 20,
        description = "Futuristic honeycomb hexagonal sub-divisions with subtle corner chamfers.",
        tagline = "Biomimetic Structure",
        previewColors = listOf(Color(0xFF00F0FF), Color(0xFF6366F1), Color(0xFF0F172A)),
        glowColor = Color(0xFF00F0FF)
    )

    val GRID_DIAMOND_PRISM = CosmeticItem(
        id = "GRID_DIAMOND_PRISM",
        name = "Diamond Facet Lattice",
        category = CosmeticCategory.GRID,
        rarity = CosmeticRarity.RARE,
        costStars = 25,
        description = "Angled diamond rhombic grid lines shimmering with crystalline refractions.",
        tagline = "Faceted Geometry",
        previewColors = listOf(Color(0xFFA78BFA), Color(0xFF818CF8), Color(0xFF1E1B4B)),
        glowColor = Color(0xFFA78BFA)
    )

    val GRID_RADAR_RETICLE = CosmeticItem(
        id = "GRID_RADAR_RETICLE",
        name = "Tactical Radar Reticle",
        category = CosmeticCategory.GRID,
        rarity = CosmeticRarity.EPIC,
        costStars = 35,
        description = "Targeting corner tick brackets and coordinate telemetry markings on every cell.",
        tagline = "Lock-on Targeting",
        previewColors = listOf(Color(0xFFEF4444), Color(0xFFF97316), Color(0xFF1C1917)),
        glowColor = Color(0xFFEF4444)
    )

    val GRID_RETRO_SCAN = CosmeticItem(
        id = "GRID_RETRO_SCAN",
        name = "8-Bit CRT Phosphor Grid",
        category = CosmeticCategory.GRID,
        rarity = CosmeticRarity.COMMON,
        costStars = 10,
        description = "Chunky pixel blocks separated by horizontal phosphor scanlines and warm amber glow.",
        tagline = "Cathode Pixel Array",
        previewColors = listOf(Color(0xFFFFB000), Color(0xFFFF8000), Color(0xFF1E1400)),
        glowColor = Color(0xFFFFB000)
    )

    val GRID_CELESTIAL_STARS = CosmeticItem(
        id = "GRID_CELESTIAL_STARS",
        name = "Constellation Starmap",
        category = CosmeticCategory.GRID,
        rarity = CosmeticRarity.LEGENDARY,
        costStars = 45,
        description = "Twinkling 4-point astral star nodes connected by delicate starlight coordinate filaments.",
        tagline = "Navigational Starmap",
        previewColors = listOf(Color(0xFFFDE047), Color(0xFF93C5FD), Color(0xFF0F172A)),
        glowColor = Color(0xFFFDE047)
    )

    val GRID_GOLDEN_FILIGREE = CosmeticItem(
        id = "GRID_GOLDEN_FILIGREE",
        name = "Royal Golden Filigree",
        category = CosmeticCategory.GRID,
        rarity = CosmeticRarity.LEGENDARY,
        costStars = 50,
        description = "Hand-engraved imperial gold corner insets with polished rosette center crests.",
        tagline = "Imperial Masterpiece",
        previewColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFF78350F)),
        glowColor = Color(0xFFFBBF24)
    )

    val GRID_MINIMAL_CLEAN = CosmeticItem(
        id = "GRID_MINIMAL_CLEAN",
        name = "Nordic Minimal Clean",
        category = CosmeticCategory.GRID,
        rarity = CosmeticRarity.MYTHIC,
        costStars = 60,
        isPremiumOnly = true,
        description = "Ultra-refined subtle frosted cell cavities with micro-specular light highlights.",
        tagline = "Pure Elegance",
        previewColors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF0F172A)),
        glowColor = Color(0xFFF8FAFC)
    )

    val gridCosmetics = listOf(
        GRID_NEON_LATTICE, GRID_HOLO_DOTS, GRID_CIRCUIT_TRACES, GRID_HONEYCOMB_HEX,
        GRID_DIAMOND_PRISM, GRID_RADAR_RETICLE, GRID_RETRO_SCAN, GRID_CELESTIAL_STARS,
        GRID_GOLDEN_FILIGREE, GRID_MINIMAL_CLEAN
    )

    // =========================================================================
    // 5. FRAMES & BORDER ENCLOSURES (10 Items)
    // =========================================================================
    val FRAME_CYBER_BRACKETS = CosmeticItem(
        id = "FRAME_CYBER_BRACKETS",
        name = "Cyber Tech Corner Brackets",
        category = CosmeticCategory.FRAME,
        rarity = CosmeticRarity.COMMON,
        costStars = 0,
        description = "Precision segmented corner brackets with pulsing cyan system LED status bars.",
        tagline = "Tactical Enclosure",
        previewColors = listOf(Color(0xFF00E5FF), Color(0xFF0284C7), Color(0xFF1E293B)),
        glowColor = Color(0xFF00E5FF)
    )

    val FRAME_NEON_PULSE_TUBE = CosmeticItem(
        id = "FRAME_NEON_PULSE_TUBE",
        name = "Continuous Neon Glow Tube",
        category = CosmeticCategory.FRAME,
        rarity = CosmeticRarity.RARE,
        costStars = 15,
        description = "Continuous glass tube filled with glowing energized neon gas and traveling light pulse.",
        tagline = "Infinite Neon Flow",
        previewColors = listOf(Color(0xFFFF007F), Color(0xFF00E5FF), Color(0xFFFFFFFF)),
        glowColor = Color(0xFFFF007F)
    )

    val FRAME_GOLDEN_ROYAL_CHOP = CosmeticItem(
        id = "FRAME_GOLDEN_ROYAL_CHOP",
        name = "Imperial Golden Bezel",
        category = CosmeticCategory.FRAME,
        rarity = CosmeticRarity.LEGENDARY,
        costStars = 50,
        description = "Heavy double-banded gilded gold frame with heraldic diamond corner gemstones.",
        tagline = "Royal Sovereignty",
        previewColors = listOf(Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFFFEF3C7)),
        glowColor = Color(0xFFF59E0B)
    )

    val FRAME_HOLO_SHIELD = CosmeticItem(
        id = "FRAME_HOLO_SHIELD",
        name = "Holographic Force Barrier",
        category = CosmeticCategory.FRAME,
        rarity = CosmeticRarity.EPIC,
        costStars = 30,
        description = "Hexagonal force-field containment perimeter with pulsating corner emitter pylons.",
        tagline = "Energy Containment",
        previewColors = listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFF00F0FF)),
        glowColor = Color(0xFF38BDF8)
    )

    val FRAME_STEAMPUNK_BRONZE = CosmeticItem(
        id = "FRAME_STEAMPUNK_BRONZE",
        name = "Industrial Riveted Bronze",
        category = CosmeticCategory.FRAME,
        rarity = CosmeticRarity.RARE,
        costStars = 20,
        description = "Cast bronze plating with prominent industrial rivets and functional corner gears.",
        tagline = "Heavy Steam Power",
        previewColors = listOf(Color(0xFFB45309), Color(0xFF78350F), Color(0xFFFDE68A)),
        glowColor = Color(0xFFD97706)
    )

    val FRAME_QUANTUM_CONTAINMENT = CosmeticItem(
        id = "FRAME_QUANTUM_CONTAINMENT",
        name = "Quantum Flux Chamber",
        category = CosmeticCategory.FRAME,
        rarity = CosmeticRarity.EPIC,
        costStars = 35,
        description = "Superconducting magnetic coils holding unstable purple plasma along the board rim.",
        tagline = "Zero-Point Flux",
        previewColors = listOf(Color(0xFFA855F7), Color(0xFF6366F1), Color(0xFF00E5FF)),
        glowColor = Color(0xFFA855F7)
    )

    val FRAME_CARBON_CHROME = CosmeticItem(
        id = "FRAME_CARBON_CHROME",
        name = "Aerospace Titanium & Chrome",
        category = CosmeticCategory.FRAME,
        rarity = CosmeticRarity.RARE,
        costStars = 25,
        description = "Brushed aircraft-grade titanium with high-gloss mirror-finish chrome chamfers.",
        tagline = "Machined Perfection",
        previewColors = listOf(Color(0xFFE2E8F0), Color(0xFF64748B), Color(0xFF0F172A)),
        glowColor = Color(0xFF94A3B8)
    )

    val FRAME_RETRO_CABINET = CosmeticItem(
        id = "FRAME_RETRO_CABINET",
        name = "Arcade Cabinet Marquee",
        category = CosmeticCategory.FRAME,
        rarity = CosmeticRarity.COMMON,
        costStars = 10,
        description = "Multi-color illuminated marquee edge with blinking neon corner lights.",
        tagline = "Insert Coin to Play",
        previewColors = listOf(Color(0xFFFF3366), Color(0xFFFFEE00), Color(0xFF00E5FF)),
        glowColor = Color(0xFFFF3366)
    )

    val FRAME_VOID_ACCRETION = CosmeticItem(
        id = "FRAME_VOID_ACCRETION",
        name = "Void Accretion Ring",
        category = CosmeticCategory.FRAME,
        rarity = CosmeticRarity.LEGENDARY,
        costStars = 45,
        description = "Swirling dark matter halo devouring background light with deep violet pulse.",
        tagline = "Gravitational Trap",
        previewColors = listOf(Color(0xFF581C87), Color(0xFF3B0764), Color(0xFFC084FC)),
        glowColor = Color(0xFF9333EA)
    )

    val FRAME_PRISMATIC_GLOW = CosmeticItem(
        id = "FRAME_PRISMATIC_GLOW",
        name = "Prismatic Rainbow Halo",
        category = CosmeticCategory.FRAME,
        rarity = CosmeticRarity.MYTHIC,
        costStars = 60,
        isPremiumOnly = true,
        description = "Ultra-luminous cycling full-spectrum rainbow aura with soft particle radiance.",
        tagline = "Pure Spectrum Energy",
        previewColors = listOf(Color(0xFFFF0055), Color(0xFFFF9900), Color(0xFF00FF99), Color(0xFF00F0FF)),
        glowColor = Color(0xFFFF00CC)
    )

    val frameCosmetics = listOf(
        FRAME_CYBER_BRACKETS, FRAME_NEON_PULSE_TUBE, FRAME_GOLDEN_ROYAL_CHOP, FRAME_HOLO_SHIELD,
        FRAME_STEAMPUNK_BRONZE, FRAME_QUANTUM_CONTAINMENT, FRAME_CARBON_CHROME, FRAME_RETRO_CABINET,
        FRAME_VOID_ACCRETION, FRAME_PRISMATIC_GLOW
    )

    // =========================================================================
    // 6. CURATED 5-PIECE PRESET THEMES (8 Complete Sets)
    // =========================================================================
    val PRESET_CYBER_DECK = CosmeticPreset(
        id = "PRESET_CYBER_DECK",
        name = "Cyber Deck 2077",
        description = "Complete cyberpunk setup: Cyber Neon Arrow, Horizon Grid BG, Carbon Weave Board, Circuit Traces Grid, and Tech Brackets Frame.",
        rarity = CosmeticRarity.COMMON,
        arrowId = ARROW_CYBER_NEON.id,
        backgroundId = BG_CYBER_GRID_WARP.id,
        boardId = BOARD_CARBON_FIBER.id,
        gridId = GRID_CIRCUIT_TRACES.id,
        frameId = FRAME_CYBER_BRACKETS.id,
        previewColors = listOf(Color(0xFF00E5FF), Color(0xFFFF007F), Color(0xFF10B981))
    )

    val PRESET_COSMOS_EXPLORER = CosmeticPreset(
        id = "PRESET_COSMOS_EXPLORER",
        name = "Cosmos Explorer",
        description = "Deep space expedition: Crystal Prism Arrow, Deep Cosmos BG, Cosmic Mirror Board, Constellation Grid, and Quantum Containment Frame.",
        rarity = CosmeticRarity.RARE,
        arrowId = ARROW_CRYSTAL_PRISM.id,
        backgroundId = BG_DEEP_COSMOS.id,
        boardId = BOARD_COSMIC_MIRROR.id,
        gridId = GRID_CELESTIAL_STARS.id,
        frameId = FRAME_QUANTUM_CONTAINMENT.id,
        previewColors = listOf(Color(0xFF818CF8), Color(0xFF38BDF8), Color(0xFFFDE047))
    )

    val PRESET_DRAGON_FORGE = CosmeticPreset(
        id = "PRESET_DRAGON_FORGE",
        name = "Infernal Dragon Forge",
        description = "Molten volcanic armor: Dragon Flame Arrow, Lava Forge BG, Obsidian Slate Board, Radar Reticle Grid, and Void Accretion Frame.",
        rarity = CosmeticRarity.EPIC,
        arrowId = ARROW_DRAGON_FLAME.id,
        backgroundId = BG_LAVA_FORGE.id,
        boardId = BOARD_OBSIDIAN.id,
        gridId = GRID_RADAR_RETICLE.id,
        frameId = FRAME_VOID_ACCRETION.id,
        previewColors = listOf(Color(0xFFFF3D00), Color(0xFFFF9100), Color(0xFFDC2626))
    )

    val PRESET_ROYAL_PANTHEON = CosmeticPreset(
        id = "PRESET_ROYAL_PANTHEON",
        name = "Royal Golden Pantheon",
        description = "Imperial regalia: Golden Royal Arrow, Celestial Temple BG, Royal Marble Board, Golden Filigree Grid, and Imperial Golden Bezel.",
        rarity = CosmeticRarity.LEGENDARY,
        arrowId = ARROW_GOLDEN_ROYAL.id,
        backgroundId = BG_GOLDEN_PANTHEON.id,
        boardId = BOARD_ROYAL_MARBLE.id,
        gridId = GRID_GOLDEN_FILIGREE.id,
        frameId = FRAME_GOLDEN_ROYAL_CHOP.id,
        previewColors = listOf(Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFFFEF3C7))
    )

    val PRESET_RETRO_80S = CosmeticPreset(
        id = "PRESET_RETRO_80S",
        name = "Retro Arcade 1986",
        description = "Authentic retro coin-op: Pixel Blade Arrow, Sunset Synthwave BG, Arcade CRT Board, Phosphor Scan Grid, and Arcade Marquee Frame.",
        rarity = CosmeticRarity.RARE,
        arrowId = ARROW_RETRO_PIXEL.id,
        backgroundId = BG_RETRO_SYNTHWAVE.id,
        boardId = BOARD_RETRO_ARCADE.id,
        gridId = GRID_RETRO_SCAN.id,
        frameId = FRAME_RETRO_CABINET.id,
        previewColors = listOf(Color(0xFFFF3366), Color(0xFFFFB000), Color(0xFF00FF99))
    )

    val PRESET_QUANTUM_NEBULA = CosmeticPreset(
        id = "PRESET_QUANTUM_NEBULA",
        name = "Quantum Plasma Nebula",
        description = "Supercharged quantum tech: Plasma Bolt Arrow, Aurora Borealis BG, Deep Amethyst Board, Diamond Prism Grid, and Holo Shield Frame.",
        rarity = CosmeticRarity.EPIC,
        arrowId = ARROW_PLASMA_BOLT.id,
        backgroundId = BG_AURORA_BOREALIS.id,
        boardId = BOARD_DEEP_AMETHYST.id,
        gridId = GRID_DIAMOND_PRISM.id,
        frameId = FRAME_HOLO_SHIELD.id,
        previewColors = listOf(Color(0xFFD946EF), Color(0xFF8B5CF6), Color(0xFF00F0FF))
    )

    val PRESET_ZEN_SANCTUARY = CosmeticPreset(
        id = "PRESET_ZEN_SANCTUARY",
        name = "Zen Sanctuary",
        description = "Tranquil craftsmanship: Steampunk Brass Arrow, Misty Bamboo BG, Ancient Cedar Board, Minimal Nordic Grid, and Titanium Frame.",
        rarity = CosmeticRarity.RARE,
        arrowId = ARROW_STEAMPUNK_BRASS.id,
        backgroundId = BG_ZEN_BAMBOO_MIST.id,
        boardId = BOARD_ZEN_CEDAR.id,
        gridId = GRID_MINIMAL_CLEAN.id,
        frameId = FRAME_CARBON_CHROME.id,
        previewColors = listOf(Color(0xFFD97706), Color(0xFF785E4F), Color(0xFFD4C3A3))
    )

    val PRESET_GOOGLY_CARNIVAL = CosmeticPreset(
        id = "PRESET_GOOGLY_CARNIVAL",
        name = "Googly Carnival Prism",
        description = "Ultimate party celebration: Googly Rainbow Arrow, Prismatic Carnival BG, Neon Acrylic Board, Holo Dots Grid, and Prismatic Glow Frame.",
        rarity = CosmeticRarity.MYTHIC,
        isPremiumOnly = true,
        arrowId = ARROW_GOOGLY_RAINBOW.id,
        backgroundId = BG_PRISMATIC_CARNIVAL.id,
        boardId = BOARD_NEON_ACRYLIC.id,
        gridId = GRID_HOLO_DOTS.id,
        frameId = FRAME_PRISMATIC_GLOW.id,
        previewColors = listOf(Color(0xFFFF007F), Color(0xFFFFEE00), Color(0xFF00E5FF), Color(0xFF00E676))
    )

    val allPresets = listOf(
        PRESET_CYBER_DECK, PRESET_COSMOS_EXPLORER, PRESET_DRAGON_FORGE, PRESET_ROYAL_PANTHEON,
        PRESET_RETRO_80S, PRESET_QUANTUM_NEBULA, PRESET_ZEN_SANCTUARY, PRESET_GOOGLY_CARNIVAL
    )

    val allCosmetics: List<CosmeticItem> = arrowCosmetics + backgroundCosmetics + boardCosmetics + gridCosmetics + frameCosmetics

    fun getCosmetic(id: String): CosmeticItem? {
        return allCosmetics.find { it.id.equals(id, ignoreCase = true) }
    }

    fun getCosmetics(category: CosmeticCategory): List<CosmeticItem> {
        return when (category) {
            CosmeticCategory.ARROW -> arrowCosmetics
            CosmeticCategory.BACKGROUND -> backgroundCosmetics
            CosmeticCategory.BOARD -> boardCosmetics
            CosmeticCategory.GRID -> gridCosmetics
            CosmeticCategory.FRAME -> frameCosmetics
            CosmeticCategory.PRESET -> emptyList()
        }
    }

    fun getDefault(category: CosmeticCategory): CosmeticItem {
        return when (category) {
            CosmeticCategory.ARROW -> ARROW_CYBER_NEON
            CosmeticCategory.BACKGROUND -> BG_DEEP_COSMOS
            CosmeticCategory.BOARD -> BOARD_OBSIDIAN
            CosmeticCategory.GRID -> GRID_NEON_LATTICE
            CosmeticCategory.FRAME -> FRAME_CYBER_BRACKETS
            CosmeticCategory.PRESET -> ARROW_CYBER_NEON
        }
    }

    fun isDefaultUnlocked(id: String): Boolean {
        return id in listOf(
            ARROW_CYBER_NEON.id,
            BG_DEEP_COSMOS.id,
            BOARD_OBSIDIAN.id,
            GRID_NEON_LATTICE.id,
            FRAME_CYBER_BRACKETS.id
        )
    }
}
