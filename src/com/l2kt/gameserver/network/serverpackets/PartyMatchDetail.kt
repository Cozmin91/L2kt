package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.partymatching.PartyMatchRoom

/**
 * @author Gnacik
 */
class PartyMatchDetail(private val _room: PartyMatchRoom) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x97)
        writeD(_room.id) // Room ID
        writeD(_room.maxMembers) // Max Members
        writeD(_room.minLvl) // Level Min
        writeD(_room.maxLvl) // Level Max
        writeD(_room.lootType) // Loot Type
        writeD(_room.location) // Room Location
        writeS(_room.title) // Room title
    }
}