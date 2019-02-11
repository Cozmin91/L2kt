package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

class PartySmallWindowUpdate(private val _member: Player) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x52)
        writeD(_member.objectId)
        writeS(_member.name)

        writeD(_member.currentCp.toInt()) // c4
        writeD(_member.maxCp) // c4
        writeD(_member.currentHp.toInt())
        writeD(_member.maxHp)
        writeD(_member.currentMp.toInt())
        writeD(_member.maxMp)

        writeD(_member.level)
        writeD(_member.classId.id)
    }
}