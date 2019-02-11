package com.l2kt.gameserver.network.serverpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.location.Location

/**
 * Format (ch)ddddd
 * @author -Wooden-
 */
class ExFishingStart(
    private val _activeChar: Creature,
    private val _fishType: Int,
    private val _loc: Location,
    private val _isNightLure: Boolean
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x13)
        writeD(_activeChar.objectId)
        writeD(_fishType) // fish type
        writeD(_loc.x) // x position
        writeD(_loc.y) // y position
        writeD(_loc.z) // z position
        writeC(if (_isNightLure) 0x01 else 0x00) // night lure
        writeC(if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED) 0x01 else 0x00) // show fish rank result button
    }
}