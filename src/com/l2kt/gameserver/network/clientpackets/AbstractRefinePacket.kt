package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.item.type.CrystalType
import com.l2kt.gameserver.model.item.type.WeaponType.FISHINGROD
import com.l2kt.gameserver.model.item.type.WeaponType.NONE
import com.l2kt.gameserver.network.SystemMessageId
import java.util.*

abstract class AbstractRefinePacket : L2GameClientPacket() {

    class LifeStone(val grade: Int, val level: Int) {

        val playerLevel: Int
            get() = LEVELS[level]

        companion object {
            // lifestone level to player level table
            private val LEVELS = intArrayOf(46, 49, 52, 55, 58, 61, 64, 67, 70, 76)
        }
    }

    companion object {
        const val GRADE_NONE = 0
        const val GRADE_MID = 1
        const val GRADE_HIGH = 2
        const val GRADE_TOP = 3

        protected const val GEMSTONE_D = 2130
        protected const val GEMSTONE_C = 2131
        protected const val GEMSTONE_B = 2132

        private val _lifeStones = HashMap<Int, LifeStone>()

        init {
            // itemId, (LS grade, LS level)
            _lifeStones[8723] = LifeStone(GRADE_NONE, 0)
            _lifeStones[8724] = LifeStone(GRADE_NONE, 1)
            _lifeStones[8725] = LifeStone(GRADE_NONE, 2)
            _lifeStones[8726] = LifeStone(GRADE_NONE, 3)
            _lifeStones[8727] = LifeStone(GRADE_NONE, 4)
            _lifeStones[8728] = LifeStone(GRADE_NONE, 5)
            _lifeStones[8729] = LifeStone(GRADE_NONE, 6)
            _lifeStones[8730] = LifeStone(GRADE_NONE, 7)
            _lifeStones[8731] = LifeStone(GRADE_NONE, 8)
            _lifeStones[8732] = LifeStone(GRADE_NONE, 9)

            _lifeStones[8733] = LifeStone(GRADE_MID, 0)
            _lifeStones[8734] = LifeStone(GRADE_MID, 1)
            _lifeStones[8735] = LifeStone(GRADE_MID, 2)
            _lifeStones[8736] = LifeStone(GRADE_MID, 3)
            _lifeStones[8737] = LifeStone(GRADE_MID, 4)
            _lifeStones[8738] = LifeStone(GRADE_MID, 5)
            _lifeStones[8739] = LifeStone(GRADE_MID, 6)
            _lifeStones[8740] = LifeStone(GRADE_MID, 7)
            _lifeStones[8741] = LifeStone(GRADE_MID, 8)
            _lifeStones[8742] = LifeStone(GRADE_MID, 9)

            _lifeStones[8743] = LifeStone(GRADE_HIGH, 0)
            _lifeStones[8744] = LifeStone(GRADE_HIGH, 1)
            _lifeStones[8745] = LifeStone(GRADE_HIGH, 2)
            _lifeStones[8746] = LifeStone(GRADE_HIGH, 3)
            _lifeStones[8747] = LifeStone(GRADE_HIGH, 4)
            _lifeStones[8748] = LifeStone(GRADE_HIGH, 5)
            _lifeStones[8749] = LifeStone(GRADE_HIGH, 6)
            _lifeStones[8750] = LifeStone(GRADE_HIGH, 7)
            _lifeStones[8751] = LifeStone(GRADE_HIGH, 8)
            _lifeStones[8752] = LifeStone(GRADE_HIGH, 9)

            _lifeStones[8753] = LifeStone(GRADE_TOP, 0)
            _lifeStones[8754] = LifeStone(GRADE_TOP, 1)
            _lifeStones[8755] = LifeStone(GRADE_TOP, 2)
            _lifeStones[8756] = LifeStone(GRADE_TOP, 3)
            _lifeStones[8757] = LifeStone(GRADE_TOP, 4)
            _lifeStones[8758] = LifeStone(GRADE_TOP, 5)
            _lifeStones[8759] = LifeStone(GRADE_TOP, 6)
            _lifeStones[8760] = LifeStone(GRADE_TOP, 7)
            _lifeStones[8761] = LifeStone(GRADE_TOP, 8)
            _lifeStones[8762] = LifeStone(GRADE_TOP, 9)
        }

        fun getLifeStone(itemId: Int): LifeStone? {
            return _lifeStones[itemId]
        }

        fun isValid(
            player: Player,
            item: ItemInstance,
            refinerItem: ItemInstance,
            gemStones: ItemInstance
        ): Boolean {
            if (!isValid(player, item, refinerItem))
                return false

            // GemStones must belong to owner
            if (gemStones.ownerId != player.objectId)
                return false
            // .. and located in inventory
            if (gemStones.location != ItemInstance.ItemLocation.INVENTORY)
                return false

            val grade = item.item.crystalType

            // Check for item id
            if (getGemStoneId(grade) != gemStones.itemId)
                return false
            // Count must be greater or equal of required number
            return getGemStoneCount(grade) <= gemStones.count

        }

        /**
         * Checks augmentation process.
         * @param player The target of the check.
         * @param item The item to check.
         * @param refinerItem The augmentation stone.
         * @return true if all checks are successfully passed, false otherwise.
         */
        fun isValid(player: Player, item: ItemInstance, refinerItem: ItemInstance): Boolean {
            if (!isValid(player, item))
                return false

            // Item must belong to owner
            if (refinerItem.ownerId != player.objectId)
                return false

            // Lifestone must be located in inventory
            if (refinerItem.location != ItemInstance.ItemLocation.INVENTORY)
                return false

            val ls = _lifeStones[refinerItem.itemId] ?: return false

            // check for level of the lifestone
            return player.level >= ls.playerLevel

        }

        fun isValid(player: Player, item: ItemInstance): Boolean {
            if (!isValid(player))
                return false

            // Item must belong to owner
            if (item.ownerId != player.objectId)
                return false
            if (item.isAugmented)
                return false
            if (item.isHeroItem)
                return false
            if (item.isShadowItem)
                return false
            if (item.item.crystalType.isLesser(CrystalType.C))
                return false

            // Source item can be equipped or in inventory
            when (item.location) {
                ItemInstance.ItemLocation.INVENTORY, ItemInstance.ItemLocation.PAPERDOLL -> {
                }
                else -> return false
            }

            if (item.item is Weapon) {
                // Rods and fists aren't augmentable
                when ((item.item as Weapon).itemType) {
                    NONE, FISHINGROD -> return false

                    else -> {
                    }
                }
            } else
                return false

            return true
        }

        fun isValid(player: Player): Boolean {
            if (player.isInStoreMode) {
                player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION)
                return false
            }
            if (player.activeTradeList != null) {
                player.sendPacket(SystemMessageId.AUGMENTED_ITEM_CANNOT_BE_DISCARDED)
                return false
            }
            if (player.isDead) {
                player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD)
                return false
            }
            if (player.isParalyzed) {
                player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED)
                return false
            }
            if (player.isFishing) {
                player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING)
                return false
            }
            if (player.isSitting) {
                player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN)
                return false
            }
            if (player.isCursedWeaponEquipped)
                return false
            return !player.isProcessingTransaction

        }

        fun getGemStoneId(itemGrade: CrystalType): Int {
            return when (itemGrade) {
                CrystalType.C, CrystalType.B -> GEMSTONE_D

                CrystalType.A, CrystalType.S -> GEMSTONE_C

                else -> 0
            }
        }

        fun getGemStoneCount(itemGrade: CrystalType): Int {
            return when (itemGrade) {
                CrystalType.C -> 20

                CrystalType.B -> 30

                CrystalType.A -> 20

                CrystalType.S -> 25

                else -> 0
            }
        }
    }
}