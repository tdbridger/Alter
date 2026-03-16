package org.alter.plugins.content.skills.mining

data class MiningEntry(
    val objects: List<String>,
    val emptyObject: String,
    val respawnTicks: Int = 5,
    val level: Int,
    val experience: Double,
    val ores: List<MiningLoot>,
) {
    @Transient var objectIds: IntArray = intArrayOf()
    @Transient var emptyObjectId: Int = -1
}

data class MiningLoot(
    val item: String,
    val min: Int = 1,
    val max: Int = min,
    val weight: Double = 1.0,
) {
    @Transient var itemId: Int = -1
}

enum class Pickaxe(
    val itemName: String,
    val levelReq: Int,
    val animation: Int,
    val speed: Int
) {
    BRONZE("item.bronze_pickaxe", 1, 625, 8),
    IRON("item.iron_pickaxe", 1, 626, 7),
    STEEL("item.steel_pickaxe", 6, 627, 6),
    BLACK("item.black_pickaxe", 11, 3873, 5),
    MITHRIL("item.mithril_pickaxe", 21, 629, 5),
    ADAMANT("item.adamant_pickaxe", 31, 628, 4),
    RUNE("item.rune_pickaxe", 41, 624, 3),
    DRAGON("item.dragon_pickaxe", 61, 7139, 2);

    @Transient var itemId: Int = -1
}
