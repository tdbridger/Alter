package org.alter.plugins.content.skills.smithing

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Smelting: use ore on furnace → bar
 */
class SmeltingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val furnaceObjects = listOf(
        "object.furnace", "object.furnace_2966", "object.furnace_3294",
        "object.furnace_4304", "object.furnace_10082", "object.furnace_11009",
        "object.furnace_11010", "object.furnace_6189", "object.furnace_6190"
    )

    init {
        loadService(SmeltingService())

        onWorldInit {
            val service = world.getService(SmeltingService::class.java) ?: return@onWorldInit

            // Register "use ore on furnace"
            service.entries.forEach { entry ->
                entry.ores.forEach { ore ->
                    furnaceObjects.forEach { furnaceName ->
                        try {
                            onItemOnObj(obj = furnaceName, item = ore.item) {
                                player.queue { smelt(player, entry) }
                            }
                        } catch (e: Exception) {}
                    }
                }
            }
        }
    }

    private suspend fun QueueTask.smelt(player: Player, entry: SmeltingEntry) {
        if (player.getSkills().getBaseLevel(Skills.SMITHING) < entry.level) {
            player.message("You need a Smithing level of ${entry.level} to smelt this.")
            return
        }

        val barName = entry.bar.removePrefix("item.").replace("_", " ")
        val startTile = player.tile

        while (true) {
            if (player.tile != startTile) break

            // Check all ore requirements
            val hasAllOres = entry.ores.all { ore ->
                player.inventory.getItemCount(ore.itemId) >= ore.amount
            }
            if (!hasAllOres) {
                player.message("You don't have the required ores.")
                break
            }

            if (player.inventory.isFull && !player.inventory.contains(entry.barId)) {
                player.message("Your inventory is too full.")
                break
            }

            player.animate(899) // smelting animation
            wait(4)
            if (player.tile != startTile) break

            // Remove ores
            entry.ores.forEach { ore ->
                player.inventory.remove(ore.itemId, ore.amount)
            }

            // Add bar
            player.inventory.add(entry.barId, 1)
            player.addXp(Skills.SMITHING, entry.experience)
            player.message("You smelt a $barName.")
        }
        player.animate(-1)
    }
}
