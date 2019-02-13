package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.pledge.ClanMember

class PledgeShowMemberListUpdate : L2GameServerPacket {
    private val _pledgeType: Int
    private val _hasSponsor: Int
    private val _name: String
    private val _level: Int
    private val _classId: Int
    private val _isOnline: Int
    private val _race: Int
    private val _sex: Int

    constructor(player: Player) {
        _pledgeType = player.pledgeType
        _hasSponsor = if (player.sponsor != 0 || player.apprentice != 0) 1 else 0
        _name = player.name
        _level = player.level
        _classId = player.classId.id
        _race = player.race.ordinal
        _sex = player.appearance.sex.ordinal
        _isOnline = if (player.isOnline) player.objectId else 0
    }

    constructor(member: ClanMember) {
        _name = member.name
        _level = member.level
        _classId = member.classId
        _isOnline = if (member.isOnline) member.objectId else 0
        _pledgeType = member.pledgeType
        _hasSponsor = if (member.sponsor != 0 || member.apprentice != 0) 1 else 0

        if (_isOnline != 0) {
            _race = member.playerInstance!!.race.ordinal
            _sex = member.playerInstance!!.appearance.sex.ordinal
        } else {
            _sex = 0
            _race = 0
        }
    }

    override fun writeImpl() {
        writeC(0x54)
        writeS(_name)
        writeD(_level)
        writeD(_classId)
        writeD(_sex)
        writeD(_race)
        writeD(_isOnline)
        writeD(_pledgeType)
        writeD(_hasSponsor)
    }
}