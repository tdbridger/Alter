package org.alter.plugins.content.skills.woodcutting

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

class WoodcuttingService : Service {

    val entries: ObjectArrayList<WoodcuttingEntry> = ObjectArrayList()
    private val entriesByObject: Int2ObjectOpenHashMap<WoodcuttingEntry> = Int2ObjectOpenHashMap()

    override fun init(server: Server, world: World, serviceProperties: ServerProperties) {
        val file = Paths.get(serviceProperties.get("woodcutting") ?: "../data/cfg/woodcutting/woodcutting.json")

        Files.newBufferedReader(file).use { reader ->
            val listType = object : TypeToken<List<WoodcuttingEntry>>() {}.type
            val loaded: List<WoodcuttingEntry> = Gson().fromJson(reader, listType)
            entries.addAll(loaded)
        }

        // Resolve RSCM names to IDs
        entries.forEach { entry ->
            entry.objectIds = entry.objects.map { getRSCM(it) }.toIntArray()
            entry.emptyObjectId = getRSCM(entry.emptyObject)
            entry.logs.forEach { loot -> loot.itemId = getRSCM(loot.item) }
            entry.objectIds.forEach { id -> entriesByObject[id] = entry }
        }

        // Resolve axe IDs
        Axe.values().forEach { axe -> axe.itemId = getRSCM(axe.itemName) }

        Server.logger.info { "Loaded ${entries.size.appendToString("woodcutting definition")}." }
    }

    override fun postLoad(server: Server, world: World) {}
    override fun bindNet(server: Server, world: World) {}
    override fun terminate(server: Server, world: World) {}

    fun lookup(objectId: Int): WoodcuttingEntry? = entriesByObject[objectId]
}
