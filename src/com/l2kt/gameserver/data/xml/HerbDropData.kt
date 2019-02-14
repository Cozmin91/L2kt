package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.HerbDropData.forEach
import com.l2kt.gameserver.model.item.DropCategory
import com.l2kt.gameserver.model.item.DropData
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads herbs drop rules.<br></br>
 * TODO parse L2OFF GF (since IL doesn't exist) and introduce the additional droplist concept directly on npc data XMLs.
 */
object HerbDropData : IXmlReader {
    private val _herbGroups = HashMap<Int, MutableList<DropCategory>>()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/herbDrops.xml")
        IXmlReader.LOGGER.info("Loaded {} herbs groups.", _herbGroups.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "group") { groupNode ->
                val groupId = parseInteger(groupNode.attributes, "id")!!
                val category =
                    (_herbGroups).computeIfAbsent(groupId) { k -> ArrayList() }
                forEach(groupNode, "item") { itemNode ->
                    val attrs = itemNode.attributes
                    val id = parseInteger(attrs, "id")!!
                    val categoryType = parseInteger(attrs, "category")!!
                    val chance = parseInteger(attrs, "chance")!!
                    val dropDat = DropData()
                    dropDat.itemId = id
                    dropDat.minDrop = 1
                    dropDat.maxDrop = 1
                    dropDat.chance = chance

                    var catExists = false
                    for (cat in category) {
                        if (cat.categoryType == categoryType) {
                            cat.addDropData(dropDat, false)
                            catExists = true
                            break
                        }
                    }
                    if (!catExists) {
                        val cat = DropCategory(categoryType)
                        cat.addDropData(dropDat, false)
                        category.add(cat)
                    }
                }
            }
        }
    }

    fun getHerbDroplist(groupId: Int): List<DropCategory> {
        return _herbGroups[groupId]!!
    }
}