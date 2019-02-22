package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q623_TheFinestFood : Quest(623, "The Finest Food") {
    init {

        setItemsIds(LEAF_OF_FLAVA, BUFFALO_MEAT, ANTELOPE_HORN)

        addStartNpc(JEREMY)
        addTalkId(JEREMY)

        addKillId(FLAVA, BUFFALO, ANTELOPE)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31521-02.htm", ignoreCase = true)) {
            if (player.level >= 71) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
            } else
                htmltext = "31521-03.htm"
        } else if (event.equals("31521-05.htm", ignoreCase = true)) {
            st.takeItems(LEAF_OF_FLAVA, -1)
            st.takeItems(BUFFALO_MEAT, -1)
            st.takeItems(ANTELOPE_HORN, -1)

            val luck = Rnd[100]
            if (luck < 11) {
                st.rewardItems(57, 25000)
                st.giveItems(6849, 1)
            } else if (luck < 23) {
                st.rewardItems(57, 65000)
                st.giveItems(6847, 1)
            } else if (luck < 33) {
                st.rewardItems(57, 25000)
                st.giveItems(6851, 1)
            } else {
                st.rewardItems(57, 73000)
                st.rewardExpAndSp(230000, 18250)
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
            Quest.STATE_CREATED -> htmltext = "31521-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "31521-06.htm"
                else if (cond == 2) {
                    if (st.getQuestItemsCount(LEAF_OF_FLAVA) >= 100 && st.getQuestItemsCount(BUFFALO_MEAT) >= 100 && st.getQuestItemsCount(
                            ANTELOPE_HORN
                        ) >= 100
                    )
                        htmltext = "31521-04.htm"
                    else
                        htmltext = "31521-07.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        when (npc.npcId) {
            FLAVA -> if (st.dropItemsAlways(
                    LEAF_OF_FLAVA,
                    1,
                    100
                ) && st.getQuestItemsCount(BUFFALO_MEAT) >= 100 && st.getQuestItemsCount(ANTELOPE_HORN) >= 100
            )
                st["cond"] = "2"

            BUFFALO -> if (st.dropItemsAlways(
                    BUFFALO_MEAT,
                    1,
                    100
                ) && st.getQuestItemsCount(LEAF_OF_FLAVA) >= 100 && st.getQuestItemsCount(ANTELOPE_HORN) >= 100
            )
                st["cond"] = "2"

            ANTELOPE -> if (st.dropItemsAlways(
                    ANTELOPE_HORN,
                    1,
                    100
                ) && st.getQuestItemsCount(LEAF_OF_FLAVA) >= 100 && st.getQuestItemsCount(BUFFALO_MEAT) >= 100
            )
                st["cond"] = "2"
        }

        return null
    }

    companion object {
        private val qn = "Q623_TheFinestFood"

        // Items
        private val LEAF_OF_FLAVA = 7199
        private val BUFFALO_MEAT = 7200
        private val ANTELOPE_HORN = 7201

        // NPC
        private val JEREMY = 31521

        // Monsters
        private val FLAVA = 21316
        private val BUFFALO = 21315
        private val ANTELOPE = 21318
    }
}