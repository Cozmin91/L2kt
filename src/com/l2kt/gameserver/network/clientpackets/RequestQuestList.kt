package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.QuestList

class RequestQuestList : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        activeChar.sendPacket(QuestList(activeChar))
    }
}