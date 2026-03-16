package org.alter.plugins.content.npcs

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.GroundItem
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM
import java.lang.ref.WeakReference

/**
 * Common NPC drop tables for basic gameplay.
 * Bones + simple drops for Lumbridge-area NPCs.
 */
class CommonNpcDropsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Cows drop bones, raw beef, cowhide
        onNpcDeath("npc.cow") { dropItems(npc, listOf(526 to 1, 2132 to 1, 1739 to 1)) }

        // Chickens drop bones, raw chicken, feather
        listOf("npc.chicken", "npc.chicken_1401").forEach { name ->
            try {
                onNpcDeath(name) { dropItems(npc, listOf(526 to 1, 2138 to 1, 314 to 5)) }
            } catch (e: Exception) {}
        }

        // Goblins drop bones, coins
        listOf("npc.goblin_3028", "npc.goblin_3029", "npc.goblin_3030").forEach { name ->
            try {
                onNpcDeath(name) {
                    val drops = mutableListOf(526 to 1) // bones
                    if (Math.random() < 0.5) drops.add(995 to (1..25).random()) // coins
                    dropItems(npc, drops)
                }
            } catch (e: Exception) {}
        }

        // Men/Women drop bones, coins
        listOf("npc.man_3106", "npc.man_3107", "npc.man_3108", "npc.woman_3111").forEach { name ->
            try {
                onNpcDeath(name) {
                    val drops = mutableListOf(526 to 1)
                    if (Math.random() < 0.7) drops.add(995 to (1..30).random())
                    dropItems(npc, drops)
                }
            } catch (e: Exception) {}
        }

        // Guards drop bones, coins
        listOf("npc.guard_3269", "npc.guard_3270", "npc.guard_3271").forEach { name ->
            try {
                onNpcDeath(name) {
                    val drops = mutableListOf(526 to 1)
                    drops.add(995 to (10..50).random())
                    if (Math.random() < 0.1) drops.add(1153 to 1) // iron med helm
                    dropItems(npc, drops)
                }
            } catch (e: Exception) {}
        }

        // Giant rats drop bones, raw rat meat
        try {
            onNpcDeath("npc.giant_rat") { dropItems(npc, listOf(526 to 1, 2134 to 1)) }
        } catch (e: Exception) {}

        // Spiders drop nothing special (no bones for small spiders)

        // Giant spiders drop bones
        try {
            onNpcDeath("npc.giant_spider") { dropItems(npc, listOf(526 to 1)) }
        } catch (e: Exception) {}
    }

    private fun dropItems(npc: Npc, drops: List<Pair<Int, Int>>) {
        val killer = npc.damageMap.getMostDamage() as? Player
        drops.forEach { (itemId, amount) ->
            npc.world.spawn(GroundItem(
                item = itemId,
                amount = amount,
                tile = npc.tile,
                owner = killer
            ))
        }
    }
}
