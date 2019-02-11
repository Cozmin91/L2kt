package com.l2kt.gameserver.network.serverpackets

/**
 * @author zabbix Lets drink to code!
 */
class GameGuardQuery : L2GameServerPacket() {

    override fun runImpl() {
        // Lets make user as gg-unauthorized, we will set him as ggOK after reply from client or kick
        client.setGameGuardOk(false)
    }

    public override fun writeImpl() {
        writeC(0xf9)
    }
}