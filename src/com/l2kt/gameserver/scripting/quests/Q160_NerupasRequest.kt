package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q160_NerupasRequest : Quest(160, "Nerupa's Request") {
    init {

        setItemsIds(SILVERY_SPIDERSILK, UNOREN_RECEIPT, CREAMEES_TICKET, NIGHTSHADE_LEAF)

        addStartNpc(NERUPA)
        addTalkId(NERUPA, UNOREN, CREAMEES, JULIA)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30370-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(SILVERY_SPIDERSILK, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF)
                htmltext = "30370-00.htm"
            else if (player.level < 3)
                htmltext = "30370-02.htm"
            else
                htmltext = "30370-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    NERUPA -> if (cond < 4)
                        htmltext = "30370-05.htm"
                    else if (cond == 4) {
                        htmltext = "30370-06.htm"
                        st.takeItems(NIGHTSHADE_LEAF, 1)
                        st.rewardItems(LESSER_HEALING_POTION, 5)
                        st.rewardExpAndSp(1000, 0)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    UNOREN -> if (cond == 1) {
                        htmltext = "30147-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SILVERY_SPIDERSILK, 1)
                        st.giveItems(UNOREN_RECEIPT, 1)
                    } else if (cond == 2)
                        htmltext = "30147-02.htm"
                    else if (cond == 4)
                        htmltext = "30147-03.htm"

                    CREAMEES -> if (cond == 2) {
                        htmltext = "30149-01.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(UNOREN_RECEIPT, 1)
                        st.giveItems(CREAMEES_TICKET, 1)
                    } else if (cond == 3)
                        htmltext = "30149-02.htm"
                    else if (cond == 4)
                        htmltext = "30149-03.htm"

                    JULIA -> if (cond == 3) {
                        htmltext = "30152-01.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(CREAMEES_TICKET, 1)
                        st.giveItems(NIGHTSHADE_LEAF, 1)
                    } else if (cond == 4)
                        htmltext = "30152-02.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private val qn = "Q160_NerupasRequest"

        // Items
        private val SILVERY_SPIDERSILK = 1026
        private val UNOREN_RECEIPT = 1027
        private val CREAMEES_TICKET = 1028
        private val NIGHTSHADE_LEAF = 1029

        // Reward
        private val LESSER_HEALING_POTION = 1060

        // NPCs
        private val NERUPA = 30370
        private val UNOREN = 30147
        private val CREAMEES = 30149
        private val JULIA = 30152
    }
}