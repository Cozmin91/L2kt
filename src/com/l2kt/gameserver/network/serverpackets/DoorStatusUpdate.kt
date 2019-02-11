package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Door

class DoorStatusUpdate(private val _door: Door) : L2GameServerPacket() {
    private val _showHp: Boolean = _door.castle != null && _door.castle.siege.isInProgress

    override fun writeImpl() {
        writeC(0x4d)
        writeD(_door.objectId)
        writeD(if (_door.isOpened) 0 else 1)
        writeD(_door.damage)
        writeD(if (_showHp) 1 else 0)
        writeD(_door.doorId)
        writeD(_door.maxHp)
        writeD(_door.currentHp.toInt())
    }
}