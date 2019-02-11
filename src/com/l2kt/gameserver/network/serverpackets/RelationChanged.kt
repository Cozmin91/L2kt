package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Playable

/**
 * @author Luca Baldi
 */
class RelationChanged(cha: Playable, private val _relation: Int, autoattackable: Boolean) : L2GameServerPacket() {

    private val _objId: Int = cha.objectId
    private val _autoAttackable: Int = if (autoattackable) 1 else 0
    private val _karma: Int = cha.karma
    private val _pvpFlag: Int = cha.pvpFlag.toInt()

    override fun writeImpl() {
        writeC(0xce)
        writeD(_objId)
        writeD(_relation)
        writeD(_autoAttackable)
        writeD(_karma)
        writeD(_pvpFlag)
    }

    companion object {
        const val RELATION_PVP_FLAG = 0x00002 // pvp ???
        const val RELATION_HAS_KARMA = 0x00004 // karma ???
        const val RELATION_LEADER = 0x00080 // leader
        const val RELATION_INSIEGE = 0x00200 // true if in siege
        const val RELATION_ATTACKER = 0x00400 // true when attacker
        const val RELATION_ALLY = 0x00800 // blue siege icon, cannot have if red
        const val RELATION_ENEMY = 0x01000 // true when red icon, doesn't matter with blue
        const val RELATION_MUTUAL_WAR = 0x08000 // double fist
        const val RELATION_1SIDED_WAR = 0x10000 // single fist
    }
}