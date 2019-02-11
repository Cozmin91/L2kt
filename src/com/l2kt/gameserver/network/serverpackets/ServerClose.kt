package com.l2kt.gameserver.network.serverpackets

/**
 * @author devScarlet & mrTJO
 */
class ServerClose private constructor() : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x26)
    }

    companion object {
        val STATIC_PACKET = ServerClose()
    }
}