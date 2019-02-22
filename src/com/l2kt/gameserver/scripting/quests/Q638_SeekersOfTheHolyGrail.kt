package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q638_SeekersOfTheHolyGrail : Quest(638, "Seekers of the Holy Grail") {
    init {

        setItemsIds(PAGAN_TOTEM)

        addStartNpc(INNOCENTIN)
        addTalkId(INNOCENTIN)

        for (i in 22138..22174)
            addKillId(i)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("31328-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31328-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 73) "31328-00.htm" else "31328-01.htm"

            Quest.STATE_STARTED -> if (st.getQuestItemsCount(PAGAN_TOTEM) >= 2000) {
                htmltext = "31328-03.htm"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(PAGAN_TOTEM, 2000)

                val chance = Rnd[3]
                if (chance == 0)
                    st.rewardItems(959, 1)
                else if (chance == 1)
                    st.rewardItems(960, 1)
                else
                    st.rewardItems(57, 3576000)
            } else
                htmltext = "31328-04.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItemsAlways(PAGAN_TOTEM, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q638_SeekersOfTheHolyGrail"

        // NPC
        private val INNOCENTIN = 31328

        // Item
        private val PAGAN_TOTEM = 8068
    }
}