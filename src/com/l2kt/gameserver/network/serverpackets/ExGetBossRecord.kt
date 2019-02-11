package com.l2kt.gameserver.network.serverpackets

/**
 * Format: ch ddd [ddd]
 * @author KenM
 */
class ExGetBossRecord(
    private val _ranking: Int,
    private val _totalPoints: Int,
    private val _bossRecordInfo: Map<Int, Int>?
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x33)
        writeD(_ranking)
        writeD(_totalPoints)
        if (_bossRecordInfo == null) {
            writeD(0x00)
            writeD(0x00)
            writeD(0x00)
            writeD(0x00)
        } else {
            writeD(_bossRecordInfo.size) // list size
            for ((key, value) in _bossRecordInfo) {
                writeD(key)
                writeD(value)
                writeD(0x00) // Total points
            }
        }
    }
}