package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.KeyPacket
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket

class ProtocolVersion : L2GameClientPacket() {
    private var _version: Int = 0

    override fun readImpl() {
        _version = readD()
    }

    override fun runImpl() {
        when (_version) {
            737, 740, 744, 746 -> client.sendPacket(KeyPacket(client.enableCrypt()))

            else -> client.close((null as L2GameServerPacket?)!!)
        }
    }
}