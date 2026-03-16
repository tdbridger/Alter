package org.alter.plugins.content.mechanics.corrupted

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import com.google.gson.JsonParser
import java.io.File

/**
 * Corrupted Gielinor Phase K: Meta-Progression
 *
 * Legacy points = final score / 100, persist across all runs in meta JSON.
 * ::legacy command to view points and perk shop.
 */

data class LegacyPerk(val name: String, val cost: Int, val description: String)

class LegacyPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val legacyPerks = listOf(
        LegacyPerk("Inherited Knowledge", 500, "Start with level 15 in a chosen skill"),
        LegacyPerk("Established Trade", 300, "One shop has 25% lower prices"),
        LegacyPerk("Ancestral Strength I", 200, "+1 to all combat stats at start"),
        LegacyPerk("Ancestral Strength II", 400, "+2 to all combat stats at start"),
        LegacyPerk("Ancestral Strength III", 600, "+3 to all combat stats at start"),
        LegacyPerk("Cartographer's Notes", 400, "Start with 50 law + 100 air runes"),
        LegacyPerk("Corruption Resistance I", 600, "Corruption rate reduced 10%"),
        LegacyPerk("Corruption Resistance II", 1200, "Corruption rate reduced 20%"),
        LegacyPerk("Night Vision I", 350, "Night combat penalty halved"),
        LegacyPerk("Scavenger I", 250, "+10% drop rates"),
        LegacyPerk("Last Stand", 1000, "Survive one killing blow per run at 1 HP"),
        LegacyPerk("Survivor Affinity", 500, "Survivors trade better, hostile later"),
        LegacyPerk("Reroll", 750, "Generate 3 new archetypes if you don't like the first set"),
    )

    init {
        onCommand("legacy", description = "View legacy points and perk shop") {
            val meta = loadMeta()
            val stats = meta.getAsJsonObject("lifetime_stats")
            val points = stats?.get("legacy_points")?.asInt ?: 0

            player.message("")
            player.message("<col=FFD700>=== Legacy Points: $points ===</col>")
            player.message("")
            player.message("<col=FFD700>Available Perks:</col>")
            legacyPerks.forEach { perk ->
                val affordable = if (points >= perk.cost) "<col=00FF00>" else "<col=FF0000>"
                player.message("$affordable${perk.name}</col> (${perk.cost} pts) — ${perk.description}")
            }
            player.message("")
            player.message("Legacy perks are purchased between runs. (Coming soon)")
        }

        onCommand("runs", description = "Show lifetime stats") {
            val meta = loadMeta()
            val stats = meta.getAsJsonObject("lifetime_stats")
            if (stats == null) {
                player.message("No stats yet.")
                return@onCommand
            }
            player.message("=== Lifetime Stats ===")
            player.message("Total Runs: ${stats.get("total_runs")?.asInt ?: 0}")
            player.message("Best Score: ${stats.get("best_score")?.asInt ?: 0}")
            player.message("Highest Total Level: ${stats.get("highest_total_level")?.asInt ?: 0}")
            player.message("Highest Corruption: ${stats.get("highest_corruption")?.asInt ?: 0}")
            player.message("Legacy Points: ${stats.get("legacy_points")?.asInt ?: 0}")
        }
    }

    companion object {
        private const val META_PATH = "data/saves/corrupted_gielinor_meta.json"

        fun loadMeta(): com.google.gson.JsonObject {
            val file = File(META_PATH)
            if (file.exists()) {
                return JsonParser.parseString(file.readText()).asJsonObject
            }
            val meta = com.google.gson.JsonObject()
            meta.add("runs", com.google.gson.JsonArray())
            val stats = com.google.gson.JsonObject()
            stats.addProperty("total_runs", 0)
            stats.addProperty("best_score", 0)
            stats.addProperty("highest_total_level", 0)
            stats.addProperty("highest_corruption", 0)
            stats.addProperty("legacy_points", 0)
            meta.add("lifetime_stats", stats)
            return meta
        }
    }
}
