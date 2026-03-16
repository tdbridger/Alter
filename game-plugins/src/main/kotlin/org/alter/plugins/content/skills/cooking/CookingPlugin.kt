package org.alter.plugins.content.skills.cooking

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

class CookingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    // Cooking range / fire object names
    private val cookingObjects = listOf(
        "object.cooking_range", "object.range", "object.cooking_range_4172",
        "object.range_7183", "object.fire_3769", "object.fire_3775",
        "object.fire_4265", "object.fire_4266", "object.fire_5249"
    )

    init {
        loadService(CookingService())

        onWorldInit {
            val service = world.getService(CookingService::class.java) ?: return@onWorldInit

            // Register "use raw fish on range/fire"
            service.entries.forEach { entry ->
                cookingObjects.forEach { objName ->
                    try {
                        onItemOnObj(obj = objName, item = entry.raw) {
                            player.queue { cook(player, entry, world) }
                        }
                    } catch (e: Exception) {
                        // Object might not exist in cache
                    }
                }
            }
        }
    }

    private suspend fun QueueTask.cook(player: Player, entry: CookingEntry, world: World) {
        if (player.getSkills().getBaseLevel(Skills.COOKING) < entry.level) {
            player.message("You need a Cooking level of ${entry.level} to cook this.")
            return
        }

        val rawName = entry.raw.removePrefix("item.").replace("_", " ")
        player.message("You attempt to cook the $rawName.")
        player.lock()
        try {
            // Cook all raw fish in inventory
            while (player.inventory.contains(entry.rawId)) {
                player.animate(883) // cooking animation
                wait(4)

                if (!player.inventory.contains(entry.rawId)) break

                val level = player.getSkills().getBaseLevel(Skills.COOKING)
                val burnChance = calculateBurnChance(level, entry)

                player.inventory.remove(entry.rawId, 1)

                if (Math.random() < burnChance) {
                    player.inventory.add(entry.burntId, 1)
                    player.message("You accidentally burn the $rawName.")
                } else {
                    player.inventory.add(entry.cookedId, 1)
                    player.addXp(Skills.COOKING, entry.experience)
                    val cookedName = entry.cooked.removePrefix("item.").replace("_", " ")
                    player.message("You successfully cook the $rawName.")
                }
            }
        } finally {
            player.animate(-1)
            player.unlock()
        }
    }

    private fun calculateBurnChance(level: Int, entry: CookingEntry): Double {
        if (level >= entry.burnStop) return 0.0
        val range = entry.burnStop - entry.level
        val progress = level - entry.level
        return (1.0 - (progress.toDouble() / range)).coerceIn(0.05, 0.6)
    }
}
