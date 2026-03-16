package org.alter.plugins.content.skills.mining

import dev.openrune.cache.CacheManager.getObject
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

class MiningPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        loadService(MiningService())

        onWorldInit {
            val service = world.getService(MiningService::class.java) ?: return@onWorldInit

            service.entries.forEach { entry ->
                entry.objectIds.forEach { objId ->
                    val mineOptions = getObject(objId).actions.filterNotNull().filter {
                        it.equals("mine", ignoreCase = true)
                    }
                    mineOptions.forEach { option ->
                        onObjOption(obj = objId, option = option) {
                            val obj = player.getInteractingGameObj()
                            player.queue { mine(player, obj, entry, world) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun QueueTask.mine(player: Player, obj: GameObject, entry: MiningEntry, world: World) {
        if (player.getSkills().getBaseLevel(Skills.MINING) < entry.level) {
            player.message("You need a Mining level of ${entry.level} to mine this rock.")
            return
        }

        val pick = findPickaxe(player)
        if (pick == null) {
            player.message("You do not have a pickaxe which you have the Mining level to use.")
            return
        }

        if (player.inventory.isFull) {
            player.message("Your inventory is too full to hold any more ore.")
            return
        }

        player.faceTile(obj.tile)
        player.message("You swing your pick at the rock.")

        player.lock()
        try {
            while (true) {
                if (world.getObject(obj.tile, obj.type) == null || world.getObject(obj.tile, obj.type)?.id != obj.id) {
                    break
                }

                player.animate(pick.animation)
                wait(pick.speed)

                val level = player.getSkills().getBaseLevel(Skills.MINING)
                val chance = calculateSuccess(level, entry.level, pick)
                if (Math.random() < chance) {
                    val loot = rollLoot(entry)
                    val amount = if (loot.min == loot.max) loot.min else loot.min + (Math.random() * (loot.max - loot.min + 1)).toInt()

                    if (player.inventory.isFull) {
                        player.message("Your inventory is too full to hold any more ore.")
                        break
                    }

                    player.inventory.add(loot.itemId, amount)
                    player.addXp(Skills.MINING, entry.experience)
                    player.message("You manage to mine some ore.")

                    val emptyRock = DynamicObject(entry.emptyObjectId, obj.type, obj.rot, obj.tile)
                    world.remove(obj)
                    world.spawn(emptyRock)

                    world.queue {
                        wait(entry.respawnTicks)
                        world.remove(emptyRock)
                        world.spawn(DynamicObject(obj.id, obj.type, obj.rot, obj.tile))
                    }
                    break
                }

                if (player.inventory.isFull) {
                    player.message("Your inventory is too full to hold any more ore.")
                    break
                }
            }
        } finally {
            player.animate(-1)
            player.unlock()
        }
    }

    private fun findPickaxe(player: Player): Pickaxe? {
        val miningLevel = player.getSkills().getBaseLevel(Skills.MINING)
        return Pickaxe.values().reversed().firstOrNull { pick ->
            miningLevel >= pick.levelReq && (
                player.equipment.contains(pick.itemId) ||
                player.inventory.contains(pick.itemId)
            )
        }
    }

    private fun calculateSuccess(playerLevel: Int, rockLevel: Int, pick: Pickaxe): Double {
        val levelDiff = playerLevel - rockLevel
        val base = 0.3 + (levelDiff * 0.02) + ((Pickaxe.values().indexOf(pick)) * 0.05)
        return min(0.95, base.coerceIn(0.1, 0.95))
    }

    private fun rollLoot(entry: MiningEntry): MiningLoot {
        if (entry.ores.size == 1) return entry.ores.first()
        val total = entry.ores.sumOf { it.weight }
        val roll = Math.random() * total
        var cumulative = 0.0
        entry.ores.forEach { loot ->
            cumulative += loot.weight
            if (roll < cumulative) return loot
        }
        return entry.ores.last()
    }
}
