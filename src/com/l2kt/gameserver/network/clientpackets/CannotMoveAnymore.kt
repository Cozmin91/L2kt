package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.location.SpawnLocation

class CannotMoveAnymore : L2GameClientPacket() {
    private var _x: Int = 0
    private var _y: Int = 0
    private var _z: Int = 0
    private var _heading: Int = 0

    override fun readImpl() {
        _x = readD()
        _y = readD()
        _z = readD()
        _heading = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (player.hasAI())
            player.ai.notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, SpawnLocation(_x, _y, _z, _heading))
    }
}