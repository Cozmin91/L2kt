package com.l2kt.gameserver.network.clientpackets

class RequestTutorialLinkHtml : L2GameClientPacket() {
    internal var _bypass: String = ""

    override fun readImpl() {
        _bypass = readS()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val qs = player.getQuestState("Tutorial")
        qs?.quest?.notifyEvent(_bypass, null, player)
    }
}