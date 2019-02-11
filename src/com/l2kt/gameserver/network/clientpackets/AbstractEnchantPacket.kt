package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.item.type.CrystalType
import com.l2kt.gameserver.model.item.type.WeaponType

abstract class AbstractEnchantPacket : L2GameClientPacket() {

    class EnchantScroll(
        protected val _isWeapon: Boolean,
        /**
         * @return true if item is a blessed scroll.
         */
        val isBlessed: Boolean,
        /**
         * @return true if item is a crystal scroll.
         */
        val isCrystal: Boolean, protected val _grade: CrystalType
    ) {

        /**
         * @param enchantItem : The item to enchant.
         * @return true if support item can be used for this item
         */
        fun isValid(enchantItem: ItemInstance?): Boolean {
            if (enchantItem == null)
                return false

            // checking scroll type and configured maximum enchant level
            when (enchantItem.item.type2) {
                Item.TYPE2_WEAPON -> if (!_isWeapon || Config.ENCHANT_MAX_WEAPON > 0 && enchantItem.enchantLevel >= Config.ENCHANT_MAX_WEAPON)
                    return false

                Item.TYPE2_SHIELD_ARMOR, Item.TYPE2_ACCESSORY -> if (_isWeapon || Config.ENCHANT_MAX_ARMOR > 0 && enchantItem.enchantLevel >= Config.ENCHANT_MAX_ARMOR)
                    return false

                else -> return false
            }

            // check for crystal type
            return _grade == enchantItem.item.crystalType

        }

        /**
         * Regarding enchant system :<br></br>
         * <br></br>
         * <u>Weapons</u>
         *
         *  * magic weapons has chance of 40% until +15 and 20% from +15 and higher. There is no upper limit, there is no dependance on current enchant level.
         *  * non magic weapons has chance of 70% until +15 and 35% from +15 and higher. There is no upper limit, there is no dependance on current enchant level.
         *
         * <u>Armors</u>
         *
         *  * non fullbody armors (jewelry, upper armor, lower armor, boots, gloves, helmets and shirts) has chance of 2/3 for +4, 1/3 for +5, 1/4 for +6, ...., 1/18 +20. If you've made a +20 armor, chance to make it +21 will be equal to zero (0%).
         *  * full body armors has a chance of 1/1 for +4, 2/3 for +5, 1/3 for +6, ..., 1/17 for +20. If you've made a +20 armor, chance to make it +21 will be equal to zero (0%).
         *
         * @param enchantItem : The item to enchant.
         * @return the enchant chance under double format (0.7 / 0.35 / 0.44324...).
         */
        fun getChance(enchantItem: ItemInstance): Double {
            if (!isValid(enchantItem))
                return -1.0

            val fullBody = enchantItem.item.bodyPart == Item.SLOT_FULL_ARMOR
            if (enchantItem.enchantLevel < Config.ENCHANT_SAFE_MAX || fullBody && enchantItem.enchantLevel < Config.ENCHANT_SAFE_MAX_FULL)
                return 1.0

            var chance = 0.0

            // Armor formula : 0.66^(current-2), chance is lower and lower for each enchant.
            if (enchantItem.isArmor)
                chance = Math.pow(Config.ENCHANT_CHANCE_ARMOR, (enchantItem.enchantLevel - 2).toDouble())
            else if (enchantItem.isWeapon) {
                if ((enchantItem.item as Weapon).isMagical)
                    chance =
                            if (enchantItem.enchantLevel > 14) Config.ENCHANT_CHANCE_WEAPON_MAGIC_15PLUS else Config.ENCHANT_CHANCE_WEAPON_MAGIC
                else
                    chance =
                            if (enchantItem.enchantLevel > 14) Config.ENCHANT_CHANCE_WEAPON_NONMAGIC_15PLUS else Config.ENCHANT_CHANCE_WEAPON_NONMAGIC
            }// Weapon formula is 70% for fighter weapon, 40% for mage weapon. Special rates after +14.

            return chance
        }
    }

