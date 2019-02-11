package com.l2kt.gameserver.network.clientpackets

/**
 * format ch
 * @author -Wooden-
 */
class RequestOlympiadObserverEnd : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (activeChar.isInObserverMode)
            activeChar.leaveOlympiadObserverMode()
    }
}