package org.alter.plugins.content.skills.firemaking

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.move.moveTo
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

enum class LogType(
    val logItem: String,
    val level: Int,
    val experience: Double,
    val fireObj: String = "object.fire_26185",
    val burnTicks: Int = 150, // how long fire lasts
) {
    NORMAL("item.logs", 1, 40.0),
    OAK("item.oak_logs", 15, 60.0),
    WILLOW("item.willow_logs", 30, 90.0),
    MAPLE("item.maple_logs", 45, 135.0),
    YEW("item.yew_logs", 60, 202.5),
    MAGIC("item.magic_logs", 75, 303.8);

    @Transient var logId: Int = -1
    @Transient var fireId: Int = -1
}

class FiremakingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onWorldInit {
            // Resolve IDs
            LogType.values().forEach { log ->
                log.logId = getRSCM(log.logItem)
                log.fireId = getRSCM(log.fireObj)
            }

            // Register "use tinderbox on logs" and "use logs on tinderbox"
            LogType.values().forEach { log ->
                onItemOnItem(item1 = "item.tinderbox", item2 = log.logItem) {
                    player.queue { light(player, log, world) }
                }
            }
        }
    }

    private suspend fun QueueTask.light(player: Player, log: LogType, world: World) {
        if (player.getSkills().getBaseLevel(Skills.FIREMAKING) < log.level) {
            player.message("You need a Firemaking level of ${log.level} to burn these logs.")
            return
        }

        val logName = log.logItem.removePrefix("item.").replace("_", " ")
        player.message("You attempt to light the $logName.")

        player.lock()
        try {
            player.animate(733) // fire lighting animation
            wait(4)

            // Remove logs
            if (!player.inventory.contains(log.logId)) {
                return
            }
            player.inventory.remove(log.logId, 1)

            // Spawn fire at player's tile
            val fireTile = player.tile
            val fire = DynamicObject(log.fireId, 10, 0, fireTile)
            world.spawn(fire)

            player.addXp(Skills.FIREMAKING, log.experience)
            player.message("The fire catches and the logs begin to burn.")

            // Move player west by 1 tile
            player.moveTo(fireTile.x - 1, fireTile.z, fireTile.height)

            // Remove fire after it burns out
            world.queue {
                wait(log.burnTicks)
                world.remove(fire)
                // Spawn ashes
                val ashesId = getRSCM("item.ashes")
                world.spawn(org.alter.game.model.entity.GroundItem(
                    item = ashesId, amount = 1, tile = fireTile,
                    owner = null
                ))
            }
        } finally {
            player.animate(-1)
            player.unlock()
        }
    }
}
