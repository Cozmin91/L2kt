package com.l2kt.gameserver.data.manager

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.manager.BuyListManager.forEach
import com.l2kt.gameserver.model.buylist.NpcBuyList
import com.l2kt.gameserver.model.buylist.Product
import com.l2kt.gameserver.taskmanager.BuyListTaskManager
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * Loads and stores [NpcBuyList], which is the most common way to show/sell items, with multisell.<br></br>
 * <br></br>
 * NpcBuyList owns a list of [Product]. Each of them can have a count, making the item acquisition impossible until the next restock timer (stored as SQL data). The count timer is stored on a global task, called [BuyListTaskManager].
 */
object BuyListManager : IXmlReader {
    private val _buyLists = HashMap<Int, NpcBuyList>()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/buyLists.xml")
        IXmlReader.LOGGER.info("Loaded {} buyLists.", _buyLists.size)

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement("SELECT * FROM `buylists`").use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val buyListId = rs.getInt("buylist_id")
                            val itemId = rs.getInt("item_id")
                            val count = rs.getInt("count")
                            val nextRestockTime = rs.getLong("next_restock_time")

                            val buyList = _buyLists[buyListId] ?: continue

                            val product = buyList.getProductByItemId(itemId) ?: continue

                            BuyListTaskManager.test(product, count, nextRestockTime)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            IXmlReader.LOGGER.error("Failed to load buyList data from database.", e)
        }

    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "buyList") { buyListNode ->
                val attrs = buyListNode.attributes
                val buyListId = parseInteger(attrs, "id")!!
                val buyList = NpcBuyList(buyListId)
                buyList.npcId = parseInteger(attrs, "npcId")!!
                forEach(buyListNode, "product") { productNode ->
                    buyList.addProduct(
                        Product(
                            buyListId,
                            parseAttributes(productNode)
                        )
                    )
                }
                _buyLists[buyListId] = buyList
            }
        }
    }

    fun getBuyList(listId: Int): NpcBuyList? {
        return _buyLists[listId]
    }

    fun getBuyListsByNpcId(npcId: Int): List<NpcBuyList> {
        return _buyLists.values.filter { b -> b.isNpcAllowed(npcId) }
    }
}