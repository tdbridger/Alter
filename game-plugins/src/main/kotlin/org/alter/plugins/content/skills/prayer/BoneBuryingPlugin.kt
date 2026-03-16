package org.alter.plugins.content.skills.prayer

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

enum class Bone(
    val itemName: String,
    val experience: Double
) {
    BONES("item.bones", 4.5),
    BIG_BONES("item.big_bones", 15.0),
    DRAGON_BONES("item.dragon_bones", 72.0),
    LAVA_DRAGON_BONES("item.lava_dragon_bones", 85.0),
    SUPERIOR_DRAGON_BONES("item.superior_dragon_bones", 150.0);

    @Transient var itemId: Int = -1
}

class BoneBuryingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onWorldInit {
            Bone.values().forEach { bone ->
                try {
                    bone.itemId = getRSCM(bone.itemName)
                    val def = getItem(bone.itemId)
                    val options = def.interfaceOptions.filterNotNull().filter { it.isNotBlank() }
                    Server.logger.info { "Bone ${bone.itemName} (${bone.itemId}) options: $options" }

                    // Find the "Bury" option
                    val buryIndex = def.interfaceOptions.indexOfFirst { it?.equals("Bury", ignoreCase = true) == true }
                    if (buryIndex != -1) {
                        onItemOption(bone.itemName, option = "bury") {
                            player.queue { bury(player, bone) }
                        }
                    } else {
                        Server.logger.error { "No 'Bury' option found for ${bone.itemName}, options: $options" }
                    }
                } catch (e: Exception) {
                    Server.logger.error { "Failed to register bone ${bone.itemName}: $e" }
                }
            }
        }
    }

    private suspend fun QueueTask.bury(player: Player, bone: Bone) {
        val boneName = bone.itemName.removePrefix("item.").replace("_", " ")
        player.lock()
        try {
            player.inventory.remove(bone.itemId, 1)
            player.animate(827)
            player.message("You bury the $boneName.")
            wait(3)
            player.addXp(Skills.PRAYER, bone.experience)
        } finally {
            player.unlock()
        }
    }
}
