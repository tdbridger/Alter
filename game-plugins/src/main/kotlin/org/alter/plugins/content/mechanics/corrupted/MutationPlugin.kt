package org.alter.plugins.content.mechanics.corrupted

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import kotlin.random.Random

/**
 * Corrupted Gielinor Phase I: World Mutations
 *
 * 2-3 mutations rolled per run that modify global game rules.
 */

val MUTATION_1_ATTR = AttributeKey<String>(persistenceKey = "cg_mutation_1")
val MUTATION_2_ATTR = AttributeKey<String>(persistenceKey = "cg_mutation_2")
val MUTATION_3_ATTR = AttributeKey<String>(persistenceKey = "cg_mutation_3")
val MUTATION_COUNT_ATTR = AttributeKey<Int>(persistenceKey = "cg_mutation_count")

enum class Mutation(val displayName: String, val description: String) {
    DRAGONFIRE_SEASON("Dragonfire Season", "Dragon NPCs: 2x spawn rate, 5x drops"),
    PLAGUE_OF_UNDEAD("Plague of Undead", "Undead spawn in towns. Prayer gives 3x XP"),
    ECONOMIC_COLLAPSE("Economic Collapse", "Shop prices 3x. Thieving yields 2x"),
    WILDERNESS_AWAKENS("Wilderness Awakens", "Revenants spawn south of wilderness border"),
    BLESSING_OF_GUTHIX("Blessing of Guthix", "One random skill: 50x XP. One skill: locked"),
    SLAYERS_DOMINION("Slayer's Dominion", "Combat XP only from Slayer tasks. Points 10x"),
    BLOOD_MOON("Blood Moon", "Night phases 2x longer. Combat XP 2x at night, 0 during day"),
    IRONCLAD("Ironclad", "Equipment never degrades. Shops don't sell weapons/armour"),
    SWIFT_CORRUPTION("Swift Corruption", "Corruption rate 2x. XP rate also 2x"),
    GATHERING_STORM("Gathering Storm", "Skilling yield 2x. Lightning hits for 3 damage every 300 ticks");
}

object MutationRoller {
    fun rollMutations(): List<Mutation> {
        val count = Random.nextInt(2, 4) // 2-3 mutations
        val pool = Mutation.values().toMutableList()
        pool.shuffle()
        return pool.take(count)
    }

    fun applyMutations(player: Player, mutations: List<Mutation>) {
        val attrs = listOf(MUTATION_1_ATTR, MUTATION_2_ATTR, MUTATION_3_ATTR)
        mutations.forEachIndexed { i, mut -> if (i < 3) player.attr[attrs[i]] = mut.name }
        player.attr[MUTATION_COUNT_ATTR] = mutations.size
    }

    fun getMutations(player: Player): List<Mutation> {
        val attrs = listOf(MUTATION_1_ATTR, MUTATION_2_ATTR, MUTATION_3_ATTR)
        val count = player.attr[MUTATION_COUNT_ATTR] ?: 0
        return (0 until count).mapNotNull { i ->
            val name = player.attr[attrs[i]] ?: return@mapNotNull null
            try { Mutation.valueOf(name) } catch (e: Exception) { null }
        }
    }

    fun hasMutation(player: Player, mutation: Mutation): Boolean = getMutations(player).contains(mutation)

    fun displayMutations(player: Player, mutations: List<Mutation>) {
        player.message("")
        player.message("<col=FF66FF>=== World Mutations ===</col>")
        for (mut in mutations) {
            player.message("<col=FF66FF>${mut.displayName}</col>: ${mut.description}")
        }
        player.message("")
    }
}

class MutationPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        onCommand("mutations", description = "Show active world mutations") {
            val mutations = MutationRoller.getMutations(player)
            if (mutations.isEmpty()) {
                player.message("No mutations active. Choose an archetype first.")
            } else {
                MutationRoller.displayMutations(player, mutations)
            }
        }
    }
}
