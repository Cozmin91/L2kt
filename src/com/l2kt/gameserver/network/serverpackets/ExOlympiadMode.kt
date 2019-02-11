package com.l2kt.gameserver.network.serverpackets

/**
 * @author godson
 */
class ExOlympiadMode
/**
 * @param mode (0 = return, 1 = side 1, 2 = side 2, 3 = spectate)
 */
    (private val _mode: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x2b)
        writeC(_mode)
    }
}