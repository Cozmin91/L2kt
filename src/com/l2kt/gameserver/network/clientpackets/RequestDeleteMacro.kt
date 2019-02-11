package com.l2kt.gameserver.network.clientpackets

class RequestDeleteMacro : L2GameClientPacket() {
    private var _id: Int = 0

    override fun readImpl() {
        _id = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        activeChar.deleteMacro(_id)
    }
}