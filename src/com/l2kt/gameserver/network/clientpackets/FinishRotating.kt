package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.StopRotation

class FinishRotating : L2GameClientPacket() {
    private var _degree: Int = 0
    private var _unknown: Int = 0

    override fun readImpl() {
        _degree = readD()
        _unknown = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        activeChar.broadcastPacket(StopRotation(activeChar.objectId, _degree, 0))
    }
}