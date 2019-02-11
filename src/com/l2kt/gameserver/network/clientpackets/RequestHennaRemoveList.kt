package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.HennaRemoveList

/**
 * RequestHennaRemoveList
 * @author Tempy
 */
class RequestHennaRemoveList : L2GameClientPacket() {
    private var _unknown: Int = 0

    override fun readImpl() {
        _unknown = readD() // ??
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        activeChar.sendPacket(HennaRemoveList(activeChar))
    }
}