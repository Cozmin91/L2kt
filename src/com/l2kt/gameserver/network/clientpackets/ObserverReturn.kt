package com.l2kt.gameserver.network.clientpackets

class ObserverReturn : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (activeChar.isInObserverMode)
            activeChar.leaveObserverMode()
    }
}