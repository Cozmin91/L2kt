package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

/**
 * Sh (dd) h (dddd)
 * @author Tempy
 */
class GMViewQuestList(private val _activeChar: Player) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x93)
        writeS(_activeChar.name)

        val quests = _activeChar.getAllQuests(true)

        writeH(quests.size)
        for (q in quests) {
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