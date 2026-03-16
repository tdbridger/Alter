package org.alter.plugins.content.skills.fletching

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

/**
 * Fletching: knife on logs → unstrung bows, bow string on unstrung → strung bow
 */

data class FletchItem(val item1: String, val item2: String, val result: String, val level: Int, val xp: Double, val amount: Int = 1)

class FletchingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val recipes = listOf(
        // Knife + logs → arrow shafts
        FletchItem("item.knife", "item.logs", "item.arrow_shaft", 1, 5.0, 15),
        // Knife + logs → shortbow (u)
        FletchItem("item.knife", "item.logs", "item.shortbow_u", 5, 5.0),
        FletchItem("item.knife", "item.logs", "item.longbow_u", 10, 10.0),
        FletchItem("item.knife", "item.oak_logs", "item.oak_shortbow_u", 20, 16.5),
        FletchItem("item.knife", "item.oak_logs", "item.oak_longbow_u", 25, 25.0),
        FletchItem("item.knife", "item.willow_logs", "item.willow_shortbow_u", 35, 33.3),
        FletchItem("item.knife", "item.willow_logs", "item.willow_longbow_u", 40, 41.5),
        FletchItem("item.knife", "item.maple_logs", "item.maple_shortbow_u", 50, 50.0),
        FletchItem("item.knife", "item.maple_logs", "item.maple_longbow_u", 55, 58.3),
        FletchItem("item.knife", "item.yew_logs", "item.yew_shortbow_u", 65, 67.5),
        FletchItem("item.knife", "item.yew_logs", "item.yew_longbow_u", 70, 75.0),
        FletchItem("item.knife", "item.magic_logs", "item.magic_shortbow_u", 80, 83.3),
        FletchItem("item.knife", "item.magic_logs", "item.magic_longbow_u", 85, 91.5),
        // Bow string + unstrung → strung
        FletchItem("item.bow_string", "item.shortbow_u", "item.shortbow", 5, 5.0),
        FletchItem("item.bow_string", "item.longbow_u", "item.longbow", 10, 10.0),
        FletchItem("item.bow_string", "item.oak_shortbow_u", "item.oak_shortbow", 20, 16.5),
        FletchItem("item.bow_string", "item.oak_longbow_u", "item.oak_longbow", 25, 25.0),
        FletchItem("item.bow_string", "item.willow_shortbow_u", "item.willow_shortbow", 35, 33.3),
        FletchItem("item.bow_string", "item.willow_longbow_u", "item.willow_longbow", 40, 41.5),
        FletchItem("item.bow_string", "item.maple_shortbow_u", "item.maple_shortbow", 50, 50.0),
        FletchItem("item.bow_string", "item.maple_longbow_u", "item.maple_longbow", 55, 58.3),
        FletchItem("item.bow_string", "item.yew_shortbow_u", "item.yew_shortbow", 65, 67.5),
        FletchItem("item.bow_string", "item.yew_longbow_u", "item.yew_longbow", 70, 75.0),
        FletchItem("item.bow_string", "item.magic_shortbow_u", "item.magic_shortbow", 80, 83.3),
        FletchItem("item.bow_string", "item.magic_longbow_u", "item.magic_longbow", 85, 91.5),
    )

    init {
        onWorldInit {
            recipes.forEach { recipe ->
                try {
                    val resultId = getRSCM(recipe.result)
                    val item2Id = getRSCM(recipe.item2)
                    onItemOnItem(item1 = recipe.item1, item2 = recipe.item2) {
                        player.queue { fletch(player, recipe, item2Id, resultId) }
                    }
                } catch (e: Exception) {
                    Server.logger.error { "Failed to register fletching: ${recipe.result}: ${e.message}" }
                }
            }
        }
    }

    private suspend fun QueueTask.fletch(player: Player, recipe: FletchItem, item2Id: Int, resultId: Int) {
        if (player.getSkills().getBaseLevel(Skills.FLETCHING) < recipe.level) {
            player.message("You need a Fletching level of ${recipe.level} to make this.")
            return
        }

        val resultName = recipe.result.removePrefix("item.").replace("_", " ")
        player.animate(1248) // fletching animation
        wait(3)

        player.inventory.remove(item2Id, 1)
        player.inventory.add(resultId, recipe.amount)
        player.addXp(Skills.FLETCHING, recipe.xp)
        player.message("You make a $resultName.")
    }
}
