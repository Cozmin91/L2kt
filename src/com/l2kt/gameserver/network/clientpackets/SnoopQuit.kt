package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World

/**
 * @author -Wooden-
 */
class SnoopQuit : L2GameClientPacket() {
    private var _snoopID: Int = 0

    override fun readImpl() {
        _snoopID = readD()
    }

    override fun runImpl() {
        client.activeChar ?: return

        World.getPlayer(_snoopID) ?: return

        // No use
    }
}