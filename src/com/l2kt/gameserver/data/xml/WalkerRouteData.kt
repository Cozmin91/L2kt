package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.WalkerRouteData.forEach
import com.l2kt.gameserver.model.location.WalkerLocation
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores routes for Walker NPCs, under a List of [WalkerLocation] ; the key being the npcId.
 */
object WalkerRouteData : IXmlReader {
    private val _routes = HashMap<Int, List<WalkerLocation>>()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/walkerRoutes.xml")
        IXmlReader.LOGGER.info("Loaded {} Walker routes.", _routes.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "route") { routeNode ->
                val attrs = routeNode.attributes
                val list = ArrayList<WalkerLocation>()
                val npcId = parseInteger(attrs, "npcId")!!
                val run = parseBoolean(attrs, "run")!!
                forEach(routeNode, "node") { nodeNode -> list.add(WalkerLocation(parseAttributes(nodeNode), run)) }
                _routes[npcId] = list
            }
        }
    }

    fun reload() {
        _routes.clear()

        load()
    }

    fun getWalkerRoute(npcId: Int): List<WalkerLocation> {
        return _routes[npcId]!!
    }
}