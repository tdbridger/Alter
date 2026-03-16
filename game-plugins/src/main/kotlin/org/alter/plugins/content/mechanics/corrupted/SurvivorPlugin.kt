package org.alter.plugins.content.mechanics.corrupted

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
 * Corrupted Gielinor Phase J: Survivor NPCs
 *
 * 10-15 randomly generated named NPCs that share the doomed world.
 * They gather, trade, fight, flee, and eventually die or turn hostile.
 */

val SURVIVORS_ATTR = AttributeKey<String>(persistenceKey = "cg_survivors") // serialized survivor data
val SURVIVOR_TIMER = TimerKey(persistenceKey = "cg_survivor_timer")

class SurvivorPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        // Spawn locations for survivors — spread across the world
        private val SPAWN_AREAS = listOf(
            Tile(3222, 3218, 0),  // Lumbridge
            Tile(3212, 3422, 0),  // Varrock
            Tile(2964, 3378, 0),  // Falador
            Tile(3093, 3493, 0),  // Edgeville
            Tile(3105, 3249, 0),  // Draynor
            Tile(3293, 3174, 0),  // Al Kharid
            Tile(2813, 3447, 0),  // Catherby
            Tile(2894, 3465, 0),  // Taverley
        )

        /**
         * Generate survivors for a new run. Called from archetype selection.
         */
        fun generateSurvivors(player: Player): List<SurvivorInfo> {
            val count = Random.nextInt(10, 16)
            val usedNpcIds = mutableSetOf<Int>()
            val usedNames = mutableSetOf<String>()
            val survivors = mutableListOf<SurvivorInfo>()

            for (i in 0 until count) {
                val survivor = SurvivorInfo.generate(usedNpcIds, usedNames)
                val area = SPAWN_AREAS[i % SPAWN_AREAS.size]
                survivor.spawnX = area.x + Random.nextInt(-15, 16)
                survivor.spawnZ = area.z + Random.nextInt(-15, 16)
                survivors.add(survivor)
            }

            // Serialize and store
            saveSurvivors(player, survivors)
            return survivors
        }

        fun saveSurvivors(player: Player, survivors: List<SurvivorInfo>) {
            val data = survivors.joinToString(";") { s ->
                "${s.name}|${s.title}|${s.npcId}|${s.personality.name}|${s.affinity.name}|${s.combatLevel}|${s.survivalRating}|${s.state.name}|${s.hostilityRolled}|${s.isHostile}|${s.isDead}|${s.spawnX}|${s.spawnZ}"
            }
            player.attr[SURVIVORS_ATTR] = data
        }

        fun loadSurvivors(player: Player): List<SurvivorInfo> {
            val data = player.attr[SURVIVORS_ATTR] ?: return emptyList()
            if (data.isBlank()) return emptyList()
            return data.split(";").mapNotNull { entry ->
                try {
                    val parts = entry.split("|")
                    SurvivorInfo(
                        name = parts[0], title = parts[1], npcId = parts[2].toInt(),
                        personality = SurvivorPersonality.valueOf(parts[3]),
                        affinity = SurvivorAffinity.valueOf(parts[4]),
                        combatLevel = parts[5].toInt(), survivalRating = parts[6].toInt(),
                        state = SurvivorState.valueOf(parts[7]),
                        hostilityRolled = parts[8].toBoolean(),
                        isHostile = parts[9].toBoolean(),
                        isDead = parts[10].toBoolean(),
                        spawnX = parts[11].toInt(), spawnZ = parts[12].toInt()
                    )
                } catch (e: Exception) { null }
            }
        }

        /**
         * Process survivor AI tick — called periodically.
         * Handles death curve, hostility transitions, state changes.
         */
        fun processSurvivorTick(player: Player, corruption: Int) {
            val survivors = loadSurvivors(player)
            if (survivors.isEmpty()) return
            var changed = false

            for (survivor in survivors) {
                if (survivor.isDead) continue

                // Death curve check (corruption-scaled survival roll)
                val deathChance = calculateDeathChance(survivor, corruption)
                if (Random.nextDouble() < deathChance) {
                    survivor.isDead = true
                    survivor.state = SurvivorState.DEAD
                    changed = true

                    // Direction from player
                    val dx = survivor.spawnX - player.tile.x
                    val dz = survivor.spawnZ - player.tile.z
                    val dir = when {
                        abs(dx) > abs(dz) -> if (dx > 0) "east" else "west"
                        else -> if (dz > 0) "north" else "south"
                    }
                    player.message("<col=FF0000>A scream echoes from the $dir... ${survivor.name} has fallen.</col>")
                    continue
                }

                // Hostility transition check
                if (!survivor.hostilityRolled && corruption >= survivor.personality.hostilityThreshold) {
                    survivor.hostilityRolled = true
                    changed = true
                    if (Random.nextDouble() < survivor.personality.hostilityChance) {
                        survivor.isHostile = true
                        survivor.state = SurvivorState.HOSTILE
                        player.message("<col=FF6600>${survivor.name} has turned hostile!</col>")
                    }
                }

                // State transitions based on corruption
                if (!survivor.isHostile) {
                    val tier = CorruptionPlugin.Companion.getTier(corruption)
                    survivor.state = when {
                        tier >= 3 -> SurvivorState.DESPERATE
                        else -> SurvivorState.GATHERING
                    }
                    changed = true
                }
            }

            if (changed) saveSurvivors(player, survivors)
        }

        private fun calculateDeathChance(survivor: SurvivorInfo, corruption: Int): Double {
            if (corruption < 50) return 0.0
            // Higher corruption = higher death chance, modified by survival rating
            val baseDanger = corruption.toDouble() / 1000.0
            val survivalModifier = survivor.survivalRating.toDouble() / 100.0
            return (baseDanger * (1.0 - survivalModifier * 0.5)).coerceIn(0.0, 0.15)
        }
    }

    init {
        // Start survivor timer when archetype chosen
        onLogin {
            val chosen = player.attr[ARCHETYPE_CHOSEN_ATTR] ?: false
            val hasSurvivors = (player.attr[SURVIVORS_ATTR] ?: "").isNotBlank()
            if (chosen && hasSurvivors && !player.timers.has(SURVIVOR_TIMER)) {
                player.timers[SURVIVOR_TIMER] = 500 // check every 5 minutes
            }
        }

        // Survivor AI tick
        onTimer(SURVIVOR_TIMER) {
            val player = pawn as? Player ?: return@onTimer
            val corruption = player.attr[CORRUPTION_ATTR] ?: 0
            processSurvivorTick(player, corruption)
            player.timers[SURVIVOR_TIMER] = 500
        }

        // ::survivors command
        onCommand("survivors", description = "Show survivor status") {
            val survivors = loadSurvivors(player)
            if (survivors.isEmpty()) {
                player.message("No survivors in this run.")
                return@onCommand
            }

            val alive = survivors.count { !it.isDead }
            val hostile = survivors.count { it.isHostile && !it.isDead }
            player.message("")
            player.message("<col=FFD700>=== Survivors: $alive alive ($hostile hostile) ===</col>")
            player.message("")
            for (s in survivors) {
                val status = when {
                    s.isDead -> "<col=FF0000>[DEAD]</col>"
                    s.isHostile -> "<col=FF6600>[HOSTILE]</col>"
                    s.state == SurvivorState.DESPERATE -> "<col=FFAA00>[DESPERATE]</col>"
                    else -> "<col=00FF00>[${s.state.name}]</col>"
                }
                val area = getAreaName(s.spawnX, s.spawnZ)
                player.message("$status ${s.fullName} (Lvl ${s.combatLevel}) — $area")
            }
            player.message("")
        }

    }

    private fun getAreaName(x: Int, z: Int): String {
        return when {
            abs(x - 3222) < 50 && abs(z - 3218) < 50 -> "Lumbridge"
            abs(x - 3212) < 50 && abs(z - 3422) < 50 -> "Varrock"
            abs(x - 2964) < 50 && abs(z - 3378) < 50 -> "Falador"
            abs(x - 3093) < 50 && abs(z - 3493) < 50 -> "Edgeville"
            abs(x - 3105) < 50 && abs(z - 3249) < 50 -> "Draynor"
            abs(x - 3293) < 50 && abs(z - 3174) < 50 -> "Al Kharid"
            abs(x - 2813) < 50 && abs(z - 3447) < 50 -> "Catherby"
            abs(x - 2894) < 50 && abs(z - 3465) < 50 -> "Taverley"
            else -> "Unknown"
        }
    }
}
