package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.MultisellData.forEach
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.multisell.Entry
import com.l2kt.gameserver.model.multisell.Ingredient
import com.l2kt.gameserver.model.multisell.ListContainer
import com.l2kt.gameserver.model.multisell.PreparedListContainer
import com.l2kt.gameserver.network.serverpackets.MultiSellList
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores multisell lists under [ListContainer].<br></br>
 * Each ListContainer contains a List of [Entry], and the list of allowed npcIds.<br></br>
 * <br></br>
 * File name is used as key, under its String hashCode.
 */
object MultisellData : IXmlReader {

    private val _entries = HashMap<Int, ListContainer>()
    const val PAGE_SIZE = 40

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/multisell")
        IXmlReader.LOGGER.info("Loaded {} multisell.", _entries.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        val id = path.toFile().nameWithoutExtension.hashCode()
        val list = ListContainer(id)
        forEach(doc, "list") { listNode ->
            val attrs = listNode.attributes

            list.applyTaxes = parseBoolean(attrs, "applyTaxes", false)!!
            list.maintainEnchantment = parseBoolean(attrs, "maintainEnchantment", false)!!

            forEach(listNode, "item") { itemNode ->
                val ingredients = ArrayList<Ingredient>()
                val products = ArrayList<Ingredient>()
                forEach(
                    itemNode,
                    "ingredient"
                ) { ingredientNode -> ingredients.add(Ingredient(parseAttributes(ingredientNode))) }
                forEach(
                    itemNode,
                    "production"
                ) { productionNode -> products.add(Ingredient(parseAttributes(productionNode))) }
                list.entries.add(Entry(ingredients, products))
            }
            forEach(listNode, "npcs") { npcsNode ->
                forEach(
                    npcsNode,
                    "npc"
                ) { npcNode -> list.allowNpc(Integer.parseInt(npcNode.textContent)) }
            }

            _entries[id] = list
        }
    }

    fun reload() {
        _entries.clear()
        load()
    }

    /**
     * Send the correct multisell content to a [Player].<br></br>
     * <br></br>
     * [ListContainer] template is first retrieved, based on its name, then [Npc] npcId check is done for security reason. Then the content is sent into [PreparedListContainer], notably to check Player inventory. Finally a [MultiSellList] packet is sent to the Player. That
     * new, prepared list is kept in memory on Player instance, mostly for memory reason.
     * @param listName : The ListContainer list name.
     * @param player : The Player to check.
     * @param npc : The Npc to check (notably used for npcId check).
     * @param inventoryOnly : if true we check inventory content.
     */
    fun separateAndSend(listName: String, player: Player, npc: Npc?, inventoryOnly: Boolean) {
        val template = _entries[listName.hashCode()] ?: return

        if (npc != null && !template.isNpcAllowed(npc.npcId) || npc == null && template.isNpcOnly)
            return

        val list = PreparedListContainer(template, inventoryOnly, player, npc)

        var index = 0
        do {
            // send list at least once even if size = 0
            player.sendPacket(MultiSellList(list, index))
            index += PAGE_SIZE
        } while (index < list.entries.size)

        player.multiSell = list
    }

    fun getList(listName: String): ListContainer {
        return _entries[listName.hashCode()]!!
    }
}