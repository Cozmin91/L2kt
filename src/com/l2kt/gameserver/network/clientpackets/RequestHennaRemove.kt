package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.SystemMessageId

/**
 * format cd
 */
class RequestHennaRemove : L2GameClientPacket() {
    private var _symbolId: Int = 0

    override fun readImpl() {
        _symbolId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        for (i in 1..3) {
            val henna = activeChar.getHenna(i)
            if (henna != null && henna.symbolId == _symbolId) {
                if (activeChar.adena >= henna.price / 5) {
                    activeChar.removeHenna(i)
                    break
                }
                activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA)
            }
        }
    }
}