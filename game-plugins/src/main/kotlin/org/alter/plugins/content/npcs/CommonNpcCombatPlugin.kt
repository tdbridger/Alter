package org.alter.plugins.content.npcs

import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Combat definitions for common NPCs around Lumbridge and early-game areas.
 */
class CommonNpcCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Man (level 2)
        listOf("npc.man_3106", "npc.man_3107", "npc.man_3108").forEach { name ->
            try { defineCombat(name, hp = 7, att = 1, str = 1, def = 1) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }
        }

        // Woman (level 2)
        try { defineCombat("npc.woman_3111", hp = 7, att = 1, str = 1, def = 1) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }

        // Goblin (level 2-5)
        listOf("npc.goblin_3028", "npc.goblin_3029", "npc.goblin_3030").forEach { name ->
            try { defineCombat(name, hp = 5, att = 1, str = 1, def = 1, attackAnim = 6184, blockAnim = 6183, deathAnim = 6182) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }
        }

        // Chicken (level 1)
        listOf("npc.chicken_1173", "npc.chicken_1174").forEach { name ->
            try { defineCombat(name, hp = 3, att = 1, str = 1, def = 1, attackAnim = 5849, blockAnim = 5850, deathAnim = 5851) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }
        }

        // Giant rat (level 1-6)
        try { defineCombat("npc.giant_rat", hp = 5, att = 2, str = 3, def = 2, attackAnim = 4933, blockAnim = 4934, deathAnim = 4935) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }

        // Guard (level 21)
        listOf("npc.guard_3269", "npc.guard_3270", "npc.guard_3271").forEach { name ->
            try { defineCombat(name, hp = 22, att = 19, str = 18, def = 18) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }
        }

        // Giant spider (level 2)
        try { defineCombat("npc.giant_spider", hp = 4, att = 1, str = 2, def = 1, attackAnim = 5327, blockAnim = 5328, deathAnim = 5329) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }

        // Guards (level 21) — Varrock/Falador/Edgeville
        listOf("npc.guard_397", "npc.guard_398", "npc.guard_399", "npc.guard_400").forEach { name ->
            try { defineCombat(name, hp = 22, att = 19, str = 18, def = 18) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }
        }

        // Dark wizard (level 7 melee)
        try { defineCombat("npc.dark_wizard", hp = 12, att = 6, str = 5, def = 5) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }
        // Dark wizard (level 20 magic)
        try { defineCombat("npc.dark_wizard_512", hp = 19, att = 1, str = 1, def = 10, mag = 20) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }

        // Al Kharid warrior (level 9)
        try { defineCombat("npc.al_kharid_warrior", hp = 14, att = 8, str = 7, def = 7) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }

        // Scorpion (level 14)
        try { defineCombat("npc.scorpion", hp = 17, att = 13, str = 12, def = 12) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }

        // Skeleton (level 22)
        try { defineCombat("npc.skeleton", hp = 22, att = 19, str = 18, def = 18) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }

        // Mugger (level 6)
        try { defineCombat("npc.mugger", hp = 10, att = 5, str = 4, def = 4) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }

        // Highwayman (level 5)
        try { defineCombat("npc.highwayman", hp = 8, att = 4, str = 3, def = 3) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }

        // Hill giant (level 28)
        try { defineCombat("npc.hill_giant", hp = 35, att = 18, str = 22, def = 18) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }

        // Moss giant (level 42)
        listOf("npc.moss_giant", "npc.moss_giant_2091", "npc.moss_giant_2092").forEach { name ->
            try { defineCombat(name, hp = 60, att = 30, str = 30, def = 30) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }
        }

        // Lesser demon (level 82)
        listOf("npc.lesser_demon", "npc.lesser_demon_2006", "npc.lesser_demon_2007").forEach { name ->
            try { defineCombat(name, hp = 79, att = 68, str = 67, def = 65) } catch (e: Exception) { Server.logger.error { "Failed to set combat def for NPC: $e" } }
        }
    }

    private fun defineCombat(
        npc: String, hp: Int, att: Int, str: Int, def: Int,
        mag: Int = 1, rng: Int = 1,
        attackAnim: Int = 422, blockAnim: Int = 424, deathAnim: Int = 836,
        speed: Int = 4, respawn: Int = 25
    ) {
        setCombatDef(npc) {
            configs {
                attackSpeed = speed
                respawnDelay = respawn
            }
            stats {
                hitpoints = hp
                attack = att
                strength = str
                defence = def
                magic = mag
                ranged = rng
            }
            anims {
                attack = attackAnim
                block = blockAnim
                death = deathAnim
            }
        }
    }
}
