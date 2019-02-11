package com.l2kt.gameserver.network.serverpackets

/**
 * Format: (ch) dd
 * @author -Wooden-
 */
class PledgeSkillListAdd(private val _id: Int, private val _lvl: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x3a)
        writeD(_id)
        writeD(_lvl)
    }
}