    companion object {
        val _scrolls = mutableMapOf<Int, EnchantScroll>()

        /**
         * Format : itemId, (isWeapon, isBlessed, isCrystal, grade)<br></br>
         * Allowed items IDs must be sorted by ascending order.
         */
        init {
            // Scrolls: Enchant Weapon
            _scrolls[729] = EnchantScroll(true, false, false, CrystalType.A)
            _scrolls[947] = EnchantScroll(true, false, false, CrystalType.B)
            _scrolls[951] = EnchantScroll(true, false, false, CrystalType.C)
            _scrolls[955] = EnchantScroll(true, false, false, CrystalType.D)
            _scrolls[959] = EnchantScroll(true, false, false, CrystalType.S)

            // Scrolls: Enchant Armor
            _scrolls[730] = EnchantScroll(false, false, false, CrystalType.A)
            _scrolls[948] = EnchantScroll(false, false, false, CrystalType.B)
            _scrolls[952] = EnchantScroll(false, false, false, CrystalType.C)
            _scrolls[956] = EnchantScroll(false, false, false, CrystalType.D)
            _scrolls[960] = EnchantScroll(false, false, false, CrystalType.S)

            // Blessed Scrolls: Enchant Weapon
            _scrolls[6569] = EnchantScroll(true, true, false, CrystalType.A)
            _scrolls[6571] = EnchantScroll(true, true, false, CrystalType.B)
            _scrolls[6573] = EnchantScroll(true, true, false, CrystalType.C)
            _scrolls[6575] = EnchantScroll(true, true, false, CrystalType.D)
            _scrolls[6577] = EnchantScroll(true, true, false, CrystalType.S)

            // Blessed Scrolls: Enchant Armor
            _scrolls[6570] = EnchantScroll(false, true, false, CrystalType.A)
            _scrolls[6572] = EnchantScroll(false, true, false, CrystalType.B)
            _scrolls[6574] = EnchantScroll(false, true, false, CrystalType.C)
            _scrolls[6576] = EnchantScroll(false, true, false, CrystalType.D)
            _scrolls[6578] = EnchantScroll(false, true, false, CrystalType.S)

            // Crystal Scrolls: Enchant Weapon
            _scrolls[731] = EnchantScroll(true, false, true, CrystalType.A)
            _scrolls[949] = EnchantScroll(true, false, true, CrystalType.B)
            _scrolls[953] = EnchantScroll(true, false, true, CrystalType.C)
            _scrolls[957] = EnchantScroll(true, false, true, CrystalType.D)
            _scrolls[961] = EnchantScroll(true, false, true, CrystalType.S)

            // Crystal Scrolls: Enchant Armor
            _scrolls[732] = EnchantScroll(false, false, true, CrystalType.A)
            _scrolls[950] = EnchantScroll(false, false, true, CrystalType.B)
            _scrolls[954] = EnchantScroll(false, false, true, CrystalType.C)
            _scrolls[958] = EnchantScroll(false, false, true, CrystalType.D)
            _scrolls[962] = EnchantScroll(false, false, true, CrystalType.S)
        }

        /**
         * @param scroll The instance of item to make checks on.
         * @return enchant template for scroll.
         */
        fun getEnchantScroll(scroll: ItemInstance): EnchantScroll? {
            return _scrolls[scroll.itemId]
        }

        /**
         * @param item The instance of item to make checks on.
         * @return true if item can be enchanted.
         */
        fun isEnchantable(item: ItemInstance): Boolean {
            if (item.isHeroItem || item.isShadowItem || item.isEtcItem || item.item.itemType === WeaponType.FISHINGROD)
                return false

            // only equipped items or in inventory can be enchanted
            return if (item.location != ItemInstance.ItemLocation.INVENTORY && item.location != ItemInstance.ItemLocation.PAPERDOLL) false else true

        }
    }
}