package org.alter.plugins.content.mechanics.corrupted

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.io.File
import java.time.Instant

/**
 * Corrupted Gielinor — Phase C: Permadeath & Scoring
 */
class DeathScoringPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onPlayerDeath {
            val corruption = player.attr[CORRUPTION_ATTR] ?: 0
            val totalLevel = player.getSkills().calculateTotalLevel
            val wealth = calculateWealth(player)

            val adjustedLevel = Math.max(0, totalLevel - 33)
            val adjustedCorruption = Math.max(0, corruption - 100)

            val baseScore = adjustedLevel * 10
            val survivalScore = adjustedCorruption * 50
            val wealthScore = (wealth / 100).toInt()

            val archetypeMultiplier = player.attr[ARCHETYPE_MULTIPLIER_ATTR] ?: 1.0
            val rawScore = baseScore + survivalScore + wealthScore
            val totalScore = (rawScore * archetypeMultiplier).toInt()

            // Display score summary
            player.message("")
            player.message("<col=FF0000>=== YOU HAVE DIED ===</col>")
            player.message("<col=8B0000>The corruption claims another soul.</col>")
            player.message("")
            player.message("  Total Level: $totalLevel (${baseScore} pts)")
            player.message("  Corruption Survived: $corruption (${survivalScore} pts)")
            player.message("  Wealth: $wealth gp (${wealthScore} pts)")
            player.message("")
            player.message("Final Score: $rawScore x ${archetypeMultiplier}x = <col=FFD700>$totalScore</col>")
            player.message("")
            player.message("<col=8B0000>Your run has ended. Create a new account to try again.</col>")

            // Save run record
            saveRunRecord(player, totalScore, corruption, totalLevel, wealth)
        }

        // ::score command
        onCommand("score", description = "Show current score estimate") {
            val corruption = player.attr[CORRUPTION_ATTR] ?: 0
            val totalLevel = player.getSkills().calculateTotalLevel
            val wealth = calculateWealth(player)

            val adjustedLevel = Math.max(0, totalLevel - 33)
            val adjustedCorruption = Math.max(0, corruption - 100)

            val baseScore = adjustedLevel * 10
            val survivalScore = adjustedCorruption * 50
            val wealthScore = (wealth / 100).toInt()

            val archetypeMultiplier = player.attr[ARCHETYPE_MULTIPLIER_ATTR] ?: 1.0
            val rawScore = baseScore + survivalScore + wealthScore
            val totalScore = (rawScore * archetypeMultiplier).toInt()

            player.message("=== Current Score Estimate: $totalScore ===")
            player.message("  Total Level: $totalLevel (${baseScore} pts)")
            player.message("  Corruption: $corruption (${survivalScore} pts)")
            player.message("  Wealth: $wealth gp (${wealthScore} pts)")
            if (archetypeMultiplier != 1.0) {
                player.message("  Archetype multiplier: ${archetypeMultiplier}x")
            }
        }

        // ::history command
        onCommand("history", description = "View past run records") {
            val meta = loadMeta()
            val runs = meta.getAsJsonArray("runs")
            if (runs == null || runs.size() == 0) {
                player.message("No runs recorded yet.")
                return@onCommand
            }
            player.message("=== Run History (last 5) ===")
            val start = Math.max(0, runs.size() - 5)
            for (i in start until runs.size()) {
                val r = runs[i].asJsonObject
                player.message("#${r.get("run_number").asInt}: ${r.get("account").asString} - Score: ${r.get("score").asInt} | Corruption: ${r.get("corruption").asInt}")
            }
        }

        // ::runs command
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
        }
    }

    companion object {
        private const val META_PATH = "data/saves/corrupted_gielinor_meta.json"
        private val gson = GsonBuilder().setPrettyPrinting().create()

        fun calculateWealth(player: Player): Long {
            var wealth = 0L
            for (i in 0 until player.inventory.capacity) {
                val item = player.inventory[i] ?: continue
                if (item.id == 995) wealth += item.amount // coins
                else wealth += item.getDef().cost.toLong() * item.amount
            }
            for (i in 0 until player.equipment.capacity) {
                val item = player.equipment[i] ?: continue
                wealth += item.getDef().cost.toLong() * item.amount
            }
            return wealth
        }

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
            meta.add("lifetime_stats", stats)
            return meta
        }

        fun saveRunRecord(player: Player, score: Int, corruption: Int, totalLevel: Int, wealth: Long) {
            val meta = loadMeta()
            val runs = meta.getAsJsonArray("runs")
            val stats = meta.getAsJsonObject("lifetime_stats")

            val runNumber = (stats.get("total_runs")?.asInt ?: 0) + 1
            stats.addProperty("total_runs", runNumber)
            if (score > (stats.get("best_score")?.asInt ?: 0)) stats.addProperty("best_score", score)
            if (totalLevel > (stats.get("highest_total_level")?.asInt ?: 0)) stats.addProperty("highest_total_level", totalLevel)
            if (corruption > (stats.get("highest_corruption")?.asInt ?: 0)) stats.addProperty("highest_corruption", corruption)

            val record = com.google.gson.JsonObject()
            record.addProperty("run_number", runNumber)
            record.addProperty("account", player.username)
            record.addProperty("score", score)
            record.addProperty("corruption", corruption)
            record.addProperty("total_level", totalLevel)
            record.addProperty("combat_level", player.combatLevel)
            record.addProperty("wealth", wealth)
            record.addProperty("timestamp", Instant.now().toString())
            runs.add(record)

            val file = File(META_PATH)
            file.parentFile?.mkdirs()
            file.writeText(gson.toJson(meta))
        }
    }
}
