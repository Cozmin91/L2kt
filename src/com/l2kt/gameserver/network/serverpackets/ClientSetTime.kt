package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.taskmanager.GameTimeTaskManager

class ClientSetTime : L2GameServerPacket() {
    override fun writeImpl() {
        writeC(0xEC)
        writeD(GameTimeTaskManager.gameTime)
        writeD(6)
    }
}