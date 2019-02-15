package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q367_ElectrifyingRecharge : Quest(367, "Electrifying Recharge!") {
    init {

        setItemsIds(LORAIN_LAMP, TITAN_LAMP_1, TITAN_LAMP_2, TITAN_LAMP_3, TITAN_LAMP_4, TITAN_LAMP_5)

        addStartNpc(LORAIN)
        addTalkId(LORAIN)

        addSpellFinishedId(CATHEROK)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30673-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(LORAIN_LAMP, 1)
        } else if (event.equals("30673-09.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(LORAIN_LAMP, 1)
        } else if (event.equals("30673-08.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_GIVEUP)
            st.exitQuest(true)
        } else if (event.equals("30673-07.htm", ignoreCase = true)) {
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(LORAIN_LAMP, 1)
        }
        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 37) "30673-02.htm" else "30673-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1) {
                    if (st.hasQuestItems(5880)) {
                        htmltext = "30673-05.htm"
                        st.playSound(QuestState.SOUND_ACCEPT)
                        st.takeItems(5880, 1)
                        st.giveItems(LORAIN_LAMP, 1)
                    } else if (st.hasQuestItems(5876)) {
                        htmltext = "30673-04.htm"
                        st.takeItems(5876, 1)
                    } else if (st.hasQuestItems(5877)) {
                        htmltext = "30673-04.htm"
                        st.takeItems(5877, 1)
                    } else if (st.hasQuestItems(5878)) {
                        htmltext = "30673-04.htm"
                        st.takeItems(5878, 1)
                    } else
                        htmltext = "30673-03.htm"
                } else if (cond == 2 && st.hasQuestItems(5879)) {
                    htmltext = "30673-06.htm"
                    st.takeItems(5879, 1)
                    st.rewardItems(REWARD[Rnd[REWARD.size]], 1)
                    st.playSound(QuestState.SOUND_FINISH)
                }
            }
        }
        return htmltext
    }

    override fun onSpellFinished(npc: Npc, player: Player?, skill: L2Skill?): String? {
        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (skill!!.id == 4072) {
            if (st.hasQuestItems(LORAIN_LAMP)) {
                val randomItem = Rnd[5876, 5880]

                st.takeItems(LORAIN_LAMP, 1)
                st.giveItems(randomItem, 1)

                if (randomItem == 5879) {
                    st["cond"] = "2"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q367_ElectrifyingRecharge"

        // NPCs
        private val LORAIN = 30673

        // Item
        private val LORAIN_LAMP = 5875
        private val TITAN_LAMP_1 = 5876
        private val TITAN_LAMP_2 = 5877
        private val TITAN_LAMP_3 = 5878
        private val TITAN_LAMP_4 = 5879
        private val TITAN_LAMP_5 = 5880

        // Reward
        private val REWARD = intArrayOf(4553, 4554, 4555, 4556, 4557, 4558, 4559, 4560, 4561, 4562, 4563, 4564)

        // Mobs
        private val CATHEROK = 21035
    }
}