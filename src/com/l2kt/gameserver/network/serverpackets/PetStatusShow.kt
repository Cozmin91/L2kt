package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Summon

/**
 * @author Yme
 */
class PetStatusShow(summon: Summon) : L2GameServerPacket() {
    private val _summonType: Int = summon.summonType

    override fun writeImpl() {
        writeC(0xB0)
        writeD(_summonType)
    }
}