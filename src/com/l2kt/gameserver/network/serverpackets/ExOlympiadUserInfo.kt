package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

/**
 * @author godson
 */
class ExOlympiadUserInfo(player: Player) : L2GameServerPacket() {
    private val _side: Int = player.olympiadSide
    private val _objectId: Int = player.objectId
    private val _name: String = player.name
    private val _classId: Int = player.classId.id
    private val _curHp: Int = player.currentHp.toInt()
    private val _maxHp: Int = player.maxHp
    private val _curCp: Int = player.currentCp.toInt()
    private val _maxCp: Int = player.maxCp

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x29)
        writeC(_side)
        writeD(_objectId)
        writeS(_name)
        writeD(_classId)
        writeD(_curHp)
        writeD(_maxHp)
        writeD(_curCp)
        writeD(_maxCp)
    }
}