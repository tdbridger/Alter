package org.alter.plugins.content.skills.herblore

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

data class HerbClean(val grimy: String, val clean: String, val level: Int, val xp: Double)
data class PotionMix(val unf: String, val secondary: String, val result: String, val level: Int, val xp: Double)
data class UnfPotion(val herb: String, val result: String, val level: Int)

class HerblorePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val herbCleans = listOf(
        HerbClean("item.grimy_guam_leaf", "item.guam_leaf", 3, 2.5),
        HerbClean("item.grimy_marrentill", "item.marrentill", 5, 3.8),
        HerbClean("item.grimy_tarromin", "item.tarromin", 11, 5.0),
        HerbClean("item.grimy_harralander", "item.harralander", 20, 6.3),
        HerbClean("item.grimy_ranarr_weed", "item.ranarr_weed", 25, 7.5),
        HerbClean("item.grimy_irit_leaf", "item.irit_leaf", 40, 8.8),
        HerbClean("item.grimy_avantoe", "item.avantoe", 48, 10.0),
        HerbClean("item.grimy_kwuarm", "item.kwuarm", 54, 11.3),
        HerbClean("item.grimy_cadantine", "item.cadantine", 65, 12.5),
        HerbClean("item.grimy_dwarf_weed", "item.dwarf_weed", 70, 13.8),
        HerbClean("item.grimy_torstol", "item.torstol", 75, 15.0),
    )

    private val unfPotions = listOf(
        UnfPotion("item.guam_leaf", "item.guam_potion_unf", 3),
        UnfPotion("item.marrentill", "item.marrentill_potion_unf", 5),
        UnfPotion("item.tarromin", "item.tarromin_potion_unf", 11),
        UnfPotion("item.harralander", "item.harralander_potion_unf", 20),
        UnfPotion("item.ranarr_weed", "item.ranarr_potion_unf", 25),
        UnfPotion("item.irit_leaf", "item.irit_potion_unf", 40),
        UnfPotion("item.avantoe", "item.avantoe_potion_unf", 48),
        UnfPotion("item.kwuarm", "item.kwuarm_potion_unf", 54),
        UnfPotion("item.cadantine", "item.cadantine_potion_unf", 65),
        UnfPotion("item.dwarf_weed", "item.dwarf_weed_potion_unf", 70),
        UnfPotion("item.torstol", "item.torstol_potion_unf", 75),
    )

    private val potionMixes = listOf(
        PotionMix("item.guam_potion_unf", "item.eye_of_newt", "item.attack_potion3", 3, 25.0),
        PotionMix("item.marrentill_potion_unf", "item.unicorn_horn_dust", "item.antipoison3", 5, 37.5),
        PotionMix("item.tarromin_potion_unf", "item.limpwurt_root", "item.strength_potion3", 12, 50.0),
        PotionMix("item.harralander_potion_unf", "item.red_spiders_eggs", "item.restore_potion3", 22, 62.5),
        PotionMix("item.ranarr_potion_unf", "item.snape_grass", "item.prayer_potion3", 38, 87.5),
        PotionMix("item.irit_potion_unf", "item.white_berries", "item.super_defence3", 66, 150.0),
        PotionMix("item.kwuarm_potion_unf", "item.limpwurt_root", "item.super_strength3", 55, 125.0),
    )

    init {
        onWorldInit {
            // Herb cleaning - click grimy herb
            herbCleans.forEach { herb ->
                try {
                    onItemOption(herb.grimy, option = "clean") {
                        player.queue { cleanHerb(player, herb) }
                    }
                } catch (e: Exception) {
                    Server.logger.error { "Failed to register herb clean: ${herb.grimy}: ${e.message}" }
                }
            }

            // Unfinished potions - herb on vial of water
            unfPotions.forEach { unf ->
                try {
                    onItemOnItem(item1 = unf.herb, item2 = "item.vial_of_water") {
                        player.queue { makeUnf(player, unf) }
                    }
                } catch (e: Exception) {
                    Server.logger.error { "Failed to register unf potion: ${unf.herb}: ${e.message}" }
                }
            }

            // Finished potions - secondary on unfinished
            potionMixes.forEach { mix ->
                try {
                    onItemOnItem(item1 = mix.secondary, item2 = mix.unf) {
                        player.queue { mixPotion(player, mix) }
                    }
                } catch (e: Exception) {
                    Server.logger.error { "Failed to register potion mix: ${mix.result}: ${e.message}" }
                }
            }
        }
    }

    private suspend fun QueueTask.cleanHerb(player: Player, herb: HerbClean) {
        if (player.getSkills().getBaseLevel(Skills.HERBLORE) < herb.level) {
            player.message("You need a Herblore level of ${herb.level} to clean this herb.")
            return
        }
        val grimyId = getRSCM(herb.grimy)
        val cleanId = getRSCM(herb.clean)
        player.inventory.remove(grimyId, 1)
        player.inventory.add(cleanId, 1)
        player.addXp(Skills.HERBLORE, herb.xp)
        val name = herb.clean.removePrefix("item.").replace("_", " ")
        player.message("You clean the $name.")
    }

    private suspend fun QueueTask.makeUnf(player: Player, unf: UnfPotion) {
        if (player.getSkills().getBaseLevel(Skills.HERBLORE) < unf.level) {
            player.message("You need a Herblore level of ${unf.level} to make this.")
            return
        }
        val herbId = getRSCM(unf.herb)
        val vialId = getRSCM("item.vial_of_water")
        val unfId = getRSCM(unf.result)
        player.inventory.remove(herbId, 1)
        player.inventory.remove(vialId, 1)
        player.inventory.add(unfId, 1)
        val name = unf.result.removePrefix("item.").replace("_", " ")
        player.message("You mix the herb into the vial.")
    }

    private suspend fun QueueTask.mixPotion(player: Player, mix: PotionMix) {
        if (player.getSkills().getBaseLevel(Skills.HERBLORE) < mix.level) {
            player.message("You need a Herblore level of ${mix.level} to mix this potion.")
            return
        }
        val unfId = getRSCM(mix.unf)
        val secId = getRSCM(mix.secondary)
        val resultId = getRSCM(mix.result)
        player.animate(363) // potion mixing animation
        wait(2)
        player.inventory.remove(unfId, 1)
        player.inventory.remove(secId, 1)
        player.inventory.add(resultId, 1)
        player.addXp(Skills.HERBLORE, mix.xp)
        val name = mix.result.removePrefix("item.").replace("_", " ")
        player.message("You mix the $name.")
    }
}
