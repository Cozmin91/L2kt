package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.group.Party

class PartySmallWindowAdd(private val _member: Player, party: Party) : L2GameServerPacket() {
    private val _leaderId: Int = party.leaderObjectId
    private val _distribution: Int = party.lootRule.ordinal

    override fun writeImpl() {
        writeC(0x4f)
        writeD(_leaderId)
        writeD(_distribution)
        writeD(_member.objectId)
        writeS(_member.name)
        writeD(_member.currentCp.toInt())
        writeD(_member.maxCp)
        writeD(_member.currentHp.toInt())
        writeD(_member.maxHp)
        writeD(_member.currentMp.toInt())
        writeD(_member.maxMp)
        writeD(_member.level)
        writeD(_member.classId.id)
        writeD(0)// writeD(0x01); ??
        writeD(0)
    }
}