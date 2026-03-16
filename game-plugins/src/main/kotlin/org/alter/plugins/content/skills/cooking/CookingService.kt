package org.alter.plugins.content.skills.cooking

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

class CookingService : Service {

    val entries: ObjectArrayList<CookingEntry> = ObjectArrayList()
    private val entriesByRaw: Int2ObjectOpenHashMap<CookingEntry> = Int2ObjectOpenHashMap()

    override fun init(server: Server, world: World, serviceProperties: ServerProperties) {
        val file = Paths.get(serviceProperties.get("cooking") ?: "../data/cfg/cooking/cooking.json")
        Files.newBufferedReader(file).use { reader ->
            val listType = object : TypeToken<List<CookingEntry>>() {}.type
            val loaded: List<CookingEntry> = Gson().fromJson(reader, listType)
            entries.addAll(loaded)
        }

        entries.forEach { entry ->
            entry.rawId = getRSCM(entry.raw)
            entry.cookedId = getRSCM(entry.cooked)
            entry.burntId = getRSCM(entry.burnt)
            entriesByRaw[entry.rawId] = entry
        }

        Server.logger.info { "Loaded ${entries.size.appendToString("cooking definition")}." }
    }

    override fun postLoad(server: Server, world: World) {}
    override fun bindNet(server: Server, world: World) {}
    override fun terminate(server: Server, world: World) {}

    fun lookupByRaw(rawId: Int): CookingEntry? = entriesByRaw[rawId]
}
