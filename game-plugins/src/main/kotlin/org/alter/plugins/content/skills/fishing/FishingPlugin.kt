package org.alter.plugins.content.skills.fishing

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class FishingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        loadService(FishingService())

        onWorldInit {
            val service = world.getService(FishingService::class.java) ?: return@onWorldInit

            // Register fishing spot handlers
            service.entries.forEach { entry ->
                entry.npcs.forEach { npcName ->
                    try {
                        onNpcOption(npcName, option = entry.option) {
                            player.queue { fish(player, entry, world) }
                        }
                    } catch (e: Exception) {
                        // NPC doesn't have this option, skip
                    }
                }
            }
        }
    }

    private suspend fun QueueTask.fish(player: Player, entry: FishingSpotEntry, world: World) {
        // Check fishing level against highest fish we can catch
        if (player.getSkills().getBaseLevel(Skills.FISHING) < entry.level) {
            player.message("You need a Fishing level of ${entry.level} to fish here.")
            return
        }

        // Check tool
        if (!player.inventory.contains(entry.toolId) && !player.equipment.contains(entry.toolId)) {
            player.message("You need a ${entry.tool.removePrefix("item.")} to fish here.")
            return
        }

        // Check bait
        if (entry.baitId != -1 && !player.inventory.contains(entry.baitId)) {
            player.message("You don't have any bait.")
            return
        }

        if (player.inventory.isFull) {
            player.message("Your inventory is too full to hold any more fish.")
            return
        }

        player.message("You cast out your ${entry.tool.removePrefix("item.").replace("_", " ")}...")

        player.lock()
        try {
            while (true) {
                player.animate(entry.animation)
                wait(5) // fishing tick

                // Roll for catch
                val level = player.getSkills().getBaseLevel(Skills.FISHING)

                // Find the best fish we can catch at our level
                val catchable = entry.fish.filter { level >= it.level }
                if (catchable.isEmpty()) break

                val chance = 0.3 + (level - entry.level) * 0.015
                if (Math.random() < chance.coerceIn(0.1, 0.9)) {
                    // Pick a fish weighted by level proximity
                    val caught = rollFish(catchable)

                    if (player.inventory.isFull) {
                        player.message("Your inventory is too full to hold any more fish.")
                        break
                    }

                    // Consume bait
                    if (entry.baitId != -1) {
                        player.inventory.remove(entry.baitId, 1)
                        if (!player.inventory.contains(entry.baitId)) {
                            player.message("You've run out of bait.")
                            player.inventory.add(caught.itemId, 1)
                            player.addXp(Skills.FISHING, caught.experience)
                            player.message("You catch a ${caught.item.removePrefix("item.").replace("_", " ")}.")
                            break
                        }
                    }

                    player.inventory.add(caught.itemId, 1)
                    player.addXp(Skills.FISHING, caught.experience)
                    player.message("You catch a ${caught.item.removePrefix("item.").replace("_", " ")}.")

                    if (player.inventory.isFull) {
                        player.message("Your inventory is too full to hold any more fish.")
                        break
                    }
                }

                // Check tool still in inventory
                if (!player.inventory.contains(entry.toolId) && !player.equipment.contains(entry.toolId)) {
                    break
                }
            }
        } finally {
            player.animate(-1)
            player.unlock()
        }
    }

    private fun rollFish(catchable: List<FishingLoot>): FishingLoot {
        if (catchable.size == 1) return catchable.first()
        val total = catchable.sumOf { it.weight }
        val roll = Math.random() * total
        var cumulative = 0.0
        catchable.forEach { fish ->
            cumulative += fish.weight
            if (roll < cumulative) return fish
        }
        return catchable.last()
    }
}
