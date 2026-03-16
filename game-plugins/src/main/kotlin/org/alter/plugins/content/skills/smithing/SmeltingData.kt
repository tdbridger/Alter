package org.alter.plugins.content.skills.smithing

data class SmeltingEntry(
    val bar: String,
    val level: Int,
    val experience: Double,
    val ores: List<OreRequirement>,
) {
    @Transient var barId: Int = -1
}

data class OreRequirement(
    val item: String,
    val amount: Int,
) {
    @Transient var itemId: Int = -1
}
