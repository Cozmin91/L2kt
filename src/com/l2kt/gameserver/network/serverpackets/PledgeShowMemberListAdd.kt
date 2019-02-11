package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

class PledgeShowMemberListAdd(player: Player) : L2GameServerPacket() {
    private val _name: String = player.name
    private val _lvl: Int = player.level
    private val _classId: Int = player.classId.id
    private val _isOnline: Int = if (player.isOnline) player.objectId else 0
    private val _pledgeType: Int = player.pledgeType
    private val _race: Int = player.race.ordinal
    private val _sex: Int = player.appearance.sex.ordinal

    override fun writeImpl() {
        writeC(0x55)
        writeS(_name)
        writeD(_lvl)
        writeD(_classId)
        writeD(_sex)
        writeD(_race)
        writeD(_isOnline)
        writeD(_pledgeType)
    }
}