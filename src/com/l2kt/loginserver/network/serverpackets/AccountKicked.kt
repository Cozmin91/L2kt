package com.l2kt.loginserver.network.serverpackets

class AccountKicked(private val _reason: AccountKickedReason) : L2LoginServerPacket() {
    enum class AccountKickedReason(val code: Int) {
        REASON_DATA_STEALER(0x01),
        REASON_GENERIC_VIOLATION(0x08),
        REASON_7_DAYS_SUSPENDED(0x10),
        REASON_PERMANENTLY_BANNED(0x20)
    }

    override fun write() {
        writeC(0x02)
        writeD(_reason.code)
    }
}