package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

/**
 * @author devScarlet
 */
class TitleUpdate(cha: Creature) : L2GameServerPacket() {
    private val _title: String = cha.title
    private val _objectId: Int = cha.objectId

    override fun writeImpl() {
        writeC(0xcc)
        writeD(_objectId)
        writeS(_title)
    }
}