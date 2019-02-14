package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.xml.FishData.forEach
import com.l2kt.gameserver.model.Fish
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores [Fish] infos.<br></br>
 * TODO Plain wrong values and system, have to be reworked entirely.
 */
object FishData : IXmlReader {
    private val _fish = ArrayList<Fish>()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/fish.xml")
        IXmlReader.LOGGER.info("Loaded {} fish.", _fish.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "fish") { fishNode ->
                _fish.add(
                    Fish(
                        parseAttributes(
                            fishNode
                        )
                    )
                )
            }
        }
    }

    /**
     * Get a random [Fish] based on level, type and group.
     * @param lvl : the fish level to check.
     * @param type : the fish type to check.
     * @param group : the fish group to check.
     * @return a Fish with good criterias.
     */
    fun getFish(lvl: Int, type: Int, group: Int): Fish {
        return Rnd[_fish.filter { f -> f.level == lvl && f.type == type && f.group == group }]!!
    }
}