package org.alter.plugins.content.skills.woodcutting

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.entity.GameObject
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import kotlin.math.min

class WoodcuttingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        loadService(WoodcuttingService())

        onWorldInit {
            val service = world.getService(WoodcuttingService::class.java) ?: return@onWorldInit

            service.entries.forEach { entry ->
                entry.objectIds.forEach { objId ->
                    val chopOptions = dev.openrune.cache.CacheManager.getObject(objId).actions.filterNotNull().filter {
                        it.equals("chop down", ignoreCase = true) || it.equals("chop", ignoreCase = true)
                    }
                    chopOptions.forEach { option ->
                        onObjOption(obj = objId, option = option) {
                            val obj = player.getInteractingGameObj()
                            player.queue { chop(player, obj, entry, world) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun QueueTask.chop(player: Player, obj: GameObject, entry: WoodcuttingEntry, world: World) {
        // Check level
        if (player.getSkills().getBaseLevel(Skills.WOODCUTTING) < entry.level) {
            player.message("You need a Woodcutting level of ${entry.level} to chop this tree.")
            return
        }

        // Find best axe
        val axe = findAxe(player)
        if (axe == null) {
            player.message("You do not have an axe which you have the Woodcutting level to use.")
            return
        }

        // Check inventory space
        if (player.inventory.isFull) {
            player.message("Your inventory is too full to hold any more logs.")
            return
        }

        player.faceTile(obj.tile)
        player.message("You swing your axe at the tree.")

        player.lock()
        try {
            while (true) {
                // Check tree still exists
                if (world.getObject(obj.tile, obj.type) == null || world.getObject(obj.tile, obj.type)?.id != obj.id) {
                    break
                }

                // Play chop animation
                player.animate(axe.animation)
                wait(axe.speed)

                // Roll for success based on level
                val level = player.getSkills().getBaseLevel(Skills.WOODCUTTING)
                val chance = calculateSuccess(level, entry.level, axe)
                if (Math.random() < chance) {
                    // Give logs
                    val loot = rollLoot(entry)
                    val amount = if (loot.min == loot.max) loot.min else loot.min + (Math.random() * (loot.max - loot.min + 1)).toInt()

                    if (player.inventory.isFull) {
                        player.message("Your inventory is too full to hold any more logs.")
                        break
                    }

                    player.inventory.add(loot.itemId, amount)
                    player.addXp(Skills.WOODCUTTING, entry.experience)
                    player.message("You get some logs.")

                    // Deplete tree → stump
                    val stump = DynamicObject(entry.emptyObjectId, obj.type, obj.rot, obj.tile)
                    world.remove(obj)
                    world.spawn(stump)

                    // Respawn tree after delay
                    world.queue {
                        wait(entry.respawnTicks)
                        world.remove(stump)
                        world.spawn(DynamicObject(obj.id, obj.type, obj.rot, obj.tile))
                    }
                    break
                }

                // Failed roll — keep chopping
                if (player.inventory.isFull) {
                    player.message("Your inventory is too full to hold any more logs.")
                    break
                }
            }
        } finally {
            player.animate(-1) // stop animation
            player.unlock()
        }
    }

    private fun findAxe(player: Player): Axe? {
        val wcLevel = player.getSkills().getBaseLevel(Skills.WOODCUTTING)
        // Check equipped weapon first, then inventory, best axe first
        return Axe.values().reversed().firstOrNull { axe ->
            wcLevel >= axe.levelReq && (
                player.equipment.contains(axe.itemId) ||
                player.inventory.contains(axe.itemId)
            )
        }
    }

    private fun calculateSuccess(playerLevel: Int, treeLevel: Int, axe: Axe): Double {
        // Higher level and better axe = better chance
        val levelDiff = playerLevel - treeLevel
        val base = 0.3 + (levelDiff * 0.02) + ((Axe.values().indexOf(axe)) * 0.05)
        return min(0.95, base.coerceIn(0.1, 0.95))
    }

    private fun rollLoot(entry: WoodcuttingEntry): WoodcuttingLoot {
        if (entry.logs.size == 1) return entry.logs.first()
        val total = entry.logs.sumOf { it.weight }
        val roll = Math.random() * total
        var cumulative = 0.0
        entry.logs.forEach { loot ->
            cumulative += loot.weight
            if (roll < cumulative) return loot
        }
        return entry.logs.last()
    }
}
