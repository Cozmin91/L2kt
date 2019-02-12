package com.l2kt.gameserver.network.clientpackets

class RequestTutorialClientEvent : L2GameClientPacket() {
    internal var eventId: Int = 0

    override fun readImpl() {
        eventId = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val qs = player.getQuestState("Tutorial")
        qs?.quest?.notifyEvent("CE$eventId", null, player)
    }
}