package com.l2kt.gameserver.network.clientpackets

/**
 * @author zabbix
 */
class GameGuardReply : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        if(client.activeChar == null)
            return

        client.setGameGuardOk(true)
    }
}