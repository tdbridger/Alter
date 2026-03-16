package org.alter.plugins.content.skills.mining

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

class MiningService : Service {

    val entries: ObjectArrayList<MiningEntry> = ObjectArrayList()
    private val entriesByObject: Int2ObjectOpenHashMap<MiningEntry> = Int2ObjectOpenHashMap()

    override fun init(server: Server, world: World, serviceProperties: ServerProperties) {
        val file = Paths.get(serviceProperties.get("mining") ?: "../data/cfg/mining/mining.json")

        Files.newBufferedReader(file).use { reader ->
            val listType = object : TypeToken<List<MiningEntry>>() {}.type
            val loaded: List<MiningEntry> = Gson().fromJson(reader, listType)
            entries.addAll(loaded)
        }

        entries.forEach { entry ->
            entry.objectIds = entry.objects.map { getRSCM(it) }.toIntArray()
            entry.emptyObjectId = getRSCM(entry.emptyObject)
            entry.ores.forEach { loot -> loot.itemId = getRSCM(loot.item) }
            entry.objectIds.forEach { id -> entriesByObject[id] = entry }
        }

        Pickaxe.values().forEach { pick -> pick.itemId = getRSCM(pick.itemName) }

        Server.logger.info { "Loaded ${entries.size.appendToString("mining definition")}." }
    }

    override fun postLoad(server: Server, world: World) {}
    override fun bindNet(server: Server, world: World) {}
    override fun terminate(server: Server, world: World) {}

    fun lookup(objectId: Int): MiningEntry? = entriesByObject[objectId]
}
