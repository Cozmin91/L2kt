package com.l2kt.gameserver.network.serverpackets

class CharCreateFail(private val _error: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x1a)
        writeD(_error)
    }

    companion object {
        val REASON_CREATION_FAILED = CharCreateFail(0x00)
        val REASON_TOO_MANY_CHARACTERS = CharCreateFail(0x01)
        val REASON_NAME_ALREADY_EXISTS = CharCreateFail(0x02)
        val REASON_16_ENG_CHARS = CharCreateFail(0x03)
        val REASON_INCORRECT_NAME = CharCreateFail(0x04)
        val REASON_CREATE_NOT_ALLOWED = CharCreateFail(0x05)
        val REASON_CHOOSE_ANOTHER_SVR = CharCreateFail(0x06)
    }
}