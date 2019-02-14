package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.TeleportLocationData.forEach
import com.l2kt.gameserver.model.location.TeleportLocation
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores [TeleportLocation]s.
 */
object TeleportLocationData : IXmlReader {
    private val _teleports = HashMap<Int, TeleportLocation>()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/teleportLocations.xml")
        IXmlReader.LOGGER.info("Loaded {} teleport locations.", _teleports.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "teleport") { teleportNode ->
                val set = parseAttributes(teleportNode)
                _teleports[set.getInteger("id")] = TeleportLocation(set)
            }
        }
    }

    fun reload() {
        _teleports.clear()
        load()
    }

    fun getTeleportLocation(id: Int): TeleportLocation {
        return _teleports[id]!!
    }
}