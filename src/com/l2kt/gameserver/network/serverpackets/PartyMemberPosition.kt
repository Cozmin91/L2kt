package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.model.location.Location
import java.util.*

class PartyMemberPosition(party: Party) : L2GameServerPacket() {
    private var _locations: MutableMap<Int, Location> = HashMap()

    init {
        reuse(party)
    }

    fun reuse(party: Party) {
        _locations.clear()

        for (member in party.members)
            _locations[member.objectId] = Location(member.x, member.y, member.z)
    }

    override fun writeImpl() {
        writeC(0xa7)
        writeD(_locations.size)

        for ((key, value) in _locations) {
            writeD(key)
            writeLoc(value)
        }
    }
}