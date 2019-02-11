package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest

class QuestList(private val _activeChar: Player) : L2GameServerPacket() {
    private val _quests: List<Quest> = _activeChar.getAllQuests(true)

    override fun writeImpl() {
        writeC(0x80)
        writeH(_quests.size)
        for (q in _quests) {
            writeD(q.questId)
            val qs = _activeChar.getQuestState(q.name)
            if (qs == null) {
                writeD(0)
                continue
            }

            val states = qs.getInt("__compltdStateFlags")
            if (states != 0)
                writeD(states)
            else
                writeD(qs.getInt("cond"))
        }
    }
}