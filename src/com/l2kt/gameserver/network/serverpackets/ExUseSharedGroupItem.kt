package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch dddd
 * @author KenM
 */
class ExUseSharedGroupItem(private val _itemId: Int, private val _grpId: Int, remainedTime: Int, totalTime: Int) :
    L2GameServerPacket() {
    private val _remainedTime: Int = remainedTime / 1000
    private val _totalTime: Int = totalTime / 1000

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x49)

        writeD(_itemId)
        writeD(_grpId)
        writeD(_remainedTime)
        writeD(_totalTime)
    }
}