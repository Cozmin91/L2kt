package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q432_BirthdayPartySong : Quest(432, "Birthday Party Song") {
    init {

        setItemsIds(RED_CRYSTAL)

        addStartNpc(OCTAVIA)
        addTalkId(OCTAVIA)

        addKillId(21103)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31043-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31043-06.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(RED_CRYSTAL) == 50) {
                htmltext = "31043-05.htm"
                st.takeItems(RED_CRYSTAL, -1)
                st.rewardItems(7061, 25)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 31) "31043-00.htm" else "31043-01.htm"

            Quest.STATE_STARTED -> htmltext =
                    if (st.getQuestItemsCount(RED_CRYSTAL) < 50) "31043-03.htm" else "31043-04.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        if (st.dropItems(RED_CRYSTAL, 1, 50, 500000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q432_BirthdayPartySong"

        // NPC
        private val OCTAVIA = 31043

        // Item
        private val RED_CRYSTAL = 7541
    }
}