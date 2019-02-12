package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.StartRotation

class StartRotating : L2GameClientPacket() {
    private var _degree: Int = 0
    private var _side: Int = 0

    override fun readImpl() {
        _degree = readD()
        _side = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        activeChar.broadcastPacket(StartRotation(activeChar.objectId, _degree, _side, 0))
    }
}