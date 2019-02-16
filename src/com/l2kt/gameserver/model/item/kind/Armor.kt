package com.l2kt.gameserver.model.item.kind

import com.l2kt.gameserver.model.item.type.ArmorType
import com.l2kt.gameserver.model.item.type.ItemType
import com.l2kt.gameserver.templates.StatsSet

/**
 * This class is dedicated to the management of armors.
 */
class Armor
/**
 * Constructor for Armor.<BR></BR>
 * <BR></BR>
 * <U><I>Variables filled :</I></U><BR></BR>
 * <LI>_avoidModifier</LI>
 * <LI>_pDef & _mDef</LI>
 * <LI>_mpBonus & _hpBonus</LI>
 * @param set : StatsSet designating the set of couples (key,value) caracterizing the armor
 * @see Item constructor
 */
    (set: StatsSet) : Item(set) {
    private var _type: ArmorType? = null

    init {
        _type = ArmorType.valueOf(set.getString("armor_type", "none")!!.toUpperCase())

        val _bodyPart = bodyPart
        if (_bodyPart == Item.SLOT_NECK || _bodyPart == Item.SLOT_FACE || _bodyPart == Item.SLOT_HAIR || _bodyPart == Item.SLOT_HAIRALL || _bodyPart and Item.SLOT_L_EAR != 0 || _bodyPart and Item.SLOT_L_FINGER != 0 || _bodyPart and Item.SLOT_BACK != 0) {
            type1 = Item.TYPE1_WEAPON_RING_EARRING_NECKLACE
            type2 = Item.TYPE2_ACCESSORY
        } else {
            if (_type === ArmorType.NONE && bodyPart == Item.SLOT_L_HAND)
            // retail define shield as NONE
                _type = ArmorType.SHIELD

            type1 = Item.TYPE1_SHIELD_ARMOR
            type2 = Item.TYPE2_SHIELD_ARMOR
        }
    }

    override val itemMask: Int
        get() = itemType!!.mask()

    override val itemType: ItemType?
        get() = _type
}