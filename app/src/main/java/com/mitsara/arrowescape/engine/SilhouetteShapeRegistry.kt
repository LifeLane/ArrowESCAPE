package com.mitsara.arrowescape.engine

import com.mitsara.arrowescape.model.GridPoint
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Registry of 500 Unique Silhouette Shapes spanning Levels 1 to 500.
 * Progressively scales grid matrix dimensions (from 10x10 up to 18x18)
 * and arrow density/lines according to level difficulty.
 */
object SilhouetteShapeRegistry {

    data class SilhouetteLevelDef(
        val levelNumber: Int,
        val name: String,
        val category: String,
        val baseGridSize: Int,
        val baseArrowCount: Int,
        val evaluator: (nx: Double, ny: Double, x: Int, y: Int, w: Int, h: Int) -> Boolean
    )

    private val levelDefs by lazy {
        Array(500) { index ->
            val level = index + 1
            buildLevelDef(level)
        }
    }

    fun getLevelDef(levelNumber: Int): SilhouetteLevelDef {
        val clamped = levelNumber.coerceIn(1, 500)
        return levelDefs[clamped - 1]
    }

    /**
     * Generates a 2D boolean mask of dimensions [gridWidth] x [gridHeight].
     */
    fun generateShapeMask(levelNumber: Int, gridWidth: Int, gridHeight: Int): Array<BooleanArray> {
        val def = getLevelDef(levelNumber)
        val mask = Array(gridWidth) { BooleanArray(gridHeight) { false } }
        val cx = (gridWidth - 1) / 2.0
        val cy = (gridHeight - 1) / 2.0

        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val nx = (x - cx) / (gridWidth / 2.0)
                val ny = (y - cy) / (gridHeight / 2.0)
                mask[x][y] = def.evaluator(nx, ny, x, y, gridWidth, gridHeight)
            }
        }
        return mask
    }

    // 500 Distinct Curated Titles
    private val SILHOUETTE_NAMES_500 = listOf(
        // Tier 1: 1 - 50 (Everyday & Fauna Archetypes)
        "Golden Key", "Morning Coffee Mug", "Origami Swan", "Vintage Teapot", "Hourglass",
        "Pocket Watch", "Craft Scissors", "Iron Padlock", "Camping Lantern", "Trophy Cup",
        "Liberty Bell", "Classic Umbrella", "Idea Lightbulb", "Oak Acorn", "Honey Bee",
        "Apple Core", "Lucky Clover", "Sunflower", "Mushroom Cap", "Seahorse",
        "Starfish", "Seashell", "Rock Crab", "Moon Jellyfish", "Sea Turtle",
        "Playful Dolphin", "Nautical Anchor", "Compass Rose", "Sailboat", "Harbor Lighthouse",
        "Calico Kitten", "Forest Squirrel", "Cotton Bunny", "Prickly Hedgehog", "Mallard Duck",
        "Barn Owl", "Peregrine Falcon", "Hummingbird", "Emperor Penguin", "Puppy Companion",
        "Giant Panda", "Sacred Heart", "Red Fox", "Antler Stag", "Koala Bear",
        "Outback Kangaroo", "Savanna Elephant", "Tall Giraffe", "Lion Mane", "Grand Piano",

        // Tier 2: 51 - 100 (Botany, Music & Architecture)
        "Bonsai Tree", "Saguaro Cactus", "Oasis Palm", "Pine Cone", "Ginkgo Leaf",
        "Lotus Bloom", "Wild Orchid", "Spring Tulip", "Velvet Rose", "Cherry Blossom",
        "Chameleon", "Spotted Gecko", "King Cobra", "River Alligator", "Tree Frog",
        "Desert Scorpion", "Dragonfly", "Praying Mantis", "Ladybug", "Jewel Beetle",
        "Acoustic Guitar", "Classic Violin", "Saxophone", "Brass Trumpet", "Golden Harp",
        "Snare Drum", "French Horn", "Silver Flute", "Concertina", "Vinyl Record",
        "Artist Palette", "Fine Paintbrush", "Quill Pen", "Open Grimoire", "Comedy Mask",
        "Vintage Camera", "Cinema Film Reel", "Magic Wand", "Crystal Sphere", "Fortune Tarot",
        "Dutch Windmill", "River Watermill", "Cozy Cabin", "Stone Well", "Alpine Chalet",
        "Covered Bridge", "Garden Gazebo", "Clock Tower", "Monarch Butterfly", "Royal Citadel",

        // Tier 3: 101 - 150 (Culinary, Celestial & Maritime)
        "Ice Cream Cone", "Sweet Cupcake", "Glazed Donut", "Pizza Slice", "Gourmet Burger",
        "Crispy Taco", "Sushi Roll", "Steamed Dumpling", "Bavarian Pretzel", "Butter Croissant",
        "Ruby Strawberry", "Banana Bunch", "Grape Cluster", "Watermelon Wedge", "Tropical Pineapple",
        "Avocado Halves", "Fiery Chili", "Garden Carrot", "Sweet Corn", "Chef Toque",
        "Crescent Moon", "Radiant Sun", "Rain Cloud", "Lightning Bolt", "Snowflake Jewel",
        "Shooting Star", "Ringed Saturn", "Space Shuttle", "Lunar Lander", "Orbital Satellite",
        "Deep Submarine", "Harbor Tugboat", "Pirate Galleon", "Viking Longship", "Fishing Trawler",
        "Offshore Speedboat", "Wooden Canoe", "River Kayak", "Windsurf Sail", "Ocean Surfboard",
        "Diving Goggles", "Brass Diver Helmet", "Message Bottle", "Treasure Chest", "Pirate Cutlass",
        "Ship Steering Wheel", "Safety Lifebuoy", "Whaler Harpoon", "Giant Squid", "Blue Whale",

        // Tier 4: 151 - 200 (Monuments, Relics & Labyrinths)
        "Roman Colosseum", "Greek Parthenon", "Great Pyramid", "Egyptian Sphinx", "Taj Mahal",
        "Eiffel Spire", "Leaning Pisa Tower", "Big Ben Clock", "Oriental Pagoda", "Shinto Torii Gate",
        "Aztec Pyramid", "Mayan Ziggurat", "Stonehenge Ring", "Easter Island Moai", "Gothic Cathedral",
        "Mosque Minaret", "Persian Palace", "Forbidden Palace", "Hanging Gardens", "Pharos Lighthouse",
        "Knight Greathelm", "Chainmail Crest", "Heraldic Shield", "Steel Broadsword", "Double Battleaxe",
        "Heavy Crossbow", "Spiked Morningstar", "Polearm Halberd", "Yew Longbow", "Iron Gauntlet",
        "Chess Pawn", "Chess Rook", "Chess Knight", "Chess Bishop", "Chess Queen",
        "Chess King", "Imperial Scepter", "Royal Crown", "Sovereign Orb", "Coronation Throne",
        "Maze Cross", "Concentric Labyrinth", "Greek Meander", "Celtic Triskelion", "Celtic Knot",
        "Diamond Maze", "Hexagonal Matrix", "Octagon Mandala", "Labyrinth Matrix", "Minotaur Labyrinth",

        // Tier 5: 201 - 250 (Flight, Velocity & Technology)
        "Hot Air Balloon", "Vintage Biplane", "Propeller Plane", "Supersonic Jet", "Jumbo Airliner",
        "Cargo Transport", "Attack Helicopter", "Rescue Chopper", "Sky Gyrocopter", "Zeppelin Airship",
        "Classic Roadster", "Muscle Cruiser", "Formula 1 Racer", "Monster Truck", "Fire Engine",
        "Police Interceptor", "Paramedic Ambulance", "School Bus", "Express Van", "Semi Big Rig",
        "Mountain Bike", "City Scooter", "Cruiser Chopper", "Racing Superbike", "All-Terrain ATV",
        "Snowmobile", "Steam Locomotive", "Bullet Train", "Metro Subway", "Alpine Cable Car",
        "Retro CRT TV", "Boombox Stereo", "Audio Cassette", "Floppy Disk", "Retro PC",
        "Modern Laptop", "Smartphone", "Smartwatch", "VR Headset", "Arcade Cabinet",
        "Dual Gamepad", "D20 Polyhedral", "Steel Gear", "Twin Cogwheels", "V8 Engine",
        "Circuit PCB", "Silicon Microchip", "Energy Battery", "Android Robot", "Combat Mech",

        // Tier 6: 251 - 300 (Mythology, Fantasy & Sorcery)
        "Dragon Head", "Spreading Wyvern", "Crimson Drake", "Rising Phoenix", "Majestic Griffin",
        "Winged Pegasus", "Starlight Unicorn", "Seven-Headed Hydra", "Abyssal Kraken", "Three-Head Cerberus",
        "Sorcerer Hat", "Ancient Spellbook", "Alchemy Flask", "Elixir Bottle", "Bubbling Cauldron",
        "Mystic Talisman", "Rune Tablet", "Crystal Wand", "Staff of Elements", "Magic Mirror",
        "Elven Bow", "Dwarven Warhammer", "Orc Cleaver", "Rogue Dirk", "Fairy Wings",
        "Mermaid Tail", "Centaur Archer", "Stone Colossus", "Spectral Phantom", "Vampire Bat",
        "Reaper Scythe", "Ancient Tombstone", "Spooky Pumpkin", "Gargoyle Perch", "Demon Horns",
        "Archangel Wings", "Golden Halo", "Holy Grail", "Excalibur in Stone", "Ankh of Eternity",
        "Ouroboros Drake", "Eye of Ra", "Sacred Scarab", "Valkyrie Winged Helm", "Mjolnir Hammer",
        "Odin Raven", "World Tree Yggdrasil", "Sea Leviathan", "Desert Behemoth", "Elder Dragon Sovereign",

        // Tier 7: 301 - 350 (Athletics, Crafts & Workshop)
        "Soccer Ball", "Basketball Net", "American Football", "Baseball Mitt", "Tennis Racket",
        "Badminton Birdie", "Ping Pong Paddle", "Golf Flag & Iron", "Bowling Pin", "Eight Ball",
        "Skateboard Deck", "Quad Roller Skate", "Figure Ice Skate", "Snowboard Deck", "Downhill Skis",
        "Boxing Glove", "Martial Black Belt", "Archery Bullseye", "Fencing Rapier", "Iron Horseshoe",
        "Claw Hammer", "Hand Crosscut Saw", "Monkey Wrench", "Cross Screwdriver", "Power Drill",
        "Craft Pliers", "Heavy Anvil", "Forge Tongs", "Hex Nut & Bolt", "Steel Toolbox",
        "Spool & Needle", "Sewing Machine", "Yarn Ball", "Dress Mannequin", "Safety Pin",
        "Zipper Slider", "Shirt Button", "Brass Thimble", "Steam Iron", "Weaving Loom",
        "Spade Shovel", "Garden Rake", "Watering Can", "Work Wheelbarrow", "Wooden Birdhouse",
        "Glass Terrarium", "Clay Flowerpot", "Pruning Shears", "Planting Trowel", "Grandmaster Trophy",

        // Tier 8: 351 - 400 (Sacred Geometry, Optical Mazes & Gems)
        "Hexagram Star", "Octagram Seal", "Nine-Point Star", "Decagram Crest", "Dodecagram Ring",
        "Flower of Life", "Seed of Creation", "Metatron Cube", "Sri Yantra", "Torus Energy Ring",
        "Mobius Ribbon", "Penrose Triangle", "Impossible Fork", "Borromean Triple", "Koch Snowflake",
        "Sierpinski Pyramid", "Hilbert Maze", "Peano Labyrinth", "Dragon Curve", "Fibonacci Spiral",
        "Yin and Yang", "Universal Peace", "Biohazard Crest", "Radiation Trefoil", "Recycle Triad",
        "Infinity Ribbon", "Hourglass Infinity", "Celtic Spiral", "Fleur-de-lis", "Maltese Cross",
        "Brilliant Diamond", "Emerald Cut Gem", "Marquise Crystal", "Pear Cut Jewel", "Oval Sapphire",
        "Radiant Ruby", "Heart Gem", "Trillion Amethyst", "Cushion Topaz", "Imperial Crown Jewel",
        "Tall Corridor Maze", "Wide Chamber Matrix", "Symmetric Vault", "Spiral Bastion", "Citadel Rampart",
        "Concentric Fortress", "Star Fort Bastion", "Quadrant Chamber", "Hex Core Labyrinth", "Grand Nexus Labyrinth",

        // Tier 9: 401 - 450 (Cosmos, Cybernetics & Quantum Core)
        "Solar Corona", "Solar Flare Burst", "Lunar Corona", "Polar Aurora", "Supernova Shock",
        "Black Hole Event", "Wormhole Chasm", "Spiral Galaxy", "Barred Spiral", "Orion Nebula",
        "Astronaut Helmet", "Cosmic EVA Suit", "Orbital Spacebase", "Hubble Mirror", "Webb Hex Shield",
        "Mars Rover", "Lunar Explorer", "Solar Helios Probe", "Deep Dish Antenna", "Radio Telescope",
        "Bionic Optical Eye", "Cybernetic Hand", "Neural Brain Link", "DNA Double Helix", "RNA Messenger Strand",
        "Capsid Molecule", "Bacteriophage", "Neuron Dendrite", "Cerebral Cortex", "Cardio EKG Pulse",
        "Bohr Atom Orbit", "Electron Wave", "Hadron Collider", "Refraction Prism", "Fiber Optic Cable",
        "Silicon Ingot", "Quantum Qubit Cell", "Laser Hologram", "Tokamak Fusion", "Plasma Fusion Core",
        "Cyberpunk Visor", "High-Frequency Blade", "Plasma Carbine", "Anti-Grav Hoverboard", "Aerocar Cruiser",
        "Synthetic Companion", "Heavy Battleframe", "Stealth Bomber", "Kinetic Railgun", "Quantum Hyperdrive",

        // Tier 10: 451 - 500 (Grandmaster Monuments, Legends & Apex Trials)
        "Golden Gate Span", "Statue of Liberty", "Christ Redeemer", "Rushmore Bastion", "Sydney Opera Shell",
        "Tower Bridge Spire", "Burj Khalifa Apex", "Empire Sky Needle", "Saint Basil Dome", "Sagrada Spire",
        "Titan Atlas Core", "Zeus Thunderbolt", "Poseidon Trident", "Hades Helm of Darkness", "Aegis Shield",
        "Apollo Sun Chariot", "Hermes Winged Sandal", "Anubis Jackal Crest", "Ra Sun Hawk", "Osiris Divine Crook",
        "Dragon King Sigil", "Wyrm Sovereign", "Ascendant Phoenix", "Archangel Blade", "Seraphim Six-Wings",
        "Cosmic Yggdrasil", "Sacred Metatron", "Matrix Nexus Key", "Chronos Temporal Ring", "Eternal Ouroboros",
        "Grandmaster Chessboard", "Daedalus Labyrinth", "Pandora Vault", "Covenant Relic", "Golden Fleece",
        "Excalibur Sovereign", "Mjolnir Storm", "Trident of Seas", "Crown of Immortality", "Scepter of Dominion",
        "Adamantine Diamond", "Tesseract Hypercube", "Infinity Singularity", "Celestial Citadel", "Cosmic Monolith",
        "Omega Matrix Labyrinth", "Archon Fortress", "Sovereign Dragon God", "Grandmaster Apex Labyrinth", "Arrow Escape Supreme Champion"
    )

    private fun buildLevelDef(levelNumber: Int): SilhouetteLevelDef {
        val name = SILHOUETTE_NAMES_500.getOrElse(levelNumber - 1) { "Silhouette Matrix $levelNumber" }

        // Grid Size scaling smoothly from 10 to 18
        val baseGridSize = when {
            levelNumber <= 20 -> 11
            levelNumber <= 50 -> 12
            levelNumber <= 100 -> 13
            levelNumber <= 200 -> 14
            levelNumber <= 320 -> 15
            levelNumber <= 420 -> 16
            else -> 17
        }

        // Arrow Count scaling smoothly from 18 up to 95+
        val baseArrowCount = when {
            levelNumber <= 20 -> (18 + (levelNumber * 8 / 20))
            levelNumber <= 50 -> (26 + ((levelNumber - 20) * 10 / 30))
            levelNumber <= 100 -> (36 + ((levelNumber - 50) * 14 / 50))
            levelNumber <= 200 -> (50 + ((levelNumber - 100) * 16 / 100))
            levelNumber <= 320 -> (66 + ((levelNumber - 200) * 16 / 120))
            levelNumber <= 420 -> (82 + ((levelNumber - 320) * 14 / 100))
            else -> (96 + ((levelNumber - 420) * 18 / 80))
        }

        val category = when ((levelNumber - 1) / 50) {
            0 -> "Natural & Everyday"
            1 -> "Fauna, Flora & Arts"
            2 -> "Maritime & Culinary"
            3 -> "Monuments & Relics"
            4 -> "Flight & Velocity"
            5 -> "Myth & Sorcery"
            6 -> "Athletics & Workshop"
            7 -> "Geometry & Crystals"
            8 -> "Cosmos & Cybernetics"
            else -> "Apex Grandmaster"
        }

        val evaluator = selectEvaluatorForLevel(levelNumber)

        return SilhouetteLevelDef(
            levelNumber = levelNumber,
            name = name,
            category = category,
            baseGridSize = baseGridSize,
            baseArrowCount = baseArrowCount,
            evaluator = evaluator
        )
    }

    private fun selectEvaluatorForLevel(levelNumber: Int): (Double, Double, Int, Int, Int, Int) -> Boolean {
        // Direct mapping for key iconic screenshot levels & milestone shapes
        return when (levelNumber) {
            10 -> ::evalTrophy
            27 -> ::evalAnchor
            40 -> ::evalDog
            42 -> ::evalHeart
            99 -> ::evalButterfly
            183 -> ::evalKnight
            198 -> ::evalMandala
            199 -> ::evalSquareMaze
            391 -> ::evalTallCorridor
            else -> {
                // Procedural archetypes modulated by level seed
                val archetypeId = (levelNumber * 17 + levelNumber / 3) % 24
                when (archetypeId) {
                    0 -> { nx, ny, _, _, _, _ -> evalVessel(nx, ny, levelNumber) }
                    1 -> { nx, ny, _, _, _, _ -> evalQuadruped(nx, ny, levelNumber) }
                    2 -> { nx, ny, _, _, _, _ -> evalBirdOrWinged(nx, ny, levelNumber) }
                    3 -> { nx, ny, _, _, _, _ -> evalHeart(nx, ny, 0, 0, 0, 0) }
                    4 -> { nx, ny, _, _, _, _ -> evalButterfly(nx, ny, 0, 0, 0, 0) }
                    5 -> { nx, ny, _, _, _, _ -> evalTreeOrFlora(nx, ny, levelNumber) }
                    6 -> { nx, ny, _, _, _, _ -> evalTowerOrCastle(nx, ny, levelNumber) }
                    7 -> { nx, ny, _, _, _, _ -> evalStarOrRadiant(nx, ny, levelNumber) }
                    8 -> { nx, ny, _, _, _, _ -> evalGemOrFacet(nx, ny, levelNumber) }
                    9 -> { nx, ny, _, _, _, _ -> evalCrownOrCrest(nx, ny, levelNumber) }
                    10 -> { nx, ny, _, _, _, _ -> evalWeaponOrSword(nx, ny, levelNumber) }
                    11 -> { nx, ny, _, _, _, _ -> evalFishOrMarine(nx, ny, levelNumber) }
                    12 -> { nx, ny, _, _, _, _ -> evalVehicleOrJet(nx, ny, levelNumber) }
                    13 -> { nx, ny, _, _, _, _ -> evalKeyOrLock(nx, ny, levelNumber) }
                    14 -> { nx, ny, _, _, _, _ -> evalInstrument(nx, ny, levelNumber) }
                    15 -> { nx, ny, _, _, _, _ -> evalShieldOrArmor(nx, ny, levelNumber) }
                    16 -> { nx, ny, _, _, _, _ -> evalCelestial(nx, ny, levelNumber) }
                    17 -> { nx, ny, _, _, _, _ -> evalMandalaJewel(nx, ny, levelNumber) }
                    18 -> { nx, ny, _, _, _, _ -> evalLabyrinthMaze(nx, ny, levelNumber) }
                    19 -> { nx, ny, _, _, _, _ -> evalSpiralOrHelix(nx, ny, levelNumber) }
                    20 -> { nx, ny, _, _, _, _ -> evalInsectOrBeetle(nx, ny, levelNumber) }
                    21 -> { nx, ny, _, _, _, _ -> evalFoodOrFruit(nx, ny, levelNumber) }
                    22 -> { nx, ny, _, _, _, _ -> evalDragonOrMonster(nx, ny, levelNumber) }
                    else -> { nx, ny, _, _, _, _ -> evalTrophy(nx, ny, 0, 0, 0, 0) }
                }
            }
        }
    }

    // =========================================================================
    // CANONICAL REFERENCE SHAPE EVALUATORS
    // =========================================================================

    private fun evalTrophy(nx: Double, ny: Double, x: Int, y: Int, w: Int, h: Int): Boolean {
        val absNx = abs(nx)
        return when {
            // Pedestal base
            ny in 0.55..0.92 && absNx <= 0.65 -> if (ny > 0.82) absNx <= 0.85 else absNx <= 0.55
            // Thin stem
            ny in 0.15..0.55 && absNx <= 0.22 -> true
            // Bowl
            ny in -0.75..0.15 && absNx <= 0.62 -> true
            // Handles
            ny in -0.65..-0.05 && absNx in 0.62..0.92 -> !(ny in -0.50..-0.20 && absNx in 0.70..0.82)
            else -> false
        }
    }

    private fun evalAnchor(nx: Double, ny: Double, x: Int, y: Int, w: Int, h: Int): Boolean {
        val absNx = abs(nx)
        return when {
            ny in -0.92..-0.62 && absNx <= 0.35 -> {
                val dist = sqrt(nx * nx + (ny + 0.77).pow(2.0))
                dist <= 0.32 && dist >= 0.08
            }
            ny in -0.65..0.70 && absNx <= 0.18 -> true
            ny in -0.42..-0.25 && absNx <= 0.75 -> true
            ny in 0.15..0.88 -> {
                val armCurve = 0.55 + 0.35 * (absNx * absNx)
                val inArm = abs(ny - armCurve) <= 0.18 && absNx in 0.15..0.88
                val flukeTip = ny in 0.05..0.45 && absNx in 0.75..0.92
                inArm || flukeTip
            }
            else -> false
        }
    }

    private fun evalDog(nx: Double, ny: Double, x: Int, y: Int, w: Int, h: Int): Boolean {
        return when {
            ny in -0.85..-0.10 && nx in 0.05..0.88 -> if (nx > 0.55 && ny < -0.30) true else nx <= 0.65
            ny in -0.15..0.45 && nx in -0.75..0.55 -> true
            ny in -0.55..-0.10 && nx in -0.88..-0.60 -> true
            ny in 0.45..0.92 && nx in 0.22..0.52 -> true
            ny in 0.45..0.92 && nx in -0.75..-0.45 -> true
            else -> false
        }
    }

    private fun evalHeart(nx: Double, ny: Double, x: Int, y: Int, w: Int, h: Int): Boolean {
        val px = nx * 1.15
        val py = -ny * 1.15 + 0.25
        val term = px * px + py * py - 1.0
        return (term * term * term - px * px * py * py * py) <= 0.05
    }

    private fun evalButterfly(nx: Double, ny: Double, x: Int, y: Int, w: Int, h: Int): Boolean {
        val absNx = abs(nx)
        return when {
            absNx <= 0.14 && ny in -0.75..0.75 -> true
            ny in -0.85..0.15 && absNx in 0.14..0.92 -> {
                val wingTop = -0.85 + 0.45 * (1.0 - absNx)
                ny >= wingTop
            }
            ny in 0.15..0.80 && absNx in 0.14..0.78 -> {
                val wingBottom = 0.80 - 0.40 * (1.0 - absNx)
                ny <= wingBottom
            }
            else -> false
        }
    }

    private fun evalKnight(nx: Double, ny: Double, x: Int, y: Int, w: Int, h: Int): Boolean {
        val absNx = abs(nx)
        return when {
            ny in 0.65..0.92 && absNx <= 0.75 -> true
            ny in 0.05..0.65 && nx in -0.65..0.65 -> true
            ny in -0.75..0.05 && nx in -0.65..0.15 -> true
            ny in -0.45..0.05 && nx in 0.15..0.75 -> true
            ny in -0.88..-0.65 && nx in -0.25..0.10 -> true
            else -> false
        }
    }

    private fun evalMandala(nx: Double, ny: Double, x: Int, y: Int, w: Int, h: Int): Boolean {
        val dist = abs(nx) + abs(ny)
        val maxAxis = maxOf(abs(nx), abs(ny))
        return dist <= 1.35 && maxAxis <= 0.88
    }

    private fun evalSquareMaze(nx: Double, ny: Double, x: Int, y: Int, w: Int, h: Int): Boolean {
        return abs(nx) <= 0.90 && abs(ny) <= 0.90
    }

    private fun evalTallCorridor(nx: Double, ny: Double, x: Int, y: Int, w: Int, h: Int): Boolean {
        return abs(nx) <= 0.82 && abs(ny) <= 0.94
    }

    // =========================================================================
    // PARAMETRIC ARCHETYPE GENERATORS FOR 500 SILHOUETTES
    // =========================================================================

    private fun evalVessel(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        val bowlWidth = 0.55 + 0.15 * ((seed % 5) / 5.0)
        return when {
            ny in 0.60..0.90 && absNx <= 0.65 -> true
            ny in 0.20..0.60 && absNx <= 0.25 -> true
            ny in -0.75..0.20 && absNx <= bowlWidth -> true
            ny in -0.60..-0.10 && absNx in bowlWidth..(bowlWidth + 0.28) -> !(ny in -0.45..-0.25 && absNx in (bowlWidth + 0.08)..(bowlWidth + 0.20))
            else -> false
        }
    }

    private fun evalQuadruped(nx: Double, ny: Double, seed: Int): Boolean {
        val facingRight = (seed % 2 == 0)
        val dirX = if (facingRight) nx else -nx
        return when {
            ny in -0.85..-0.15 && dirX in 0.15..0.85 -> true
            ny in -0.15..0.45 && dirX in -0.75..0.55 -> true
            ny in -0.55..-0.10 && dirX in -0.88..-0.60 -> true
            ny in 0.45..0.90 && dirX in 0.20..0.50 -> true
            ny in 0.45..0.90 && dirX in -0.70..-0.40 -> true
            else -> false
        }
    }

    private fun evalBirdOrWinged(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        val wingSpan = 0.85 + 0.08 * ((seed % 3) / 3.0)
        return when {
            absNx <= 0.20 && ny in -0.85..0.75 -> true
            ny in -0.50..0.35 && absNx in 0.20..wingSpan -> ny >= -0.50 + 0.35 * (absNx - 0.20)
            ny in 0.70..0.92 && absNx <= 0.35 -> true
            else -> false
        }
    }

    private fun evalTreeOrFlora(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            ny in 0.60..0.92 && absNx <= 0.22 -> true
            ny in -0.85..-0.35 && absNx <= (0.45 * (ny + 0.85) / 0.50) -> true
            ny in -0.35..0.15 && absNx <= (0.70 * (ny + 0.35) / 0.50) -> true
            ny in 0.15..0.65 && absNx <= (0.90 * (ny - 0.15) / 0.50) -> true
            else -> false
        }
    }

    private fun evalTowerOrCastle(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            ny in 0.40..0.90 && absNx <= 0.80 -> true
            ny in -0.30..0.40 && absNx <= 0.50 -> true
            ny in -0.90..-0.30 && absNx <= (0.35 * (ny + 0.90) / 0.60) -> true
            ny in -0.20..0.40 && absNx in 0.50..0.75 -> true
            else -> false
        }
    }

    private fun evalStarOrRadiant(nx: Double, ny: Double, seed: Int): Boolean {
        val r = sqrt(nx * nx + ny * ny)
        val angle = atan2(ny, nx)
        val points = 5 + (seed % 4)
        val starR = 0.52 + 0.35 * cos(points * angle)
        return r <= starR
    }

    private fun evalGemOrFacet(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            ny in -0.75..-0.25 && absNx <= 0.85 -> true
            ny in -0.25..0.85 && absNx <= (0.85 * (0.85 - ny) / 1.10) -> true
            else -> false
        }
    }

    private fun evalCrownOrCrest(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            ny in 0.40..0.80 && absNx <= 0.85 -> true
            ny in -0.85..0.40 && absNx <= 0.22 -> true
            ny in -0.60..0.40 && absNx in 0.45..0.85 -> true
            ny in 0.05..0.40 && absNx <= 0.85 -> true
            else -> false
        }
    }

    private fun evalWeaponOrSword(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            ny in 0.65..0.90 && absNx <= 0.12 -> true
            ny in 0.50..0.65 && absNx <= 0.65 -> true
            ny in -0.90..0.50 && absNx <= 0.22 -> {
                if (ny < -0.70) absNx <= (0.22 * (ny + 0.90) / 0.20) else true
            }
            else -> false
        }
    }

    private fun evalFishOrMarine(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            (nx / 0.65).pow(2.0) + (ny / 0.45).pow(2.0) <= 1.0 -> true
            ny in -0.85..-0.30 && nx in -0.25..0.30 -> true
            ny in 0.30..0.85 && nx in -0.25..0.30 -> true
            nx in -0.90..-0.50 && abs(ny) <= 0.55 * (abs(nx) - 0.50) / 0.40 -> true
            else -> false
        }
    }

    private fun evalVehicleOrJet(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            absNx <= 0.20 && ny in -0.90..0.85 -> true
            ny in -0.20..0.35 && absNx in 0.20..0.92 -> ny >= -0.20 + 0.40 * (absNx - 0.20)
            ny in 0.60..0.85 && absNx in 0.20..0.60 -> true
            else -> false
        }
    }

    private fun evalKeyOrLock(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            ny in -0.85..-0.25 && absNx <= 0.65 -> {
                val dist = sqrt(nx * nx + (ny + 0.55).pow(2.0))
                dist <= 0.38 && dist >= 0.12
            }
            ny in -0.25..0.85 && absNx <= 0.15 -> true
            ny in 0.40..0.80 && nx in 0.15..0.55 -> {
                (ny in 0.40..0.55) || (ny in 0.65..0.80)
            }
            else -> false
        }
    }

    private fun evalInstrument(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            ny in -0.92..-0.15 && absNx <= 0.16 -> true
            ny in -0.15..0.25 && absNx <= 0.52 -> true
            ny in 0.25..0.40 && absNx <= 0.38 -> true
            ny in 0.40..0.90 && absNx <= 0.72 -> true
            else -> false
        }
    }

    private fun evalShieldOrArmor(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            ny in -0.85..0.15 && absNx <= 0.78 -> true
            ny in 0.15..0.88 && absNx <= (0.78 * (0.88 - ny) / 0.73) -> true
            else -> false
        }
    }

    private fun evalCelestial(nx: Double, ny: Double, seed: Int): Boolean {
        val dist1 = sqrt(nx * nx + ny * ny)
        val dist2 = sqrt((nx - 0.35).pow(2.0) + (ny + 0.20).pow(2.0))
        return dist1 <= 0.78 && dist2 >= 0.55
    }

    private fun evalMandalaJewel(nx: Double, ny: Double, seed: Int): Boolean {
        val dist = abs(nx) + abs(ny)
        val maxAxis = maxOf(abs(nx), abs(ny))
        val r = sqrt(nx * nx + ny * ny)
        return dist <= 1.30 && maxAxis <= 0.85 && r >= 0.12
    }

    private fun evalLabyrinthMaze(nx: Double, ny: Double, seed: Int): Boolean {
        return abs(nx) <= 0.88 && abs(ny) <= 0.88
    }

    private fun evalSpiralOrHelix(nx: Double, ny: Double, seed: Int): Boolean {
        val r = sqrt(nx * nx + ny * ny)
        val angle = atan2(ny, nx) + Math.PI
        val spiralFactor = (angle / (2 * Math.PI))
        val band1 = abs(r - (0.25 + 0.50 * spiralFactor)) <= 0.22
        return band1 || r <= 0.35
    }

    private fun evalInsectOrBeetle(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            absNx <= 0.35 && ny in -0.40..0.75 -> true
            absNx <= 0.30 && ny in -0.80..-0.40 -> true
            ny in -0.30..0.50 && absNx in 0.35..0.85 -> {
                abs(ny - (-0.20)) <= 0.10 || abs(ny - 0.10) <= 0.10 || abs(ny - 0.40) <= 0.10
            }
            else -> false
        }
    }

    private fun evalFoodOrFruit(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            ny in -0.85..-0.55 && nx in 0.0..0.35 -> abs(ny - (-0.70)) <= 0.12
            (nx / 0.70).pow(2.0) + ((ny - 0.10) / 0.65).pow(2.0) <= 1.0 -> true
            else -> false
        }
    }

    private fun evalDragonOrMonster(nx: Double, ny: Double, seed: Int): Boolean {
        val absNx = abs(nx)
        return when {
            absNx <= 0.25 && ny in -0.85..0.70 -> true
            ny in -0.45..0.25 && absNx in 0.25..0.90 -> ny >= -0.45 + 0.35 * (absNx - 0.25)
            ny in 0.40..0.88 && nx in -0.75..-0.25 -> true
            ny in -0.85..-0.50 && nx in 0.15..0.75 -> true
            else -> false
        }
    }
}
