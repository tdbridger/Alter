package org.alter.plugins.content.mechanics.corrupted

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Player
import org.alter.game.model.priv.Privilege
import org.alter.game.model.timer.TimerKey
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Corrupted Gielinor — Phase A: Corruption Timer Core
 *
 * +1 corruption every 100 ticks (60 seconds).
 * Chat messages at thresholds. ::corruption and ::setcorruption commands.
 */

val CORRUPTION_ATTR = AttributeKey<Int>(persistenceKey = "cg_corruption")
val CORRUPTION_TIMER = TimerKey(persistenceKey = "cg_corruption_timer")
val ARCHETYPE_MULTIPLIER_ATTR = AttributeKey<Double>(persistenceKey = "cg_archetype_multiplier")
val ARCHETYPE_NAME_ATTR = AttributeKey<String>(persistenceKey = "cg_archetype_name")
val ARCHETYPE_CHOSEN_ATTR = AttributeKey<Boolean>(persistenceKey = "cg_archetype_chosen")

class CorruptionPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Set XP rate on login, only start corruption if archetype chosen
        onLogin {
            // Phase B: 15x XP rate
            player.xpRate = 15.0

            // Only start corruption timer if archetype has been chosen
            val archetypeChosen = player.attr[ARCHETYPE_CHOSEN_ATTR] ?: false
            if (archetypeChosen && !player.timers.has(CORRUPTION_TIMER)) {
                player.timers[CORRUPTION_TIMER] = 100
            }
        }

        // Handle corruption tick
        onTimer(CORRUPTION_TIMER) {
            val player = pawn as? Player ?: return@onTimer

            val oldCorruption = player.attr[CORRUPTION_ATTR] ?: 0
            val newCorruption = oldCorruption + 1
            val oldTier = getTier(oldCorruption)
            val newTier = getTier(newCorruption)

            player.attr[CORRUPTION_ATTR] = newCorruption

            // Threshold messages
            when (newCorruption) {
                50 -> player.message("<col=8B0000>You feel a faint unease...</col>")
                90 -> player.message("<col=8B0000>The air grows heavy. Something is changing.</col>")
            }

            // Tier transition messages
            if (newTier > oldTier) {
                when (newTier) {
                    1 -> {
                        player.message("<col=FF0000>=== CORRUPTION TIER 1: UNEASE ===</col>")
                        player.message("<col=8B0000>Hostile creatures stir in the towns. Food is less effective.</col>")
                    }
                    2 -> {
                        player.message("<col=FF0000>=== CORRUPTION TIER 2: DREAD ===</col>")
                        player.message("<col=8B0000>Resources grow scarce. Shops raise prices.</col>")
                    }
                    3 -> {
                        player.message("<col=FF0000>=== CORRUPTION TIER 3: TERROR ===</col>")
                        player.message("<col=8B0000>Monsters grow stronger. Boss-tier threats wander the world.</col>")
                    }
                    4 -> {
                        player.message("<col=FF0000>=== CORRUPTION TIER 4: COLLAPSE ===</col>")
                        player.message("<col=8B0000>The World Boss has spawned. The end approaches.</col>")
                        WorldBossPlugin.spawnBoss(player)
                    }
                    5 -> {
                        player.message("<col=FF0000>=== CORRUPTION TIER 5: ANNIHILATION ===</col>")
                        player.message("<col=8B0000>The World Boss hunts you. No safe zones remain. Gielinor is dying.</col>")
                    }
                }
            }

            // Periodic reminders every 100 corruption
            if (newCorruption > 100 && newCorruption % 100 == 0 && newTier == oldTier) {
                player.message("<col=8B0000>Corruption: $newCorruption (${getTierName(newTier)})</col>")
            }

            // Apply corruption tier effects (Phase F)
            CorruptionEffects.applyTickEffects(player, player.world, newCorruption)

            // Reschedule timer
            player.timers[CORRUPTION_TIMER] = 100
        }

        // ::corruption command — available to all players
        onCommand("corruption", description = "Show corruption level") {
            val corruption = player.attr[CORRUPTION_ATTR] ?: 0
            val tier = getTier(corruption)
            val tierName = getTierName(tier)
            val nextThreshold = when (tier) {
                0 -> 100; 1 -> 200; 2 -> 300; 3 -> 500; 4 -> 750; 5 -> 1000; else -> 0
            }
            player.message("=== Corruption: $corruption / 1000 ===")
            player.message("Tier: $tier ($tierName)")
            if (tier < 5) {
                player.message("Next tier at: $nextThreshold (${nextThreshold - corruption} away)")
            } else {
                player.message("Maximum corruption tier reached.")
            }
        }

        // ::setcorruption command — available to all during development
        onCommand("setcorruption", description = "Set corruption level") {
            val args = player.getCommandArgs()
            val value = args[0].toIntOrNull()
            if (value == null || value < 0) {
                player.message("Usage: ::setcorruption <value>")
                return@onCommand
            }
            player.attr[CORRUPTION_ATTR] = value
            val tier = getTier(value)
            player.message("Corruption set to $value (Tier $tier: ${getTierName(tier)})")
        }

        // ::killme command — for testing death/scoring
        onCommand("killme", description = "Kill yourself (testing)") {
            player.getSkills().setCurrentLevel(Skills.HITPOINTS, 0)
            player.message("You have been killed by your own command.")
        }
    }

    companion object {
        fun getCorruption(player: Player): Int = player.attr[CORRUPTION_ATTR] ?: 0

        fun getTier(corruption: Int): Int = when {
            corruption < 100 -> 0
            corruption < 200 -> 1
            corruption < 300 -> 2
            corruption < 500 -> 3
            corruption < 750 -> 4
            else -> 5
        }

        fun getTierName(tier: Int): String = when (tier) {
            0 -> "Peace"; 1 -> "Unease"; 2 -> "Dread"
            3 -> "Terror"; 4 -> "Collapse"; 5 -> "Annihilation"
            else -> "Unknown"
        }
    }
}
