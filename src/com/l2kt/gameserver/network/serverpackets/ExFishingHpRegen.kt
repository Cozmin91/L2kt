package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

/**
 * Format (ch)dddcccd
 * @author -Wooden-
 */
class ExFishingHpRegen(
    private val _activeChar: Creature,
    private val _time: Int,
    private val _fishHP: Int,
    private val _hpMode: Int,
    private val _goodUse: Int,
    private val _anim: Int,
    private val _penalty: Int,
    private val _hpBarColor: Int
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x16)

        writeD(_activeChar.objectId)
        writeD(_time)
        writeD(_fishHP)
        writeC(_hpMode) // 0 = HP stop, 1 = HP raise
        writeC(_goodUse) // 0 = none, 1 = success, 2 = failed
        writeC(_anim) // Anim: 0 = none, 1 = reeling, 2 = pumping
        writeD(_penalty) // Penalty
        writeC(_hpBarColor) // 0 = normal hp bar, 1 = purple hp bar
    }
}