package com.l2kt.gameserver.network.serverpackets

class EnchantResult private constructor(private val _result: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x81)
        writeD(_result)
    }

    companion object {
        val SUCCESS = EnchantResult(0)
        val UNK_RESULT_1 = EnchantResult(1)
        val CANCELLED = EnchantResult(2)
        val UNSUCCESS = EnchantResult(3)
        val UNK_RESULT_4 = EnchantResult(4)
    }
}