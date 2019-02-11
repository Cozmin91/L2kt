package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.ExSendManorList

/**
 * Format: ch
 * @author l3x
 */
class RequestManorList : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        player.sendPacket(ExSendManorList.STATIC_PACKET)
    }
}