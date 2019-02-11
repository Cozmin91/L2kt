package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.group.Party

class PartySmallWindowAll(private val _exclude: Player, private val _party: Party) : L2GameServerPacket() {
    private val _dist: Int = _party.lootRule.ordinal
    private val _leaderObjectId: Int = _party.leaderObjectId

    override fun writeImpl() {
        writeC(0x4e)
        writeD(_leaderObjectId)
        writeD(_dist)
        writeD(_party.membersCount - 1)

        for (member in _party.members) {
            if (member == _exclude)
                continue

            writeD(member.objectId)
            writeS(member.name)
            writeD(member.currentCp.toInt())
            writeD(member.maxCp)
            writeD(member.currentHp.toInt())
            writeD(member.maxHp)
            writeD(member.currentMp.toInt())
            writeD(member.maxMp)
            writeD(member.level)
            writeD(member.classId.id)
            writeD(0)// writeD(0x01); ??
            writeD(member.race.ordinal)
        }
    }
}