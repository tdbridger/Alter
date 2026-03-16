package org.alter.plugins.content.mechanics.corrupted

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Player
import org.alter.game.model.timer.TimerKey
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Corrupted Gielinor Phase G: Day/Night Cycle
 *
 * 1200 ticks day (12 min), 800 ticks night (8 min) = 20 min total cycle.
 * Night: NPC combat +20%, aggro radius doubled.
 */

val DAY_NIGHT_TIMER = TimerKey(persistenceKey = "cg_daynight_timer")
val DAY_NIGHT_TICK_ATTR = AttributeKey<Int>(persistenceKey = "cg_daynight_tick")
val IS_NIGHT_ATTR = AttributeKey<Boolean>(persistenceKey = "cg_is_night")

class DayNightPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        const val DAY_TICKS = 1200   // 12 minutes
        const val NIGHT_TICKS = 800  // 8 minutes
        const val CYCLE_TICKS = DAY_TICKS + NIGHT_TICKS
        const val TICK_INTERVAL = 10 // check every 10 ticks (6 seconds)

        fun isNight(player: Player): Boolean = player.attr[IS_NIGHT_ATTR] ?: false

        fun getNightCombatMultiplier(player: Player): Double {
            return if (isNight(player)) 1.2 else 1.0
        }
    }

    init {
        // Start day/night timer on login if archetype chosen
        onLogin {
            val chosen = player.attr[ARCHETYPE_CHOSEN_ATTR] ?: false
            if (chosen && !player.timers.has(DAY_NIGHT_TIMER)) {
                player.timers[DAY_NIGHT_TIMER] = TICK_INTERVAL
                if (player.attr[DAY_NIGHT_TICK_ATTR] == null) {
                    player.attr[DAY_NIGHT_TICK_ATTR] = 0
                }
            }
        }

        onTimer(DAY_NIGHT_TIMER) {
            val player = pawn as? Player ?: return@onTimer

            val currentTick = (player.attr[DAY_NIGHT_TICK_ATTR] ?: 0) + TICK_INTERVAL
            val cyclePosition = currentTick % CYCLE_TICKS
            val wasNight = player.attr[IS_NIGHT_ATTR] ?: false
            val isNowNight = cyclePosition >= DAY_TICKS

            player.attr[DAY_NIGHT_TICK_ATTR] = currentTick

            // Transition messages
            if (!wasNight && isNowNight) {
                player.attr[IS_NIGHT_ATTR] = true
                player.message("<col=4444AA>Darkness falls across the land...</col>")
            } else if (wasNight && !isNowNight) {
                player.attr[IS_NIGHT_ATTR] = false
                player.message("<col=FFDD44>Dawn breaks across Gielinor.</col>")
            }

            // Warning before transitions
            val ticksUntilNight = if (cyclePosition < DAY_TICKS) DAY_TICKS - cyclePosition else -1
            val ticksUntilDay = if (cyclePosition >= DAY_TICKS) CYCLE_TICKS - cyclePosition else -1
            if (ticksUntilNight in 1..100 && ticksUntilNight > 90) {
                player.message("<col=8888CC>The sun begins to set...</col>")
            }
            if (ticksUntilDay in 1..100 && ticksUntilDay > 90) {
                player.message("<col=DDCC44>The first light of dawn approaches.</col>")
            }

            player.timers[DAY_NIGHT_TIMER] = TICK_INTERVAL
        }

        // ::night command
        onCommand("night", description = "Show time until next day/night transition") {
            val tick = player.attr[DAY_NIGHT_TICK_ATTR] ?: 0
            val cyclePosition = tick % CYCLE_TICKS
            val isNight = cyclePosition >= DAY_TICKS

            if (isNight) {
                val remaining = CYCLE_TICKS - cyclePosition
                val minutes = (remaining * 600) / 60000
                player.message("It is currently <col=4444AA>night</col>. Dawn in ~${minutes} minutes.")
            } else {
                val remaining = DAY_TICKS - cyclePosition
                val minutes = (remaining * 600) / 60000
                player.message("It is currently <col=FFDD44>day</col>. Nightfall in ~${minutes} minutes.")
            }
        }
    }
}
