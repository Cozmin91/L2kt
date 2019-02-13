package com.l2kt.gameserver.model

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.model.itemcontainer.Inventory

/**
 * A datatype used to store character selection screen informations.
 */
class CharSelectSlot(val objectId: Int, val name: String) {

    var charId = 0x00030b7a
    var exp: Long = 0
    var sp = 0
    var clanId = 0
    var race = 0
    var classId = 0
    var baseClassId = 0
    var deleteTimer = 0L
    var lastAccess = 0L
    var face = 0
    var hairStyle = 0
    var hairColor = 0
    var sex = 0
    var level = 1
    var maxHp = 0
    var currentHp = 0.0
    var maxMp = 0
    var currentMp = 0.0
    private val _paperdoll: Array<IntArray>
    var karma = 0
    var pkKills = 0
    var pvPKills = 0
    var augmentationId = 0
    var x = 0
    var y = 0
    var z = 0
    var accessLevel = 0

    val enchantEffect: Int
        get() = _paperdoll[Inventory.PAPERDOLL_RHAND][2]

    init {
        _paperdoll = restoreVisibleInventory(objectId)
    }

    fun getPaperdollObjectId(slot: Int): Int {
        return _paperdoll[slot][0]
    }

    fun getPaperdollItemId(slot: Int): Int {
        return _paperdoll[slot][1]
    }

    companion object {
        private val LOGGER = CLogger(CharSelectSlot::class.java.name)

        private const val RESTORE_PAPERDOLLS =
            "SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'"

        private fun restoreVisibleInventory(objectId: Int): Array<IntArray> {
            val paperdoll = Array(0x12) { IntArray(3) }

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(RESTORE_PAPERDOLLS).use { ps ->
                        ps.setInt(1, objectId)

                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                val slot = rs.getInt("loc_data")

                                paperdoll[slot][0] = rs.getInt("object_id")
                                paperdoll[slot][1] = rs.getInt("item_id")
                                paperdoll[slot][2] = rs.getInt("enchant_level")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't restore paperdolls for {}.", e, objectId)
            }

            return paperdoll
        }
    }
}