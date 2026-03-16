package org.alter.plugins.content.mechanics.corrupted

import kotlin.random.Random

enum class SurvivorPersonality(
    val displayName: String,
    val weight: Int,          // roll weight out of 100
    val baseSurvival: Int,
    val hostilityThreshold: Int, // corruption level to roll hostility
    val hostilityChance: Double, // chance to actually turn
    val tradeWillingness: Double // 0.0 = never trades, 1.0 = always trades
) {
    PEACEFUL("Peaceful", 40, 20, 600, 0.10, 0.9),
    INDUSTRIOUS("Industrious", 25, 50, 500, 0.30, 0.6),
    AGGRESSIVE("Aggressive", 20, 70, 400, 0.70, 0.3),
    PARANOID("Paranoid", 10, 60, Int.MAX_VALUE, 0.0, 0.1),
    FERAL("Feral", 5, 80, 0, 1.0, 0.0);

    companion object {
        fun rollPersonality(): SurvivorPersonality {
            val roll = Random.nextInt(100)
            var cumulative = 0
            for (p in values()) {
                cumulative += p.weight
                if (roll < cumulative) return p
            }
            return PEACEFUL
        }
    }
}

enum class SurvivorAffinity(val displayName: String) {
    COMBAT("Fighter"),
    MINING("Miner"),
    FISHING("Fisher"),
    WOODCUTTING("Woodcutter"),
    HERBLORE("Herbalist"),
    THIEVING("Thief"),
    GENERAL("Wanderer");

    companion object {
        fun rollAffinity(): SurvivorAffinity = values().random()
    }
}

enum class SurvivorState {
    GATHERING, TRADING, FLEEING, FIGHTING, DESPERATE, HOSTILE, DEAD
}

val SURVIVOR_NAMES = listOf(
    "Grimshaw", "Elena", "Thorne", "Sable", "Wren", "Dax", "Ironjaw", "Bramble",
    "Fenn", "Mira", "Rorik", "Isolde", "Kael", "Vera", "Holt", "Sage",
    "Ember", "Flint", "Orin", "Talia", "Bryn", "Maren", "Silas", "Eira",
    "Quinn", "Aldric", "Greta", "Voss", "Calla", "Draven", "Nia", "Corvin",
    "Lyra", "Fenwick", "Bram", "Ash", "Dusk", "Red Claw", "Sister Mara",
    "Old Greta", "The Whisperer", "Farrow", "Wynn", "Cedric", "Elara",
    "Tobias", "Ravenna", "Osric", "Lilith", "Magnus", "Petra", "Garrick",
    "Nessa", "Ulric", "Brigid", "Alaric", "Rowena", "Tormund", "Ysolde"
)

// Human-looking NPC IDs from the OSRS cache (men, women, warriors, monks, etc.)
val SURVIVOR_NPC_IDS = intArrayOf(
    3106, 3107, 3108, 3109, // men
    3111, 3112, 3113,       // women
    1158, 3260,             // warriors
    1164, 1165, 1166, 1167, // monks
    659, 660,               // farmers
    1086, 1087,             // dwarves
    5442, 5443,             // adventurers
    3014, 3015,             // various humans
)

data class SurvivorInfo(
    val name: String,
    val title: String,
    val npcId: Int,
    val personality: SurvivorPersonality,
    val affinity: SurvivorAffinity,
    val combatLevel: Int,
    val survivalRating: Int,
    var state: SurvivorState = if (personality == SurvivorPersonality.FERAL) SurvivorState.HOSTILE else SurvivorState.GATHERING,
    var hostilityRolled: Boolean = personality == SurvivorPersonality.FERAL,
    var isHostile: Boolean = personality == SurvivorPersonality.FERAL,
    var isDead: Boolean = false,
    var spawnX: Int = 0,
    var spawnZ: Int = 0,
) {
    val fullName: String get() = "$name, $title"

    companion object {
        fun generate(usedNpcIds: MutableSet<Int>, usedNames: MutableSet<String>): SurvivorInfo {
            val personality = SurvivorPersonality.rollPersonality()
            val affinity = SurvivorAffinity.rollAffinity()

            // Pick unique name
            var name: String
            do { name = SURVIVOR_NAMES.random() } while (name in usedNames)
            usedNames.add(name)

            // Pick unique NPC appearance
            var npcId: Int
            do { npcId = SURVIVOR_NPC_IDS.random() } while (npcId in usedNpcIds)
            usedNpcIds.add(npcId)

            // Generate title
            val title = "${personality.displayName} ${affinity.displayName}"

            // Combat level
            val baseLevel = Random.nextInt(20, 81)
            val combatLevel = when (personality) {
                SurvivorPersonality.AGGRESSIVE -> baseLevel + 20
                SurvivorPersonality.FERAL -> baseLevel + 20
                else -> baseLevel
            }.coerceAtMost(126)

            // Survival rating
            val survivalRating = personality.baseSurvival + (combatLevel / 3)

            return SurvivorInfo(
                name = name, title = title, npcId = npcId,
                personality = personality, affinity = affinity,
                combatLevel = combatLevel, survivalRating = survivalRating
            )
        }
    }
}
