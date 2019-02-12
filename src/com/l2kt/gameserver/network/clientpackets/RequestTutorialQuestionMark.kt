package com.l2kt.gameserver.network.clientpackets

class RequestTutorialQuestionMark : L2GameClientPacket() {
    internal var _number: Int = 0

    override fun readImpl() {
        _number = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val qs = player.getQuestState("Tutorial")
        qs?.quest?.notifyEvent("QM$_number", null, player)
    }
}