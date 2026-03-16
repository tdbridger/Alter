package org.alter.plugins.content.skills.fishing

data class FishingSpotEntry(
    val npcs: List<String>,
    val option: String,          // "net", "bait", "lure", "cage", "harpoon"
    val level: Int,
    val experience: Double,
    val tool: String,            // required tool item
    val bait: String? = null,    // required bait item (consumed)
    val fish: List<FishingLoot>,
    val animation: Int = 621,
) {
    @Transient var npcIds: IntArray = intArrayOf()
    @Transient var toolId: Int = -1
    @Transient var baitId: Int = -1
}

data class FishingLoot(
    val item: String,
    val level: Int,
    val experience: Double,
    val weight: Double = 1.0,
) {
    @Transient var itemId: Int = -1
}
