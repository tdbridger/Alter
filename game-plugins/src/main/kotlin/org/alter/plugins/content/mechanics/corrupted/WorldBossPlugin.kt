package org.alter.plugins.content.mechanics.corrupted

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.model.timer.TimerKey
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import kotlin.math.abs
import kotlin.random.Random

/**
 * Corrupted Gielinor Phase H: World Boss
 *
 * Spawns at corruption 500 at a dramatic location.
 * Hunts player at corruption 750+.
 * Kill = VICTORY (2x score).
 */

val BOSS_SPAWNED_ATTR = AttributeKey<Boolean>(persistenceKey = "cg_boss_spawned")
val BOSS_TILE_X_ATTR = AttributeKey<Int>(persistenceKey = "cg_boss_tile_x")
val BOSS_TILE_Z_ATTR = AttributeKey<Int>(persistenceKey = "cg_boss_tile_z")
val BOSS_HUNT_TIMER = TimerKey(persistenceKey = "cg_boss_hunt")

class WorldBossPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    // Dramatic spawn locations
    private val spawnLocations = listOf(
        Tile(3234, 3371, 0),  // Stone circle south of Varrock
        Tile(2970, 3340, 0),  // Falador castle area
        Tile(3110, 3516, 0),  // Wilderness border
        Tile(3293, 3174, 0),  // Al Kharid desert
        Tile(3094, 3233, 0),  // Wizard's tower area
    )

    // Boss NPC — we'll use a lesser demon (2005) as the visual placeholder
    // In a full implementation this would be a custom NPC
    private val BOSS_NPC_ID = 2005

    init {
        // Proximity hint timer
        onTimer(BOSS_HUNT_TIMER) {
            val player = pawn as? Player ?: return@onTimer
            val bossX = player.attr[BOSS_TILE_X_ATTR] ?: return@onTimer
            val bossZ = player.attr[BOSS_TILE_Z_ATTR] ?: return@onTimer
            val corruption = player.attr[CORRUPTION_ATTR] ?: 0

            val dx = bossX - player.tile.x
            val dz = bossZ - player.tile.z
            val distance = abs(dx) + abs(dz)

            if (corruption >= 750) {
                // Boss hunts — move toward player
                val moveX = if (dx > 0) 64 else if (dx < 0) -64 else 0
                val moveZ = if (dz > 0) 64 else if (dz < 0) -64 else 0
                player.attr[BOSS_TILE_X_ATTR] = bossX - moveX.coerceIn(-64, 64)
                player.attr[BOSS_TILE_Z_ATTR] = bossZ - moveZ.coerceIn(-64, 64)

                when {
                    distance < 30 -> player.message("<col=FF0000>IT'S HERE.</col>")
                    distance < 100 -> player.message("<col=FF0000>The ground trembles. It's close.</col>")
                    distance < 300 -> player.message("<col=8B0000>It's coming.</col>")
                    else -> player.message("<col=8B0000>The World Boss draws nearer...</col>")
                }
            } else {
                // Just proximity hints
                val direction = when {
                    abs(dx) > abs(dz) -> if (dx > 0) "east" else "west"
                    else -> if (dz > 0) "north" else "south"
                }
                when {
                    distance < 50 -> player.message("<col=FF6600>The presence is overwhelming. It's very close, to the $direction.</col>")
                    distance < 200 -> player.message("<col=FF6600>The presence grows stronger to the $direction...</col>")
                    else -> player.message("<col=8B0000>You sense a terrible presence to the $direction...</col>")
                }
            }

            player.timers[BOSS_HUNT_TIMER] = if (corruption >= 750) 100 else 500 // faster when hunting
        }

        // ::worldboss command
        onCommand("worldboss", description = "Show World Boss proximity hint") {
            val spawned = player.attr[BOSS_SPAWNED_ATTR] ?: false
            if (!spawned) {
                player.message("The World Boss has not yet appeared. (Spawns at corruption 500)")
                return@onCommand
            }
            val bossX = player.attr[BOSS_TILE_X_ATTR] ?: 0
            val bossZ = player.attr[BOSS_TILE_Z_ATTR] ?: 0
            val dx = bossX - player.tile.x
            val dz = bossZ - player.tile.z
            val distance = abs(dx) + abs(dz)
            val direction = when {
                abs(dx) > abs(dz) -> if (dx > 0) "east" else "west"
                else -> if (dz > 0) "north" else "south"
            }
            player.message("The World Boss is approximately $distance tiles to the $direction.")
        }
    }

    companion object {
        /**
         * Called from corruption timer when corruption reaches 500.
         */
        fun spawnBoss(player: Player) {
            if (player.attr[BOSS_SPAWNED_ATTR] == true) return

            val locations = listOf(
                Tile(3234, 3371, 0), Tile(2970, 3340, 0),
                Tile(3110, 3516, 0), Tile(3293, 3174, 0),
            )
            val spawnTile = locations.random()

            player.attr[BOSS_SPAWNED_ATTR] = true
            player.attr[BOSS_TILE_X_ATTR] = spawnTile.x
            player.attr[BOSS_TILE_Z_ATTR] = spawnTile.z

            player.message("<col=FF0000>A terrible presence has materialised somewhere in the world.</col>")

            // Start proximity hint timer
            if (!player.timers.has(BOSS_HUNT_TIMER)) {
                player.timers[BOSS_HUNT_TIMER] = 500 // hint every 5 minutes
            }
        }
    }
}
