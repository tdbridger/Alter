package org.alter.plugins.content.skills.cooking

data class CookingEntry(
    val raw: String,
    val cooked: String,
    val burnt: String,
    val level: Int,
    val experience: Double,
    val burnStop: Int = 99, // level at which you stop burning
) {
    @Transient var rawId: Int = -1
    @Transient var cookedId: Int = -1
    @Transient var burntId: Int = -1
}
