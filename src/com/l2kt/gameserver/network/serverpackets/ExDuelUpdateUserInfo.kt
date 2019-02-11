package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

/**
 * Format: ch Sddddddddd
 * @author KenM
 */
class ExDuelUpdateUserInfo(private val _activeChar: Player) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x4f)
        writeS(_activeChar.name)
        writeD(_activeChar.objectId)
        writeD(_activeChar.classId.id)
        writeD(_activeChar.level)
        writeD(_activeChar.currentHp.toInt())
        writeD(_activeChar.maxHp)
        writeD(_activeChar.currentMp.toInt())
        writeD(_activeChar.maxMp)
        writeD(_activeChar.currentCp.toInt())
        writeD(_activeChar.maxCp)
    }
}