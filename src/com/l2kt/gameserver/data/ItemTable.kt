package com.l2kt.gameserver.data

import com.l2kt.gameserver.model.item.kind.Armor
import com.l2kt.gameserver.model.item.kind.EtcItem
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.skills.DocumentItem
import java.io.File
import java.util.*
import java.util.logging.Logger

object ItemTable {

    private lateinit var _allTemplates: Array<Item?>
    private val _log = Logger.getLogger(ItemTable::class.java.name)

    var slots = mutableMapOf<String, Int>()
    private val _armors = HashMap<Int, Armor>()
    private val _etcItems = HashMap<Int, EtcItem>()
    private val _weapons = HashMap<Int, Weapon>()

    init {
        slots["chest"] = Item.SLOT_CHEST
        slots["fullarmor"] = Item.SLOT_FULL_ARMOR
        slots["alldress"] = Item.SLOT_ALLDRESS
        slots["head"] = Item.SLOT_HEAD
        slots["hair"] = Item.SLOT_HAIR
        slots["face"] = Item.SLOT_FACE
        slots["hairall"] = Item.SLOT_HAIRALL
        slots["underwear"] = Item.SLOT_UNDERWEAR
        slots["back"] = Item.SLOT_BACK
        slots["neck"] = Item.SLOT_NECK
        slots["legs"] = Item.SLOT_LEGS
        slots["feet"] = Item.SLOT_FEET
        slots["gloves"] = Item.SLOT_GLOVES
        slots["chest,legs"] = Item.SLOT_CHEST or Item.SLOT_LEGS
        slots["rhand"] = Item.SLOT_R_HAND
        slots["lhand"] = Item.SLOT_L_HAND
        slots["lrhand"] = Item.SLOT_LR_HAND
        slots["rear;lear"] = Item.SLOT_R_EAR or Item.SLOT_L_EAR
        slots["rfinger;lfinger"] = Item.SLOT_R_FINGER or Item.SLOT_L_FINGER
        slots["none"] = Item.SLOT_NONE
        slots["wolf"] = Item.SLOT_WOLF // for wolf
        slots["hatchling"] = Item.SLOT_HATCHLING // for hatchling
        slots["strider"] = Item.SLOT_STRIDER // for strider
        slots["babypet"] = Item.SLOT_BABYPET // for babypet
        load()
    }

    private fun load() {
        val dir = File("./data/xml/items")

        var highest = 0
        for (file in dir.listFiles()!!) {
            val document = DocumentItem(file)
            document.parse()

            for (item in document.itemList) {
                if (highest < item.itemId)
                    highest = item.itemId

                when (item) {
                    is EtcItem -> _etcItems[item.itemId] = item
                    is Armor -> _armors[item.itemId] = item
                    else -> _weapons[item.itemId] = item as Weapon
                }
            }
        }

        _log.info("ItemTable: Highest used itemID : $highest")

        // Feed an array with all items templates.
        _allTemplates = arrayOfNulls(highest + 1)

        for (item in _armors.values)
            _allTemplates[item.itemId] = item

        for (item in _weapons.values)
            _allTemplates[item.itemId] = item

        for (item in _etcItems.values)
            _allTemplates[item.itemId] = item
    }

    /**
     * @param id : int designating the item
     * @return the item corresponding to the item ID.
     */
    fun getTemplate(id: Int): Item? {
        return if (id >= _allTemplates.size) null else _allTemplates[id]

    }

    fun reload() {
        _armors.clear()
        _etcItems.clear()
        _weapons.clear()

        load()
    }
}