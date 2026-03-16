package org.alter.plugins.content.mechanics.corrupted

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import kotlin.random.Random

/**
 * Corrupted Gielinor Phase F: Corruption Effects
 *
 * Hooks into the corruption timer to apply tier-based effects.
 * Call applyTickEffects() from the corruption timer each tick.
 */
object CorruptionEffects {

    // Town spawn locations for invasion NPCs
    private val TOWN_LOCATIONS = listOf(
        Tile(3222, 3218, 0),  // Lumbridge
        Tile(3212, 3422, 0),  // Varrock
        Tile(2964, 3378, 0),  // Falador
        Tile(3093, 3493, 0),  // Edgeville
        Tile(3105, 3249, 0),  // Draynor
        Tile(3293, 3174, 0),  // Al Kharid
    )

    // Thematic NPC IDs for invasion spawns
    private val WEAK_NPCS = intArrayOf(2854, 3017, 3018) // rats, spiders
    private val MEDIUM_NPCS = intArrayOf(70, 26, 85, 510)  // skeleton, zombie, ghost, dark wizard
    private val STRONG_NPCS = intArrayOf(2005, 2098, 2090) // lesser demon, hill giant, moss giant
    private val ELITE_NPCS = intArrayOf(2005, 2006, 2007)  // lesser demons

    /**
     * Get food healing multiplier based on corruption tier.
     */
    fun getFoodMultiplier(corruption: Int): Double {
        val tier = CorruptionPlugin.Companion.getTier(corruption)
        return when (tier) {
            0 -> 1.0; 1 -> 0.9; 2 -> 0.8; 3 -> 0.7; 4 -> 0.5; 5 -> 0.25; else -> 1.0
        }
    }

    /**
     * Get NPC combat stat multiplier based on corruption tier.
     */
    fun getNpcStatMultiplier(corruption: Int): Double {
        val tier = CorruptionPlugin.Companion.getTier(corruption)
        return when (tier) {
            0 -> 1.0; 1 -> 1.0; 2 -> 1.0
            3 -> 1.25; 4 -> 1.5; 5 -> 2.0; else -> 1.0
        }
    }

    /**
     * Called every corruption tick (every 60 seconds) to apply effects.
     */
    fun applyTickEffects(player: Player, world: World, corruption: Int) {
        val tier = CorruptionPlugin.Companion.getTier(corruption)

        // Tier 1+: Invasion spawns in towns and near player
        if (tier >= 1) {
            val townSpawns = when (tier) { 1 -> 2; 2 -> 4; 3 -> 6; 4 -> 10; 5 -> 15; else -> 1 }
            val nearSpawns = when (tier) { 1 -> 1; 2 -> 2; 3 -> 3; 4 -> 5; 5 -> 8; else -> 0 }

            repeat(townSpawns) { spawnInvasionNpc(world, corruption, TOWN_LOCATIONS.random(), 15) }
            repeat(nearSpawns) { spawnInvasionNpc(world, corruption, player.tile, 12) }
        }

        // Tier 2+: Passive prayer drain
        if (tier >= 2 && corruption % 2 == 0) {
            val currentPrayer = player.getSkills().getCurrentLevel(5) // Prayer = skill 5
            if (currentPrayer > 0) {
                player.getSkills().setCurrentLevel(5, currentPrayer - 1)
            }
        }
    }

    private fun spawnInvasionNpc(world: World, corruption: Int, centre: Tile, radius: Int) {
        val roll = Random.nextInt(100)
        val pool = when {
            corruption < 200 -> if (roll < 70) WEAK_NPCS else MEDIUM_NPCS
            corruption < 400 -> when {
                roll < 25 -> WEAK_NPCS; roll < 65 -> MEDIUM_NPCS; else -> STRONG_NPCS
            }
            corruption < 600 -> when {
                roll < 15 -> WEAK_NPCS; roll < 40 -> MEDIUM_NPCS; roll < 80 -> STRONG_NPCS; else -> ELITE_NPCS
            }
            else -> when {
                roll < 10 -> MEDIUM_NPCS; roll < 50 -> STRONG_NPCS; else -> ELITE_NPCS
            }
        }

        val npcId = pool.random()
        val offsetX = Random.nextInt(radius * 2) - radius
        val offsetZ = Random.nextInt(radius * 2) - radius
        val spawnTile = Tile(centre.x + offsetX, centre.z + offsetZ, centre.height)

        try {
            val npc = Npc(npcId, spawnTile, world)
            npc.respawns = false
            world.spawn(npc)
        } catch (e: Exception) {
            // Invalid spawn location, skip
        }
    }
}
