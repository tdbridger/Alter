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
            try { defineCombat(name, hp = 7, att = 1, str = 1, def = 1) } catch (e: Exception) {}
        }

        // Woman (level 2)
        try { defineCombat("npc.woman_3111", hp = 7, att = 1, str = 1, def = 1) } catch (e: Exception) {}

        // Goblin (level 2-5)
        listOf("npc.goblin_3028", "npc.goblin_3029", "npc.goblin_3030").forEach { name ->
            try { defineCombat(name, hp = 5, att = 1, str = 1, def = 1, attackAnim = 6184, blockAnim = 6183, deathAnim = 6182) } catch (e: Exception) {}
        }

        // Chicken (level 1)
        listOf("npc.chicken", "npc.chicken_1401").forEach { name ->
            try { defineCombat(name, hp = 3, att = 1, str = 1, def = 1, attackAnim = 5849, blockAnim = 5850, deathAnim = 5851) } catch (e: Exception) {}
        }

        // Giant rat (level 1-6)
        try { defineCombat("npc.giant_rat", hp = 5, att = 2, str = 3, def = 2, attackAnim = 4933, blockAnim = 4934, deathAnim = 4935) } catch (e: Exception) {}

        // Guard (level 21)
        listOf("npc.guard_3269", "npc.guard_3270", "npc.guard_3271").forEach { name ->
            try { defineCombat(name, hp = 22, att = 19, str = 18, def = 18) } catch (e: Exception) {}
        }

        // Giant spider (level 2)
        try { defineCombat("npc.giant_spider", hp = 4, att = 1, str = 2, def = 1, attackAnim = 5327, blockAnim = 5328, deathAnim = 5329) } catch (e: Exception) {}
    }

    private fun defineCombat(
        npc: String, hp: Int, att: Int, str: Int, def: Int,
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
                magic = 1
                ranged = 1
            }
            anims {
                attack = attackAnim
                block = blockAnim
                death = deathAnim
            }
        }
    }
}
