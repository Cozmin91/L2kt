package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.SoulCrystalData.forEach
import com.l2kt.gameserver.model.soulcrystal.LevelingInfo
import com.l2kt.gameserver.model.soulcrystal.SoulCrystal
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores following Soul Crystal infos :
 *
 *  * [SoulCrystal] infos related to items (such as level, initial / broken / succeeded itemId) ;
 *  * [LevelingInfo] infos related to NPCs (such as absorb type, chances of fail/success, if the item cast needs to be done and the list of allowed crystal levels).
 *
 */
object SoulCrystalData : IXmlReader {
    private val _soulCrystals = HashMap<Int, SoulCrystal>()
    private val _levelingInfos = HashMap<Int, LevelingInfo>()

    val soulCrystals: Map<Int, SoulCrystal>
        get() = _soulCrystals

    val levelingInfos: Map<Int, LevelingInfo>
        get() = _levelingInfos

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/soulCrystals.xml")
        IXmlReader.LOGGER.info(
            "Loaded {} Soul Crystals data and {} NPCs data.",
            _soulCrystals.size,
            _levelingInfos.size
        )
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "crystals") { crystalsNode ->
                forEach(crystalsNode, "crystal") { crystalNode ->
                    val set = parseAttributes(crystalNode)
                    _soulCrystals[set.getInteger("initial")] = SoulCrystal(set)
                }
            }
            forEach(listNode, "npcs") { npcsNode ->
                forEach(npcsNode, "npc") { npcNode ->
                    val set = parseAttributes(npcNode)
                    _levelingInfos[set.getInteger("id")] = LevelingInfo(set)
                }
            }
        }
    }
}