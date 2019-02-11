package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

/**
 * Format (ch)dddcc
 * @author -Wooden-
 */
class ExFishingStartCombat(
    private val _activeChar: Creature,
    private val _time: Int,
    private val _hp: Int,
    private val _mode: Int,
    private val _lureType: Int,
    private val _deceptiveMode: Int
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x15)

        writeD(_activeChar.objectId)
        writeD(_time)
        writeD(_hp)
        writeC(_mode) // mode: 0 = resting, 1 = fighting
        writeC(_lureType) // 0 = newbie lure, 1 = normal lure, 2 = night lure
        writeC(_deceptiveMode) // Fish Deceptive Mode: 0 = no, 1 = yes
    }
}