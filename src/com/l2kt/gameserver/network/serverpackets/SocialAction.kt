package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

class SocialAction(cha: Creature, private val _actionId: Int) : L2GameServerPacket() {
    private val _charObjId: Int = cha.objectId

    override fun writeImpl() {
        writeC(0x2d)
        writeD(_charObjId)
        writeD(_actionId)
    }
}