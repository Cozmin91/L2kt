package com.l2kt.gameserver.network.clientpackets

class RequestPCCafeCouponUse : L2GameClientPacket() {
    private var _str: String = ""

    override fun readImpl() {
        _str = readS()
    }

    override fun runImpl() {}
}