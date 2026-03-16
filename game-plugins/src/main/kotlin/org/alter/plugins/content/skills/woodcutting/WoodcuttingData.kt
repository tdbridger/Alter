package org.alter.plugins.content.skills.woodcutting

data class WoodcuttingEntry(
    val objects: List<String>,
    val emptyObject: String,
    val respawnTicks: Int = 10,
    val level: Int,
    val experience: Double,
    val logs: List<WoodcuttingLoot>,
) {
    @Transient var objectIds: IntArray = intArrayOf()
    @Transient var emptyObjectId: Int = -1

    init {
        require(objects.isNotEmpty())
        require(emptyObject.isNotBlank())
        require(level >= 1)
        require(experience >= 0.0)
        require(logs.isNotEmpty())
    }
}

data class WoodcuttingLoot(
    val item: String,
    val min: Int = 1,
    val max: Int = min,
    val weight: Double = 1.0,
) {
    @Transient var itemId: Int = -1
}

/**
 * Axe definitions with level requirements, animations, and speed bonuses.
 */
enum class Axe(
    val itemName: String,
    val levelReq: Int,
    val animation: Int,
    val speed: Int // lower = faster
) {
    BRONZE("item.bronze_axe", 1, 879, 8),
    IRON("item.iron_axe", 1, 877, 7),
    STEEL("item.steel_axe", 6, 875, 6),
    BLACK("item.black_axe", 11, 873, 5),
    MITHRIL("item.mithril_axe", 21, 871, 5),
    ADAMANT("item.adamant_axe", 31, 869, 4),
    RUNE("item.rune_axe", 41, 867, 3),
    DRAGON("item.dragon_axe", 61, 2846, 2);

    @Transient var itemId: Int = -1
}
