package org.alter.plugins.content.skills.crafting

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

data class CraftRecipe(val item1: String, val item2: String, val result: String, val level: Int, val xp: Double, val amount: Int = 1)

class CraftingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val leatherRecipes = listOf(
        // Needle + leather → leather items
        CraftRecipe("item.needle", "item.leather", "item.leather_gloves", 1, 13.8),
        CraftRecipe("item.needle", "item.leather", "item.leather_boots", 7, 16.3),
        CraftRecipe("item.needle", "item.leather", "item.leather_cowl", 9, 18.5),
        CraftRecipe("item.needle", "item.leather", "item.leather_vambraces", 11, 22.0),
        CraftRecipe("item.needle", "item.leather", "item.leather_body", 14, 25.0),
        CraftRecipe("item.needle", "item.leather", "item.leather_chaps", 18, 27.0),
    )

    private val gemRecipes = listOf(
        // Chisel + uncut gem → cut gem
        CraftRecipe("item.chisel", "item.uncut_opal", "item.opal", 1, 15.0),
        CraftRecipe("item.chisel", "item.uncut_jade", "item.jade", 13, 20.0),
        CraftRecipe("item.chisel", "item.uncut_red_topaz", "item.red_topaz", 16, 25.0),
        CraftRecipe("item.chisel", "item.uncut_sapphire", "item.sapphire", 20, 50.0),
        CraftRecipe("item.chisel", "item.uncut_emerald", "item.emerald", 27, 67.5),
        CraftRecipe("item.chisel", "item.uncut_ruby", "item.ruby", 63, 85.0),
        CraftRecipe("item.chisel", "item.uncut_diamond", "item.diamond", 43, 107.5),
        CraftRecipe("item.chisel", "item.uncut_dragonstone", "item.dragonstone", 55, 137.5),
        CraftRecipe("item.chisel", "item.uncut_onyx", "item.onyx", 67, 167.5),
    )

    init {
        onWorldInit {
            // Leather crafting
            leatherRecipes.forEach { recipe ->
                try {
                    onItemOnItem(item1 = recipe.item1, item2 = recipe.item2) {
                        player.queue { craft(player, recipe) }
                    }
                } catch (e: Exception) {
                    Server.logger.error { "Failed to register crafting: ${recipe.result}: ${e.message}" }
                }
            }

            // Gem cutting
            gemRecipes.forEach { recipe ->
                try {
                    onItemOnItem(item1 = recipe.item1, item2 = recipe.item2) {
                        player.queue { craft(player, recipe) }
                    }
                } catch (e: Exception) {
                    Server.logger.error { "Failed to register crafting: ${recipe.result}: ${e.message}" }
                }
            }
        }
    }

    private suspend fun QueueTask.craft(player: Player, recipe: CraftRecipe) {
        if (player.getSkills().getBaseLevel(Skills.CRAFTING) < recipe.level) {
            player.message("You need a Crafting level of ${recipe.level} to make this.")
            return
        }
        val item2Id = getRSCM(recipe.item2)
        val resultId = getRSCM(recipe.result)
        player.animate(885) // gem cutting / crafting animation
        wait(3)
        player.inventory.remove(item2Id, 1)
        player.inventory.add(resultId, recipe.amount)
        player.addXp(Skills.CRAFTING, recipe.xp)
        val name = recipe.result.removePrefix("item.").replace("_", " ")
        player.message("You make a $name.")
    }
}
