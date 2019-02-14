package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.NewbieBuffData.forEach
import com.l2kt.gameserver.model.NewbieBuff
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and store [NewbieBuff] into a List.
 */
object NewbieBuffData : IXmlReader {
    private val _buffs = ArrayList<NewbieBuff>()

    var magicLowestLevel = 100
        private set

    var physicLowestLevel = 100
        private set

    var magicHighestLevel = 1
        private set

    var physicHighestLevel = 1
        private set

    val buffs: List<NewbieBuff>
        get() = _buffs

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/newbieBuffs.xml")
        IXmlReader.LOGGER.info("Loaded {} newbie buffs.", _buffs.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "buff") { buffNode ->
                val set = parseAttributes(buffNode)
                val lowerLevel = set.getInteger("lowerLevel")
                val upperLevel = set.getInteger("upperLevel")
                if (set.getBool("isMagicClass")) {
                    if (lowerLevel < magicLowestLevel)
                        magicLowestLevel = lowerLevel
                    if (upperLevel > magicHighestLevel)
                        magicHighestLevel = upperLevel
                } else {
                    if (lowerLevel < physicLowestLevel)
                        physicLowestLevel = lowerLevel
                    if (upperLevel > physicHighestLevel)
                        physicHighestLevel = upperLevel
                }
                _buffs.add(NewbieBuff(set))
            }
        }
    }
}