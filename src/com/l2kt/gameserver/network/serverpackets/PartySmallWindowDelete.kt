package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

class PartySmallWindowDelete(private val _member: Player) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x51)
        writeD(_member.objectId)
        writeS(_member.name)
    }
}