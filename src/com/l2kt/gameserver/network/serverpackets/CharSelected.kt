package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager

class CharSelected(private val _activeChar: Player, private val _sessionId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x15)

        writeS(_activeChar.name)
        writeD(_activeChar.objectId)
        writeS(_activeChar.title)
        writeD(_sessionId)
        writeD(_activeChar.clanId)

        writeD(0x00) // unknown

        writeD(_activeChar.appearance.sex.ordinal)
        writeD(_activeChar.race.ordinal)
        writeD(_activeChar.classId.id)

        writeD(0x01)

        writeD(_activeChar.x)
        writeD(_activeChar.y)
        writeD(_activeChar.z)
        writeF(_activeChar.currentHp)
        writeF(_activeChar.currentMp)
        writeD(_activeChar.sp)
        writeQ(_activeChar.exp)
        writeD(_activeChar.level)
        writeD(_activeChar.karma)
        writeD(_activeChar.pkKills)
        writeD(_activeChar.int)
        writeD(_activeChar.str)
        writeD(_activeChar.con)
        writeD(_activeChar.men)
        writeD(_activeChar.dex)
        writeD(_activeChar.wit)

        for (i in 0..29) {
            writeD(0x00)
        }

        writeD(0x00) // c3 work
        writeD(0x00) // c3 work

        writeD(GameTimeTaskManager.gameTime)

        writeD(0x00) // c3

        writeD(_activeChar.classId.id)

        writeD(0x00) // c3 InspectorBin
        writeD(0x00) // c3
        writeD(0x00) // c3
        writeD(0x00) // c3
    }
}