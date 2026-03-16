package org.alter.plugins.content.skills.agility

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.ForcedMovement
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.move.moveTo
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Gnome Stronghold Agility Course (level 1+)
 *
 * Obstacles:
 * 1. Log balance (23145) - walk across log
 * 2. Obstacle net (23134) - climb net
 * 3. Tree branch (23559) - climb up
 * 4. Balancing rope (23557) - walk rope
 * 5. Tree branch (23560) - climb down
 * 6. Obstacle net (23135) - climb net
 * 7. Obstacle pipe (23138/23139) - squeeze through
 */
class GnomeCoursePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // 1. Log balance (23145) - 7.5 XP
        onObjOption("object.log_balance_23145", option = "Walk-across") {
            val obj = player.getInteractingGameObj()
            player.queue { logBalance(player) }
        }

        // 2. Obstacle net (23134) - 7.5 XP
        onObjOption("object.obstacle_net_23134", option = "Climb-over") {
            player.queue { climbNet1(player) }
        }

        // 3. Tree branch up (23559) - 5 XP
        onObjOption("object.tree_branch_23559", option = "Climb") {
            player.queue { treeBranchUp(player) }
        }

        // 4. Balancing rope (23557) - 7.5 XP
        onObjOption("object.balancing_rope_23557", option = "Walk-on") {
            player.queue { balancingRope(player) }
        }

        // 5. Tree branch down (23560) - 5 XP
        onObjOption("object.tree_branch_23560", option = "Climb-down") {
            player.queue { treeBranchDown(player) }
        }

        // 6. Obstacle net (23135) - 7.5 XP
        onObjOption("object.obstacle_net_23135", option = "Climb-over") {
            player.queue { climbNet2(player) }
        }

        // 7. Obstacle pipe (23138 and 23139) - 7.5 XP (+ 39 bonus for completing course)
        listOf("object.obstacle_pipe_23138", "object.obstacle_pipe_23139").forEach { pipe ->
            try {
                onObjOption(pipe, option = "Squeeze-through") {
                    player.queue { obstaclePipe(player) }
                }
            } catch (e: Exception) {}
        }
    }

    private suspend fun QueueTask.logBalance(player: Player) {
        player.lock()
        player.message("You walk carefully across the log...")
        player.animate(762) // balance walk animation
        wait(8)
        player.moveTo(2474, 3429, 0)
        player.addXp(Skills.AGILITY, 7.5)
        player.message("...and make it safely to the other side.")
        player.animate(-1)
        player.unlock()
    }

    private suspend fun QueueTask.climbNet1(player: Player) {
        player.lock()
        player.message("You climb the net.")
        player.animate(828) // climbing animation
        wait(2)
        player.moveTo(player.tile.x, 3424, 1) // go up a level
        player.addXp(Skills.AGILITY, 7.5)
        player.unlock()
    }

    private suspend fun QueueTask.treeBranchUp(player: Player) {
        player.lock()
        player.message("You climb the tree branch.")
        player.animate(828)
        wait(2)
        player.moveTo(2473, 3420, 2) // go up another level
        player.addXp(Skills.AGILITY, 5.0)
        player.unlock()
    }

    private suspend fun QueueTask.balancingRope(player: Player) {
        player.lock()
        player.message("You carefully cross the rope...")
        player.animate(762)
        wait(7)
        player.moveTo(2483, 3420, 2)
        player.addXp(Skills.AGILITY, 7.5)
        player.message("...and make it to the other side.")
        player.animate(-1)
        player.unlock()
    }

    private suspend fun QueueTask.treeBranchDown(player: Player) {
        player.lock()
        player.message("You climb down the tree branch.")
        player.animate(828)
        wait(2)
        player.moveTo(2487, 3420, 0) // back to ground
        player.addXp(Skills.AGILITY, 5.0)
        player.unlock()
    }

    private suspend fun QueueTask.climbNet2(player: Player) {
        player.lock()
        player.message("You climb the net.")
        player.animate(828)
        wait(2)
        player.moveTo(player.tile.x, 3427, 0)
        player.addXp(Skills.AGILITY, 7.5)
        player.unlock()
    }

    private suspend fun QueueTask.obstaclePipe(player: Player) {
        player.lock()
        player.message("You squeeze through the pipe...")
        player.animate(844) // pipe crawl animation
        wait(7)
        player.moveTo(2487, 3437, 0)
        player.addXp(Skills.AGILITY, 7.5)
        // Course completion bonus
        player.addXp(Skills.AGILITY, 39.0)
        player.message("You completed a lap of the course!")
        player.animate(-1)
        player.unlock()
    }
}
