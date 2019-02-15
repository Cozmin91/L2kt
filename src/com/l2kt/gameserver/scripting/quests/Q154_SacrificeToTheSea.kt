package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q154_SacrificeToTheSea : Quest(154, "Sacrifice to the Sea") {
    init {

        setItemsIds(FOX_FUR, FOX_FUR_YARN, MAIDEN_DOLL)

        addStartNpc(ROCKSWELL)
        addTalkId(ROCKSWELL, CRISTEL, ROLFE)

        addKillId(20481, 20544, 20545) // Following Keltirs can be found near Talking Island.
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30312-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 2) "30312-02.htm" else "30312-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ROCKSWELL -> if (cond == 1)
                        htmltext = "30312-05.htm"
                    else if (cond == 2)
                        htmltext = "30312-08.htm"
                    else if (cond == 3)
                        htmltext = "30312-06.htm"
                    else if (cond == 4) {
                        htmltext = "30312-07.htm"
                        st.takeItems(MAIDEN_DOLL, -1)
                        st.giveItems(EARING, 1)
                        st.rewardExpAndSp(100, 0)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    CRISTEL -> if (cond == 1)
                        htmltext = if (st.hasQuestItems(FOX_FUR)) "30051-01.htm" else "30051-01a.htm"
                    else if (cond == 2) {
                        htmltext = "30051-02.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(FOX_FUR, -1)
                        st.giveItems(FOX_FUR_YARN, 1)
                    } else if (cond == 3)
                        htmltext = "30051-03.htm"
                    else if (cond == 4)
                        htmltext = "30051-04.htm"

                    ROLFE -> if (cond < 3)
                        htmltext = "30055-03.htm"
                    else if (cond == 3) {
                        htmltext = "30055-01.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(FOX_FUR_YARN, 1)
                        st.giveItems(MAIDEN_DOLL, 1)
                    } else if (cond == 4)
                        htmltext = "30055-02.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(FOX_FUR, 1, 10, 400000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q154_SacrificeToTheSea"

        // NPCs
        private val ROCKSWELL = 30312
        private val CRISTEL = 30051
        private val ROLFE = 30055

        // Items
        private val FOX_FUR = 1032
        private val FOX_FUR_YARN = 1033
        private val MAIDEN_DOLL = 1034

        // Reward
        private val EARING = 113
    }
}