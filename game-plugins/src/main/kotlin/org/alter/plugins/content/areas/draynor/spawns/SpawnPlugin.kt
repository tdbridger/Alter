package org.alter.plugins.content.areas.draynor.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        // Draynor Village men/women
        spawnNpc(npc = "npc.man_3106", x = 3104, z = 3259, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3108", x = 3096, z = 3255, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.woman_3111", x = 3109, z = 3253, walkRadius = 8, direction = Direction.NORTH)

        // Draynor bank
        spawnNpc(npc = "npc.banker_766", x = 3092, z = 3245, walkRadius = 0, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker_766", x = 3093, z = 3245, walkRadius = 0, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker_766", x = 3094, z = 3245, walkRadius = 0, direction = Direction.SOUTH)

        // Wizards at Wizards' Tower area
        spawnNpc(npc = "npc.dark_wizard", x = 3115, z = 3233, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_wizard", x = 3111, z = 3228, walkRadius = 3, direction = Direction.EAST)

        // Highwayman between Draynor and Falador
        spawnNpc(npc = "npc.highwayman", x = 3029, z = 3291, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.highwayman", x = 3035, z = 3288, walkRadius = 8, direction = Direction.EAST)

        // Mugger near Draynor
        spawnNpc(npc = "npc.mugger", x = 3086, z = 3244, walkRadius = 5, direction = Direction.SOUTH)

        // Fishing spots at Draynor (net/bait)
        spawnNpc(npc = "npc.fishing_spot_1530", x = 3085, z = 3231, walkRadius = 0, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.fishing_spot_1530", x = 3085, z = 3227, walkRadius = 0, direction = Direction.SOUTH)
    }
}
