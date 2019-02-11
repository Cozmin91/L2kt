package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Door

class DoorInfo(private val _door: Door) : L2GameServerPacket() {
    private val _showHp: Boolean = _door.castle != null && _door.castle.siege.isInProgress

    override fun writeImpl() {
        writeC(0x4c)
        writeD(_door.objectId)
        writeD(_door.doorId)
        writeD(if (_showHp) 1 else 0)
        writeD(1) // ??? (can target)
        writeD(if (_door.isOpened) 0 else 1)
        writeD(_door.maxHp)
        writeD(_door.currentHp.toInt())
        writeD(0) // ??? (show HP)
        writeD(0) // ??? (Damage)
    }
}