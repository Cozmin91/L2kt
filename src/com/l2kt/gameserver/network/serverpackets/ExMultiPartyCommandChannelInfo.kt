package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.group.CommandChannel

class ExMultiPartyCommandChannelInfo(private val _channel: CommandChannel?) : L2GameServerPacket() {

    override fun writeImpl() {
        if (_channel == null)
            return

        writeC(0xfe)
        writeH(0x30)

        writeS(_channel.leader.name)
        writeD(0) // Channel loot
        writeD(_channel.membersCount)

        writeD(_channel.parties.size)
        for (party in _channel.parties) {
            writeS(party.leader.name)
            writeD(party.leaderObjectId)
            writeD(party.membersCount)
        }
    }
}