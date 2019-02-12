package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.handler.UserCommandHandler

class RequestUserCommand : L2GameClientPacket() {
    private var _command: Int = 0

    override fun readImpl() {
        _command = readD()
    }

    override fun runImpl() {
        client.activeChar ?: return

        val handler = UserCommandHandler.getInstance().getHandler(_command)
        handler?.useUserCommand(_command, client.activeChar)
    }
}