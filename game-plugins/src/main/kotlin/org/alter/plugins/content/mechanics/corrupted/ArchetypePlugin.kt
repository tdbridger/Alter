package org.alter.plugins.content.mechanics.corrupted

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.move.moveTo
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.attr.NEW_ACCOUNT_ATTR
import org.alter.game.model.entity.Player
import org.alter.game.model.priv.Privilege
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import kotlin.random.Random

// Temp attributes for archetype choices (not persisted)
val ARCH1_DATA_ATTR = AttributeKey<String>()
val ARCH2_DATA_ATTR = AttributeKey<String>()
val ARCH3_DATA_ATTR = AttributeKey<String>()

// Persisted archetype attributes
val AFFINITY_ATTR = AttributeKey<String>(persistenceKey = "cg_affinity")
val RESTRICTION_ATTR = AttributeKey<String>(persistenceKey = "cg_restriction")

data class GeneratedArchetype(
    val name: String,
    val affinity: Affinity,
    val restriction: Restriction,
    val startLocation: StartLocation,
    val difficultyScore: Int,
    val multiplier: Double,
    val multiplierLabel: String
) {
    fun serialize(): String = "$name|${affinity.name}|${restriction.name}|${startLocation.name}|$difficultyScore|$multiplier|$multiplierLabel"

    companion object {
        fun deserialize(s: String): GeneratedArchetype {
            val parts = s.split("|")
            return GeneratedArchetype(
                name = parts[0],
                affinity = Affinity.valueOf(parts[1]),
                restriction = Restriction.valueOf(parts[2]),
                startLocation = StartLocation.valueOf(parts[3]),
                difficultyScore = parts[4].toInt(),
                multiplier = parts[5].toDouble(),
                multiplierLabel = parts[6]
            )
        }
    }
}

class ArchetypePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Present archetype selection to new accounts
        onLogin {
            val isNew = player.attr[NEW_ACCOUNT_ATTR] ?: false
            val chosen = player.attr[ARCHETYPE_CHOSEN_ATTR] ?: false

            if (isNew && !chosen) {
                // Generate 3 archetypes and store
                val archetypes = listOf(generateArchetype(), generateArchetype(), generateArchetype())
                player.attr[ARCH1_DATA_ATTR] = archetypes[0].serialize()
                player.attr[ARCH2_DATA_ATTR] = archetypes[1].serialize()
                player.attr[ARCH3_DATA_ATTR] = archetypes[2].serialize()

                // Display choices
                player.message("")
                player.message("<col=FFD700>=== Choose Your Fate ===</col>")
                player.message("")
                archetypes.forEachIndexed { index, arch ->
                    val skillNames = arch.affinity.boostedSkills.map { Skills.getSkillName(player.world, it) }.joinToString("/")
                    val xpInfo = if (skillNames.isNotEmpty()) "${arch.affinity.xpMultiplier}x $skillNames XP" else "${arch.affinity.xpMultiplier}x all skills XP"

                    player.message("<col=FFD700>[${index + 1}]</col> ${arch.name}")
                    player.message("    ${arch.affinity.displayName} ($xpInfo)")
                    player.message("    <col=FF6600>${arch.restriction.description}</col>")
                    player.message("    Starting: ${arch.startLocation.displayName}")
                    player.message("    Score multiplier: ${arch.multiplier}x (${arch.multiplierLabel})")
                    player.message("")
                }
                player.message("Type <col=FFD700>::choose 1</col>, <col=FFD700>::choose 2</col>, or <col=FFD700>::choose 3</col>")
            }
        }

        // ::choose command
        onCommand("choose", description = "Choose archetype") {
            val chosen = player.attr[ARCHETYPE_CHOSEN_ATTR] ?: false
            if (chosen) {
                player.message("You have already chosen your archetype.")
                return@onCommand
            }

            val args = player.getCommandArgs()
            val choice = args[0].toIntOrNull()
            if (choice == null || choice < 1 || choice > 3) {
                player.message("Usage: ::choose 1, ::choose 2, or ::choose 3")
                return@onCommand
            }

            val data = when (choice) {
                1 -> player.attr[ARCH1_DATA_ATTR]
                2 -> player.attr[ARCH2_DATA_ATTR]
                3 -> player.attr[ARCH3_DATA_ATTR]
                else -> null
            }

            if (data == null) {
                player.message("No archetypes available. Please relog.")
                return@onCommand
            }

            val archetype = GeneratedArchetype.deserialize(data)
            applyArchetype(player, archetype)

            // Clean up temp attributes
            player.attr.remove(ARCH1_DATA_ATTR)
            player.attr.remove(ARCH2_DATA_ATTR)
            player.attr.remove(ARCH3_DATA_ATTR)
        }

        // ::archetype command
        onCommand("archetype", description = "Show current archetype") {
            val name = player.attr[ARCHETYPE_NAME_ATTR] ?: ""
            if (name.isEmpty()) {
                player.message("No archetype selected yet.")
                return@onCommand
            }
            val affinityName = player.attr[AFFINITY_ATTR] ?: ""
            val restrictionName = player.attr[RESTRICTION_ATTR] ?: ""
            val multiplier = player.attr[ARCHETYPE_MULTIPLIER_ATTR] ?: 1.0

            val aff = try { Affinity.valueOf(affinityName) } catch (e: Exception) { null }
            val res = try { Restriction.valueOf(restrictionName) } catch (e: Exception) { null }

            player.message("=== $name ===")
            if (aff != null) {
                val skills = aff.boostedSkills.map { Skills.getSkillName(player.world, it) }.joinToString("/")
                val xpInfo = if (skills.isNotEmpty()) "${aff.xpMultiplier}x $skills XP" else "${aff.xpMultiplier}x all skills XP"
                player.message("Affinity: ${aff.displayName} ($xpInfo)")
            }
            if (res != null) {
                player.message("Restriction: ${res.description}")
            }
            player.message("Score Multiplier: ${multiplier}x")
        }
    }

    private fun applyArchetype(player: Player, archetype: GeneratedArchetype) {
        // Store persistent attributes
        player.attr[ARCHETYPE_NAME_ATTR] = archetype.name
        player.attr[AFFINITY_ATTR] = archetype.affinity.name
        player.attr[RESTRICTION_ATTR] = archetype.restriction.name
        player.attr[ARCHETYPE_MULTIPLIER_ATTR] = archetype.multiplier
        player.attr[ARCHETYPE_CHOSEN_ATTR] = true

        // Give starting gear
        for ((itemId, amount) in archetype.affinity.startingGear) {
            player.inventory.add(itemId, amount)
        }

        // Teleport to starting location
        player.moveTo(archetype.startLocation.x, archetype.startLocation.z)

        // Start corruption timer
        if (!player.timers.has(CORRUPTION_TIMER)) {
            player.timers[CORRUPTION_TIMER] = 100
        }

        // Announce
        player.message("")
        player.message("<col=FFD700>=== ${archetype.name} ===</col>")
        player.message("Affinity: ${archetype.affinity.displayName}")
        player.message("Restriction: <col=FF6600>${archetype.restriction.description}</col>")
        player.message("Starting Location: ${archetype.startLocation.displayName}")
        player.message("Score Multiplier: ${archetype.multiplier}x (${archetype.multiplierLabel})")
        player.message("")
        player.message("<col=8B0000>The corruption begins...</col>")
    }

    private fun generateArchetype(): GeneratedArchetype {
        val affinity = Affinity.values().random()
        val forbidden = AFFINITY_CONTRADICTIONS[affinity] ?: emptySet()
        val restriction = Restriction.values().filter { it !in forbidden }.random()
        val startLocation = StartLocation.values().random()
        val name = generateName(affinity, restriction)
        val difficultyScore = restriction.difficultyWeight + startLocation.difficultyWeight - (affinity.strengthRating / 2)
        val (multiplier, label) = getMultiplier(difficultyScore)
        return GeneratedArchetype(name, affinity, restriction, startLocation, difficultyScore, multiplier, label)
    }

    private fun generateName(affinity: Affinity, restriction: Restriction): String {
        val firstName = ARCHETYPE_NAMES.random()
        return if (Random.nextBoolean()) "$firstName the ${restriction.getFlavour()}" else "$firstName the ${affinity.getAdjective()}"
    }

    private fun getMultiplier(score: Int): Pair<Double, String> = when {
        score <= 4 -> 1.0 to "Easy"
        score <= 8 -> 1.3 to "Moderate"
        score <= 12 -> 1.6 to "Hard"
        score <= 16 -> 2.0 to "Brutal"
        else -> 2.5 to "Suicidal"
    }
}
