package org.alter.plugins.content.mechanics.corrupted

import org.alter.api.Skills

/**
 * Skill affinity — the archetype's core strength.
 */
enum class Affinity(
    val displayName: String,
    val boostedSkills: IntArray,
    val xpMultiplier: Double,
    val startingGear: List<Pair<Int, Int>>, // itemId to amount
    val strengthRating: Int
) {
    MELEE("Melee specialist", intArrayOf(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE), 3.0,
        listOf(1333 to 1, 1105 to 1, 1151 to 1), 8),
    RANGED("Ranged specialist", intArrayOf(Skills.RANGED), 3.0,
        listOf(861 to 1, 890 to 1000, 1135 to 1, 1099 to 1, 1065 to 1), 7),
    ARCANE("Arcane specialist", intArrayOf(Skills.MAGIC, Skills.RUNECRAFTING), 3.0,
        listOf(1381 to 1, 4089 to 1, 4091 to 1, 4093 to 1, 556 to 500, 555 to 500, 557 to 500, 554 to 500), 7),
    DEVOUT("Devout", intArrayOf(Skills.PRAYER), 5.0,
        listOf(536 to 500, 1718 to 1), 5),
    ARTISAN("Artisan", intArrayOf(Skills.MINING, Skills.SMITHING, Skills.CRAFTING), 3.0,
        listOf(1275 to 1, 2347 to 1, 1755 to 1, 444 to 50), 4),
    GATHERER("Gatherer", intArrayOf(Skills.FISHING, Skills.WOODCUTTING, Skills.COOKING), 3.0,
        listOf(311 to 1, 1359 to 1, 590 to 1, 377 to 50), 4),
    SHADOW("Shadow", intArrayOf(Skills.THIEVING, Skills.AGILITY), 4.0,
        listOf(1523 to 1, 2491 to 1, 995 to 10000), 5),
    HERBALIST("Herbalist", intArrayOf(Skills.HERBLORE, Skills.FARMING), 4.0,
        listOf(249 to 200, 227 to 100, 5343 to 1, 5295 to 50), 4),
    SURVIVALIST("Survivalist", intArrayOf(Skills.HITPOINTS, Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.RANGED, Skills.MAGIC), 1.5,
        listOf(385 to 20, 2446 to 5, 8007 to 3), 6),
    GENERALIST("Generalist", IntArray(0), 1.5,
        listOf(1359 to 1, 1275 to 1, 563 to 100, 556 to 500, 995 to 50000), 6),
    BRUTE("Brute", intArrayOf(Skills.STRENGTH), 5.0,
        listOf(4153 to 1, 3751 to 1, 1725 to 1), 5);

    fun getAdjective(): String = when (this) {
        MELEE -> "Warrior"; RANGED -> "Archer"; ARCANE -> "Sorcerer"
        DEVOUT -> "Devout"; ARTISAN -> "Artisan"; GATHERER -> "Gatherer"
        SHADOW -> "Shadow"; HERBALIST -> "Herbalist"; SURVIVALIST -> "Survivor"
        GENERALIST -> "Wanderer"; BRUTE -> "Brute"
    }
}

/**
 * Restriction — what the archetype cannot do.
 */
enum class Restriction(
    val displayName: String,
    val description: String,
    val difficultyWeight: Int
) {
    NO_BODY_ARMOUR("No Body Armour", "Cannot equip anything in the body slot", 6),
    NO_SHIELD("No Shield", "Cannot equip shields or defenders", 3),
    NO_MAGIC("No Magic", "Spellbook locked, cannot use runes", 7),
    NO_RANGED("No Ranged", "Cannot equip ranged weapons or ammunition", 5),
    NO_MELEE_WEAPONS("No Melee Weapons", "Can only punch", 9),
    NO_SHOPS("No Shops", "Cannot buy from any NPC shop", 5),
    NO_BANKING_UNTIL_100("No Banking Until Corruption 100", "Banks locked until corruption 100", 7),
    NO_RUNNING("No Running", "Walk speed only, permanently", 8),
    NO_PRAYER("No Prayer", "Prayer trains but no prayers can be activated", 7),
    NO_TELEPORTING("No Teleporting", "No teleport spells, tabs, or jewellery", 6),
    HALF_FOOD("Half Food", "All food heals 50% less", 4),
    GLASS_CANNON("Glass Cannon", "Max HP permanently halved", 8),
    CLUMSY_HANDS("Clumsy Hands", "10% chance to drop held item on any action", 3),
    SLOW_LEARNER("Slow Learner", "One random combat skill gets 0.25x XP rate", 4),
    HUNTED("Hunted", "All NPCs within 15 tiles are aggressive", 6);

    fun getFlavour(): String = when (this) {
        NO_BODY_ARMOUR -> "Unarmoured"; NO_SHIELD -> "Shieldless"
        NO_MAGIC -> "Mundane"; NO_RANGED -> "Grounded"
        NO_MELEE_WEAPONS -> "Fistfighter"; NO_SHOPS -> "Outcast"
        NO_BANKING_UNTIL_100 -> "Vagabond"; NO_RUNNING -> "Plodding"
        NO_PRAYER -> "Faithless"; NO_TELEPORTING -> "Earthbound"
        HALF_FOOD -> "Hungry"; GLASS_CANNON -> "Fragile"
        CLUMSY_HANDS -> "Butterfingers"; SLOW_LEARNER -> "Slow"
        HUNTED -> "Hunted"
    }
}

/**
 * Starting location with difficulty weight.
 */
enum class StartLocation(
    val displayName: String,
    val x: Int, val z: Int,
    val difficultyWeight: Int
) {
    LUMBRIDGE("Lumbridge", 3222, 3218, 0),
    VARROCK("Varrock", 3212, 3422, 1),
    FALADOR("Falador", 2964, 3378, 1),
    EDGEVILLE("Edgeville", 3093, 3493, 2),
    DRAYNOR("Draynor Village", 3105, 3249, 2),
    CATHERBY("Catherby", 2813, 3447, 3),
    TAVERLEY("Taverley", 2894, 3465, 3),
    BURTHORPE("Burthorpe", 2926, 3559, 4),
    CANIFIS("Canifis", 3496, 3488, 6),
    ARDOUGNE("Ardougne", 2662, 3305, 3),
    KARAMJA("Karamja (Brimhaven)", 2760, 3175, 5),
    AL_KHARID("Al Kharid", 3293, 3174, 2),
    WILDERNESS("Graveyard of Shadows", 3174, 3672, 9);
}

val ARCHETYPE_NAMES = listOf(
    "Kael", "Vera", "Grimm", "Sable", "Thorne", "Wren", "Ash", "Dusk",
    "Corvin", "Lyra", "Fenwick", "Mira", "Bram", "Isolde", "Rorik",
    "Calla", "Draven", "Nia", "Holt", "Sage", "Ember", "Flint",
    "Orin", "Talia", "Voss", "Bryn", "Maren", "Silas", "Eira", "Quinn"
)

// Affinity-restriction contradictions
val AFFINITY_CONTRADICTIONS = mapOf(
    Affinity.MELEE to setOf(Restriction.NO_MELEE_WEAPONS),
    Affinity.RANGED to setOf(Restriction.NO_RANGED),
    Affinity.ARCANE to setOf(Restriction.NO_MAGIC),
    Affinity.DEVOUT to setOf(Restriction.NO_PRAYER),
    Affinity.BRUTE to setOf(Restriction.NO_MELEE_WEAPONS),
    Affinity.SURVIVALIST to setOf(Restriction.GLASS_CANNON),
    Affinity.HERBALIST to setOf(Restriction.NO_SHOPS)
)
