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
        listOf("npc.chicken_1173", "npc.chicken_1174").forEach { name ->
            try {
                onNpcDeath(name) { dropItems(npc, listOf(526 to 1, 2138 to 1, 314 to 5)) }
            } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }
        }

        // Goblins drop bones, coins
        listOf("npc.goblin_3028", "npc.goblin_3029", "npc.goblin_3030").forEach { name ->
            try {
                onNpcDeath(name) {
                    val drops = mutableListOf(526 to 1) // bones
                    if (Math.random() < 0.5) drops.add(995 to (1..25).random()) // coins
                    dropItems(npc, drops)
                }
            } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }
        }

        // Men/Women drop bones, coins
        listOf("npc.man_3106", "npc.man_3107", "npc.man_3108", "npc.woman_3111").forEach { name ->
            try {
                onNpcDeath(name) {
                    val drops = mutableListOf(526 to 1)
                    if (Math.random() < 0.7) drops.add(995 to (1..30).random())
                    dropItems(npc, drops)
                }
            } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }
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
            } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }
        }

        // Giant rats drop bones, raw rat meat
        try {
            onNpcDeath("npc.giant_rat") { dropItems(npc, listOf(526 to 1, 2134 to 1)) }
        } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }

        // Spiders drop nothing special (no bones for small spiders)

        // Giant spiders drop bones
        try {
            onNpcDeath("npc.giant_spider") { dropItems(npc, listOf(526 to 1)) }
        } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }

        // Guards drop bones, coins, iron equipment
        listOf("npc.guard_397", "npc.guard_398", "npc.guard_399", "npc.guard_400").forEach { name ->
            try {
                onNpcDeath(name) {
                    val drops = mutableListOf(526 to 1, 995 to (15..60).random())
                    if (Math.random() < 0.08) drops.add(1139 to 1) // iron med helm
                    if (Math.random() < 0.05) drops.add(1153 to 1) // iron kite
                    dropItems(npc, drops)
                }
            } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }
        }

        // Dark wizards drop bones, runes, coins
        listOf("npc.dark_wizard", "npc.dark_wizard_512").forEach { name ->
            try {
                onNpcDeath(name) {
                    val drops = mutableListOf(526 to 1)
                    drops.add(995 to (10..40).random())
                    if (Math.random() < 0.3) drops.add(556 to (3..15).random()) // air runes
                    if (Math.random() < 0.2) drops.add(558 to (2..8).random()) // mind runes
                    if (Math.random() < 0.1) drops.add(554 to (2..7).random()) // fire runes
                    if (Math.random() < 0.05) drops.add(577 to 1) // wizard hat
                    dropItems(npc, drops)
                }
            } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }
        }

        // Al Kharid warriors drop bones, coins
        try {
            onNpcDeath("npc.al_kharid_warrior") {
                val drops = mutableListOf(526 to 1, 995 to (5..35).random())
                dropItems(npc, drops)
            }
        } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }

        // Scorpions drop nothing (no bones)

        // Skeletons drop bones, coins
        try {
            onNpcDeath("npc.skeleton") {
                val drops = mutableListOf(526 to 1)
                if (Math.random() < 0.5) drops.add(995 to (5..30).random())
                if (Math.random() < 0.1) drops.add(882 to (1..5).random()) // bronze arrows
                dropItems(npc, drops)
            }
        } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }

        // Mugger drops bones, coins
        try {
            onNpcDeath("npc.mugger") {
                dropItems(npc, listOf(526 to 1, 995 to (1..20).random()))
            }
        } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }

        // Highwayman drops bones, coins
        try {
            onNpcDeath("npc.highwayman") {
                dropItems(npc, listOf(526 to 1, 995 to (3..25).random()))
            }
        } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }

        // Hill giant drops big bones, coins, limpwurt root
        try {
            onNpcDeath("npc.hill_giant") {
                val drops = mutableListOf(532 to 1) // big bones
                drops.add(995 to (15..100).random())
                if (Math.random() < 0.15) drops.add(225 to 1) // limpwurt root
                if (Math.random() < 0.1) drops.add(1139 to 1) // iron med helm
                dropItems(npc, drops)
            }
        } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }

        // Moss giant drops big bones, coins, black sq shield
        listOf("npc.moss_giant", "npc.moss_giant_2091", "npc.moss_giant_2092").forEach { name ->
            try {
                onNpcDeath(name) {
                    val drops = mutableListOf(532 to 1) // big bones
                    drops.add(995 to (30..200).random())
                    if (Math.random() < 0.1) drops.add(1179 to 1) // black sq shield
                    if (Math.random() < 0.05) drops.add(1303 to 1) // iron 2h
                    dropItems(npc, drops)
                }
            } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }
        }

        // Lesser demon drops coins, rune med helm (rare)
        listOf("npc.lesser_demon", "npc.lesser_demon_2006", "npc.lesser_demon_2007").forEach { name ->
            try {
                onNpcDeath(name) {
                    val drops = mutableListOf<Pair<Int, Int>>()
                    drops.add(995 to (50..300).random())
                    if (Math.random() < 0.2) drops.add(554 to (5..25).random()) // fire runes
                    if (Math.random() < 0.1) drops.add(1149 to 1) // dragon med helm? no, mith med
                    if (Math.random() < 0.01) drops.add(1163 to 1) // rune med helm
                    dropItems(npc, drops)
                }
            } catch (e: Exception) { Server.logger.error { "Failed to bind NPC death: $e" } }
        }
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
