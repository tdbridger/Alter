package org.alter.plugins.content.skills.fishing

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import gg.rsmod.util.ServerProperties
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.alter.api.ext.appendToString
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.service.Service
import org.alter.rscm.RSCM.getRSCM
import java.nio.file.Files
import java.nio.file.Paths

class FishingService : Service {

    val entries: ObjectArrayList<FishingSpotEntry> = ObjectArrayList()
    // Map from npcId to list of entries (one NPC can have multiple options like "net" and "bait")
    private val entriesByNpc: Int2ObjectOpenHashMap<MutableList<FishingSpotEntry>> = Int2ObjectOpenHashMap()

    override fun init(server: Server, world: World, serviceProperties: ServerProperties) {
        val file = Paths.get(serviceProperties.get("fishing") ?: "../data/cfg/fishing/fishing.json")

        Files.newBufferedReader(file).use { reader ->
            val listType = object : TypeToken<List<FishingSpotEntry>>() {}.type
            val loaded: List<FishingSpotEntry> = Gson().fromJson(reader, listType)
            entries.addAll(loaded)
        }

        entries.forEach { entry ->
            entry.npcIds = entry.npcs.map { getRSCM(it) }.toIntArray()
            entry.toolId = getRSCM(entry.tool)
            if (entry.bait != null) entry.baitId = getRSCM(entry.bait)
            entry.fish.forEach { loot -> loot.itemId = getRSCM(loot.item) }
            entry.npcIds.forEach { id ->
                entriesByNpc.getOrPut(id) { mutableListOf() }.add(entry)
            }
        }

        Server.logger.info { "Loaded ${entries.size.appendToString("fishing definition")}." }
    }

    override fun postLoad(server: Server, world: World) {}
    override fun bindNet(server: Server, world: World) {}
    override fun terminate(server: Server, world: World) {}

    fun lookup(npcId: Int, option: String): FishingSpotEntry? {
        return entriesByNpc[npcId]?.firstOrNull { it.option.equals(option, ignoreCase = true) }
    }
}
