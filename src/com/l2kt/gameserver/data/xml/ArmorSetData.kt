package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.ArmorSetData.forEach
import com.l2kt.gameserver.model.item.ArmorSet
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores [ArmorSet]s, the key being the chest item id.
 */
object ArmorSetData : IXmlReader {
    private val _armorSets = HashMap<Int, ArmorSet>()

    val sets: Collection<ArmorSet>
        get() = _armorSets.values

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/armorSets.xml")
        IXmlReader.LOGGER.info("Loaded {} armor sets.", _armorSets.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "armorset") { armorsetNode ->
                val set = parseAttributes(armorsetNode)
                _armorSets[set.getInteger("chest")] = ArmorSet(set)
            }
        }
    }

    fun getSet(chestId: Int): ArmorSet? {
        return _armorSets[chestId]
    }
}