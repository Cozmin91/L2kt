package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q602_ShadowOfLight : Quest(602, "Shadow of Light") {
    init {

        setItemsIds(EYE_OF_DARKNESS)

        addStartNpc(31683) // Eye of Argos
        addTalkId(31683)

        addKillId(21299, 21304)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31683-02.htm", ignoreCase = true)) {
            if (player.level < 68)
                htmltext = "31683-02a.htm"
            else {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
            }
        } else if (event.equals("31683-05.htm", ignoreCase = true)) {
            st.takeItems(EYE_OF_DARKNESS, -1)

            val random = Rnd[100]
            for (element in REWARDS) {
                if (random < element[4]) {
                    st.rewardItems(57, element[1])

                    if (element[0] != 0)
                        st.giveItems(element[0], 3)

                    st.rewardExpAndSp(element[2].toLong(), element[3])
                    break
                }
            }
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "31683-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "31683-03.htm"
                else if (cond == 2)
                    htmltext = "31683-04.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player, npc, "cond", "1") ?: return null

        if (st.dropItems(EYE_OF_DARKNESS, 1, 100, if (npc.npcId == 21299) 450000 else 500000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q602_ShadowOfLight"

        private val EYE_OF_DARKNESS = 7189

        private val REWARDS = arrayOf(
            intArrayOf(6699, 40000, 120000, 20000, 20),
            intArrayOf(6698, 60000, 110000, 15000, 40),
            intArrayOf(6700, 40000, 150000, 10000, 50),
            intArrayOf(0, 100000, 140000, 11250, 100)
        )
    }
}