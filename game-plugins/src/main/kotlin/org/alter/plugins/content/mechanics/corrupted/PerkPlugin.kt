package org.alter.plugins.content.mechanics.corrupted

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import kotlin.random.Random

// Persistent perk attributes
val PERK_1_ATTR = AttributeKey<String>(persistenceKey = "cg_perk_1")
val PERK_2_ATTR = AttributeKey<String>(persistenceKey = "cg_perk_2")
val PERK_3_ATTR = AttributeKey<String>(persistenceKey = "cg_perk_3")
val PERK_4_ATTR = AttributeKey<String>(persistenceKey = "cg_perk_4")
val PERK_COUNT_ATTR = AttributeKey<Int>(persistenceKey = "cg_perk_count")

enum class PerkType { POSITIVE, NEGATIVE, NEUTRAL }

enum class Perk(val displayName: String, val type: PerkType, val description: String) {
    // Positive (40%)
    IRON_STOMACH("Iron Stomach", PerkType.POSITIVE, "Food heals 50% more"),
    FLEET_FOOTED("Fleet Footed", PerkType.POSITIVE, "Run energy never depletes"),
    LUCKY("Lucky", PerkType.POSITIVE, "Drop tables roll twice, keep the better result"),
    THICK_BLOODED("Thick Blooded", PerkType.POSITIVE, "Immune to poison and stat drain"),
    SCHOLAR("Scholar", PerkType.POSITIVE, "All XP gains doubled"),
    FORAGER("Forager", PerkType.POSITIVE, "Gathering skills yield double"),
    ADRENALINE("Adrenaline", PerkType.POSITIVE, "Special attack energy regenerates 5x faster"),
    MERCHANTS_FAVOUR("Merchant's Favour", PerkType.POSITIVE, "Shop prices halved"),
    SECOND_WIND("Second Wind", PerkType.POSITIVE, "Once per run, survive a killing blow at 1 HP"),

    // Negative (40%)
    GLASSBONES("Glassbones", PerkType.NEGATIVE, "Max HP permanently halved"),
    PACIFIST("Pacifist", PerkType.NEGATIVE, "Cannot deal direct damage"),
    BUTTERFINGERS("Butterfingers", PerkType.NEGATIVE, "10% chance per action to drop held item"),
    INSOMNIAC("Insomniac", PerkType.NEGATIVE, "Night phase penalties doubled"),
    CLUMSY("Clumsy", PerkType.NEGATIVE, "Take 1-3 unavoidable damage every 200 ticks"),
    MARKED("Marked", PerkType.NEGATIVE, "All NPCs within 15 tiles aggressive"),
    HEAVY_POCKETS("Heavy Pockets", PerkType.NEGATIVE, "Walking speed only, no running"),
    AMNESIA("Amnesia", PerkType.NEGATIVE, "Cannot use banks"),
    MUTE("Mute", PerkType.NEGATIVE, "Cannot activate prayers"),

    // Neutral (20%)
    VAMPIRIC("Vampiric", PerkType.NEUTRAL, "Heal 10% of damage dealt, take 1 per 30 ticks"),
    GAMBLER("Gambler", PerkType.NEUTRAL, "Every XP drop: 50% doubled, 50% zero"),
    GIGANTISM("Gigantism", PerkType.NEUTRAL, "All NPC HP doubled, all NPC drops doubled"),
    NEMESIS("Nemesis", PerkType.NEUTRAL, "One random NPC: 5x spawn, 2x stats, 10x XP/drops"),
    ECHOES("Echoes", PerkType.NEUTRAL, "5% chance killed NPC respawns stronger (+20 combat)");

    companion object {
        val POSITIVES = values().filter { it.type == PerkType.POSITIVE }
        val NEGATIVES = values().filter { it.type == PerkType.NEGATIVE }
        val NEUTRALS = values().filter { it.type == PerkType.NEUTRAL }

        val CONTRADICTIONS = setOf(
            setOf(PACIFIST, VAMPIRIC),
            setOf(AMNESIA, MERCHANTS_FAVOUR),
            setOf(FLEET_FOOTED, HEAVY_POCKETS)
        )
    }
}

object PerkRoller {
    fun rollPerks(restriction: String?): List<Perk> {
        val selected = mutableListOf<Perk>()
        for (i in 0 until 4) {
            val roll = Random.nextInt(100)
            val type = when {
                roll < 40 -> PerkType.POSITIVE
                roll < 80 -> PerkType.NEGATIVE
                else -> PerkType.NEUTRAL
            }
            val pool = when (type) {
                PerkType.POSITIVE -> Perk.POSITIVES
                PerkType.NEGATIVE -> Perk.NEGATIVES
                PerkType.NEUTRAL -> Perk.NEUTRALS
            }
            val valid = pool.filter { perk ->
                perk !in selected && !hasContradiction(perk, selected)
            }
            if (valid.isNotEmpty()) {
                selected.add(valid.random())
            } else {
                val fallback = Perk.values().filter { it !in selected && !hasContradiction(it, selected) }
                if (fallback.isNotEmpty()) selected.add(fallback.random())
            }
        }
        return selected
    }

    private fun hasContradiction(perk: Perk, existing: List<Perk>): Boolean {
        return existing.any { other -> Perk.CONTRADICTIONS.any { it == setOf(perk, other) } }
    }

    fun applyPerks(player: Player, perks: List<Perk>) {
        val attrs = listOf(PERK_1_ATTR, PERK_2_ATTR, PERK_3_ATTR, PERK_4_ATTR)
        perks.forEachIndexed { i, perk -> if (i < 4) player.attr[attrs[i]] = perk.name }
        player.attr[PERK_COUNT_ATTR] = perks.size
    }

    fun getPerks(player: Player): List<Perk> {
        val attrs = listOf(PERK_1_ATTR, PERK_2_ATTR, PERK_3_ATTR, PERK_4_ATTR)
        val count = player.attr[PERK_COUNT_ATTR] ?: 0
        return (0 until count).mapNotNull { i ->
            val name = player.attr[attrs[i]] ?: return@mapNotNull null
            try { Perk.valueOf(name) } catch (e: Exception) { null }
        }
    }

    fun hasPerk(player: Player, perk: Perk): Boolean = getPerks(player).contains(perk)

    fun displayPerks(player: Player, perks: List<Perk>) {
        player.message("")
        player.message("<col=FFD700>=== Your Perks ===</col>")
        for (perk in perks) {
            val color = when (perk.type) {
                PerkType.POSITIVE -> "<col=00FF00>"
                PerkType.NEGATIVE -> "<col=FF0000>"
                PerkType.NEUTRAL -> "<col=FFFF00>"
            }
            player.message("$color${perk.displayName}</col>: ${perk.description}")
        }
        val negCount = perks.count { it.type == PerkType.NEGATIVE }
        val negMult = when (negCount) { 0 -> 1.0; 1 -> 1.2; 2 -> 1.5; 3 -> 2.0; else -> 3.0 }
        if (negMult > 1.0) player.message("<col=FFD700>Negative perk bonus: ${negMult}x legacy points</col>")
        player.message("")
    }
}

class PerkPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        onCommand("perks", description = "Show active perks") {
            val perks = PerkRoller.getPerks(player)
            if (perks.isEmpty()) {
                player.message("No perks active. Choose an archetype first.")
            } else {
                PerkRoller.displayPerks(player, perks)
            }
        }
    }
}
