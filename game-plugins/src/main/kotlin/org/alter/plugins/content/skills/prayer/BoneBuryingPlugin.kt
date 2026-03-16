package org.alter.plugins.content.skills.prayer

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
    BABY_DRAGON_BONES("item.baby_dragon_bone", 30.0),
    DRAGON_BONES("item.dragon_bones", 72.0),
    DAGANNOTH_BONES("item.dagannoth_bones", 125.0),
    WYVERN_BONES("item.wyvern_bones", 72.0),
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
            // Resolve bone IDs
            Bone.values().forEach { bone ->
                try {
                    bone.itemId = getRSCM(bone.itemName)
                } catch (e: Exception) {
                    // Some bones might not exist in this revision
                }
            }

            // Register "bury" option for each bone type
            Bone.values().forEach { bone ->
                try {
                    onItemOption(item = bone.itemName, option = "bury") {
                        player.queue { bury(player, bone) }
                    }
                } catch (e: Exception) {
                    // Some bones might not exist in this revision
                }
            }
        }
    }

    private suspend fun QueueTask.bury(player: Player, bone: Bone) {
        val boneName = bone.itemName.removePrefix("item.").replace("_", " ")
        player.lock()
        try {
            player.inventory.remove(bone.itemId, 1)
            player.animate(827) // bone burying animation
            player.message("You bury the $boneName.")
            wait(3)
            player.addXp(Skills.PRAYER, bone.experience)
        } finally {
            player.unlock()
        }
    }
}
