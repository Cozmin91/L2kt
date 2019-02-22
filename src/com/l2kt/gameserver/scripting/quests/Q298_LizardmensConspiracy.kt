package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q298_LizardmensConspiracy : Quest(298, "Lizardmen's Conspiracy") {
    init {

        setItemsIds(PATROL_REPORT, WHITE_GEM, RED_GEM)

        addStartNpc(PRAGA)
        addTalkId(PRAGA, ROHMER)

        addKillId(20926, 20927, 20922, 20923, 20924)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30333-1.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(PATROL_REPORT, 1)
        } else if (event.equals("30344-1.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(PATROL_REPORT, 1)
        } else if (event.equals("30344-4.htm", ignoreCase = true)) {
            if (st.getInt("cond") == 3) {
                htmltext = "30344-3.htm"
                st.takeItems(WHITE_GEM, -1)
                st.takeItems(RED_GEM, -1)
                st.rewardExpAndSp(0, 42000)
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 25) "30333-0b.htm" else "30333-0a.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                PRAGA -> htmltext = "30333-2.htm"

                ROHMER -> if (st.getInt("cond") == 1)
                    htmltext = if (st.hasQuestItems(PATROL_REPORT)) "30344-0.htm" else "30344-0a.htm"
                else
                    htmltext = "30344-2.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "2") ?: return null

        when (npc.npcId) {
            20922 -> if (st.dropItems(WHITE_GEM, 1, 50, 400000) && st.getQuestItemsCount(RED_GEM) >= 50)
                st["cond"] = "3"

            20923 -> if (st.dropItems(WHITE_GEM, 1, 50, 450000) && st.getQuestItemsCount(RED_GEM) >= 50)
                st["cond"] = "3"

            20924 -> if (st.dropItems(WHITE_GEM, 1, 50, 350000) && st.getQuestItemsCount(RED_GEM) >= 50)
                st["cond"] = "3"

            20926, 20927 -> if (st.dropItems(RED_GEM, 1, 50, 400000) && st.getQuestItemsCount(WHITE_GEM) >= 50)
                st["cond"] = "3"
        }

        return null
    }

    companion object {
        private val qn = "Q298_LizardmensConspiracy"

        // NPCs
        private val PRAGA = 30333
        private val ROHMER = 30344

        // Items
        private val PATROL_REPORT = 7182
        private val WHITE_GEM = 7183
        private val RED_GEM = 7184
    }
}