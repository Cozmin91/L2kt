package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneType
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q636_TruthBeyondTheGate : Quest(636, "The Truth Beyond the Gate") {
    init {

        addStartNpc(ELIYAH)
        addTalkId(ELIYAH, FLAURON)

        addEnterZoneId(100000)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31329-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32010-02.htm", ignoreCase = true)) {
            st.giveItems(VISITOR_MARK, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 73) "31329-01.htm" else "31329-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                ELIYAH -> htmltext = "31329-05.htm"

                FLAURON -> htmltext = if (st.hasQuestItems(VISITOR_MARK)) "32010-03.htm" else "32010-01.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onEnterZone(character: Creature, zone: ZoneType): String? {
        // QuestState already null on enter because quest is finished
        if (character is Player) {
            if (character.actingPlayer!!.destroyItemByItemId("Mark", VISITOR_MARK, 1, character, false))
                character.actingPlayer!!.addItem("Mark", FADED_VISITOR_MARK, 1, character, true)
        }
        return null
    }

    companion object {
        private val qn = "Q636_TruthBeyondTheGate"

        // NPCs
        private val ELIYAH = 31329
        private val FLAURON = 32010

        // Reward
        private val VISITOR_MARK = 8064
        private val FADED_VISITOR_MARK = 8065
    }
}