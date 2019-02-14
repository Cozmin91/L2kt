package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.xml.ScriptData

class RequestQuestAbort : L2GameClientPacket() {
    private var _questId: Int = 0

    override fun readImpl() {
        _questId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val qe = ScriptData.getQuest(_questId) ?: return

        val qs = activeChar.getQuestState(qe.name)
        qs?.exitQuest(true)
    }
}