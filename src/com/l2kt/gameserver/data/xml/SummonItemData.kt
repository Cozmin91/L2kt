package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.SummonItemData.forEach
import com.l2kt.gameserver.model.holder.IntIntHolder
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores summon items.<br></br>
 * TODO Delete it and move it back wherever it belongs.
 */
object SummonItemData : IXmlReader {
    private val _items = HashMap<Int, IntIntHolder>()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/summonItems.xml")
        IXmlReader.LOGGER.info("Loaded {} summon items.", _items.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "item") { itemNode ->
                val attrs = itemNode.attributes
                val itemId = parseInteger(attrs, "id")!!
                val npcId = parseInteger(attrs, "npcId")!!
                val summonType = parseInteger(attrs, "summonType")!!
                _items[itemId] = IntIntHolder(npcId, summonType)
            }
        }
    }

    fun getSummonItem(itemId: Int): IntIntHolder {
        return _items[itemId]!!
    }
}