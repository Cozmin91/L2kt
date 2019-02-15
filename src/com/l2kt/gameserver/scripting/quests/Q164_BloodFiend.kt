package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q164_BloodFiend : Quest(164, "Blood Fiend") {
    init {

        setItemsIds(KIRUNAK_SKULL)

        addStartNpc(30149)
        addTalkId(30149)

        addKillId(27021)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30149-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race == ClassRace.DARK_ELF)
                htmltext = "30149-00.htm"
            else if (player.level < 21)
                htmltext = "30149-02.htm"
            else
                htmltext = "30149-03.htm"

            Quest.STATE_STARTED -> if (st.hasQuestItems(KIRUNAK_SKULL)) {
                htmltext = "30149-06.htm"
                st.takeItems(KIRUNAK_SKULL, 1)
                st.rewardItems(57, 42130)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "30149-05.htm"

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        st["cond"] = "2"
        st.playSound(QuestState.SOUND_MIDDLE)
        st.giveItems(KIRUNAK_SKULL, 1)

        return null
    }

    companion object {
        private val qn = "Q164_BloodFiend"

        // Item
        private val KIRUNAK_SKULL = 1044
    }
}