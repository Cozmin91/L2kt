package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q352_HelpRoodRaiseANewPet : Quest(352, "Help Rood Raise A New Pet!") {
    init {

        setItemsIds(LIENRIK_EGG_1, LIENRIK_EGG_2)

        addStartNpc(31067) // Rood
        addTalkId(31067)

        addKillId(20786, 20787, 21644, 21645)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("31067-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31067-09.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 39) "31067-00.htm" else "31067-01.htm"

            Quest.STATE_STARTED -> {
                val eggs1 = st.getQuestItemsCount(LIENRIK_EGG_1)
                val eggs2 = st.getQuestItemsCount(LIENRIK_EGG_2)

                if (eggs1 + eggs2 == 0)
                    htmltext = "31067-05.htm"
                else {
                    var reward = 2000
                    if (eggs1 > 0 && eggs2 == 0) {
                        htmltext = "31067-06.htm"
                        reward += eggs1 * 34

                        st.takeItems(LIENRIK_EGG_1, -1)
                        st.rewardItems(57, reward)
                    } else if (eggs1 == 0 && eggs2 > 0) {
                        htmltext = "31067-08.htm"
                        reward += eggs2 * 1025

                        st.takeItems(LIENRIK_EGG_2, -1)
                        st.rewardItems(57, reward)
                    } else if (eggs1 > 0 && eggs2 > 0) {
                        htmltext = "31067-08.htm"
                        reward += eggs1 * 34 + eggs2 * 1025 + 2000

                        st.takeItems(LIENRIK_EGG_1, -1)
                        st.takeItems(LIENRIK_EGG_2, -1)
                        st.rewardItems(57, reward)
                    }
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId
        val random = Rnd[100]
        val chance = if (npcId == 20786 || npcId == 21644) 44 else 58

        if (random < chance)
            st.dropItemsAlways(LIENRIK_EGG_1, 1, 0)
        else if (random < chance + 4)
            st.dropItemsAlways(LIENRIK_EGG_2, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q352_HelpRoodRaiseANewPet"

        // Items
        private val LIENRIK_EGG_1 = 5860
        private val LIENRIK_EGG_2 = 5861
    }
}