package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.HennaData.forEach
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.Henna
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores [Henna]s infos. Hennas are called "dye" ingame.
 */
object HennaData : IXmlReader {
    private val _hennas = HashMap<Int, Henna>()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/hennas.xml")
        IXmlReader.LOGGER.info("Loaded {} hennas.", _hennas.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "henna") { hennaNode ->
                val set = parseAttributes(hennaNode)
                _hennas[set.getInteger("symbolId")] = Henna(set)
            }
        }
    }

    fun getHenna(id: Int): Henna {
        return _hennas[id]!!
    }

    /**
     * Retrieve all [Henna]s available for a [Player] class.
     * @param player : The Player used as class parameter.
     * @return a List of all available Hennas for this Player.
     */
    fun getAvailableHennasFor(player: Player): List<Henna> {
        return _hennas.values.filter { h -> h.canBeUsedBy(player) }
    }
}