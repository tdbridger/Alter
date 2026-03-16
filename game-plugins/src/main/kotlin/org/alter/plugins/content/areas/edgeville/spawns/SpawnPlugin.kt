package org.alter.plugins.content.areas.edgeville.spawns

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
        // Edgeville men
        spawnNpc(npc = "npc.man_3106", x = 3091, z = 3508, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3108", x = 3098, z = 3510, walkRadius = 8, direction = Direction.EAST)
        spawnNpc(npc = "npc.woman_3111", x = 3088, z = 3502, walkRadius = 8, direction = Direction.NORTH)

        // Edgeville bankers
        spawnNpc(npc = "npc.banker_766", x = 3098, z = 3491, walkRadius = 0, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker_766", x = 3097, z = 3491, walkRadius = 0, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banker_766", x = 3096, z = 3491, walkRadius = 0, direction = Direction.SOUTH)

        // Guards near wilderness ditch
        spawnNpc(npc = "npc.guard_398", x = 3087, z = 3516, walkRadius = 3, direction = Direction.NORTH)
        spawnNpc(npc = "npc.guard_398", x = 3094, z = 3518, walkRadius = 3, direction = Direction.NORTH)

        // Skeletons at Edgeville dungeon entrance area
        spawnNpc(npc = "npc.skeleton", x = 3094, z = 3524, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.skeleton", x = 3098, z = 3526, walkRadius = 5, direction = Direction.WEST)
    }
}
