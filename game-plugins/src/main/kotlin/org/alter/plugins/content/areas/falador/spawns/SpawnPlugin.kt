package org.alter.plugins.content.areas.falador.spawns

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
        // Falador guards
        spawnNpc(npc = "npc.guard_398", x = 2963, z = 3382, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_398", x = 2969, z = 3392, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.guard_398", x = 2956, z = 3389, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.guard_398", x = 2946, z = 3381, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.guard_398", x = 2970, z = 3376, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_398", x = 2939, z = 3375, walkRadius = 5, direction = Direction.EAST)

        // Falador men/women
        spawnNpc(npc = "npc.man_3106", x = 2960, z = 3388, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3108", x = 2948, z = 3383, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.woman_3111", x = 2965, z = 3374, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.man_3106", x = 2955, z = 3396, walkRadius = 10, direction = Direction.WEST)

        // Falador east bank
        spawnNpc(npc = "npc.banker_766", x = 3013, z = 3355, walkRadius = 0, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker_766", x = 3014, z = 3355, walkRadius = 0, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker_766", x = 3015, z = 3355, walkRadius = 0, direction = Direction.SOUTH)

        // Falador west bank
        spawnNpc(npc = "npc.banker_766", x = 2946, z = 3368, walkRadius = 0, direction = Direction.NORTH)
        spawnNpc(npc = "npc.banker_766", x = 2947, z = 3368, walkRadius = 0, direction = Direction.NORTH)
        spawnNpc(npc = "npc.banker_766", x = 2948, z = 3368, walkRadius = 0, direction = Direction.NORTH)

        // Dwarven mine entrance area - dwarves/scorpions
        spawnNpc(npc = "npc.scorpion", x = 3020, z = 3338, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.scorpion", x = 3017, z = 3341, walkRadius = 5, direction = Direction.EAST)

        // White knights (outside castle)
        spawnNpc(npc = "npc.guard_398", x = 2970, z = 3343, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.guard_398", x = 2965, z = 3340, walkRadius = 5, direction = Direction.EAST)
    }
}
