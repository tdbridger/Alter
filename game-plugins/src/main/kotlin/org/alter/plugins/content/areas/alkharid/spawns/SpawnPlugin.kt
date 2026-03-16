package org.alter.plugins.content.areas.alkharid.spawns

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
        // Al Kharid warriors
        spawnNpc(npc = "npc.al_kharid_warrior", x = 3293, z = 3174, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.al_kharid_warrior", x = 3289, z = 3170, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.al_kharid_warrior", x = 3298, z = 3177, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.al_kharid_warrior", x = 3285, z = 3173, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.al_kharid_warrior", x = 3296, z = 3169, walkRadius = 5, direction = Direction.SOUTH)

        // Al Kharid guards at gate
        spawnNpc(npc = "npc.guard_398", x = 3268, z = 3226, walkRadius = 2, direction = Direction.WEST)
        spawnNpc(npc = "npc.guard_398", x = 3268, z = 3228, walkRadius = 2, direction = Direction.WEST)

        // Al Kharid bankers
        spawnNpc(npc = "npc.banker_766", x = 3269, z = 3167, walkRadius = 0, direction = Direction.NORTH)
        spawnNpc(npc = "npc.banker_766", x = 3270, z = 3167, walkRadius = 0, direction = Direction.NORTH)
        spawnNpc(npc = "npc.banker_766", x = 3271, z = 3167, walkRadius = 0, direction = Direction.NORTH)

        // Scorpions in mine area
        spawnNpc(npc = "npc.scorpion", x = 3297, z = 3305, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.scorpion", x = 3300, z = 3299, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.scorpion", x = 3293, z = 3297, walkRadius = 5, direction = Direction.NORTH)

        // Men in Al Kharid
        spawnNpc(npc = "npc.man_3106", x = 3277, z = 3186, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3108", x = 3281, z = 3192, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.woman_3111", x = 3285, z = 3183, walkRadius = 8, direction = Direction.NORTH)
    }
}
