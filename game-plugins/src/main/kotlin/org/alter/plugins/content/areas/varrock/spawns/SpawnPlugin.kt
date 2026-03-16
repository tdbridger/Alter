package org.alter.plugins.content.areas.varrock.spawns

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
        // Varrock Guards
        spawnNpc(npc = "npc.guard_398", x = 3211, z = 3462, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_398", x = 3218, z = 3459, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.guard_398", x = 3202, z = 3456, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.guard_398", x = 3207, z = 3477, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_398", x = 3229, z = 3467, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.guard_398", x = 3245, z = 3471, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.guard_398", x = 3224, z = 3401, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_398", x = 3262, z = 3410, walkRadius = 5, direction = Direction.NORTH)

        // Varrock men/women
        spawnNpc(npc = "npc.man_3106", x = 3216, z = 3430, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3106", x = 3227, z = 3435, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.man_3108", x = 3237, z = 3444, walkRadius = 10, direction = Direction.WEST)
        spawnNpc(npc = "npc.woman_3111", x = 3221, z = 3444, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.woman_3111", x = 3240, z = 3430, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3106", x = 3254, z = 3427, walkRadius = 10, direction = Direction.EAST)

        // Dark wizards south of Varrock
        spawnNpc(npc = "npc.dark_wizard", x = 3229, z = 3369, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_wizard", x = 3226, z = 3366, walkRadius = 4, direction = Direction.EAST)
        spawnNpc(npc = "npc.dark_wizard", x = 3222, z = 3370, walkRadius = 4, direction = Direction.NORTH)
        spawnNpc(npc = "npc.dark_wizard", x = 3232, z = 3366, walkRadius = 4, direction = Direction.WEST)
        spawnNpc(npc = "npc.dark_wizard_512", x = 3225, z = 3363, walkRadius = 4, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.dark_wizard_512", x = 3230, z = 3372, walkRadius = 4, direction = Direction.EAST)

        // Mugger near Varrock
        spawnNpc(npc = "npc.mugger", x = 3249, z = 3390, walkRadius = 5, direction = Direction.SOUTH)

        // Varrock bankers
        spawnNpc(npc = "npc.banker_766", x = 3253, z = 3420, walkRadius = 0, direction = Direction.WEST)
        spawnNpc(npc = "npc.banker_766", x = 3253, z = 3421, walkRadius = 0, direction = Direction.WEST)
        spawnNpc(npc = "npc.banker_766", x = 3253, z = 3422, walkRadius = 0, direction = Direction.WEST)

        // Varrock west bank
        spawnNpc(npc = "npc.banker_766", x = 3185, z = 3436, walkRadius = 0, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker_766", x = 3186, z = 3436, walkRadius = 0, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker_766", x = 3187, z = 3436, walkRadius = 0, direction = Direction.SOUTH)

        // Rats in Varrock sewers area
        spawnNpc(npc = "npc.rat_2854", x = 3237, z = 3458, walkRadius = 5, direction = Direction.NORTH)
        spawnNpc(npc = "npc.rat_2854", x = 3230, z = 3456, walkRadius = 5, direction = Direction.EAST)
    }
}
