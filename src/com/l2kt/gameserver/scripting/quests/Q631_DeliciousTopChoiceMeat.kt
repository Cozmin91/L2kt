package com.l2kt.gameserver.scripting.quests

import java.util.HashMap

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q631_DeliciousTopChoiceMeat : Quest(631, "Delicious Top Choice Meat") {
    init {
        CHANCES[21460] = 601000
        CHANCES[21461] = 480000
        CHANCES[21462] = 447000
        CHANCES[21463] = 808000
        CHANCES[21464] = 447000
        CHANCES[21465] = 808000
        CHANCES[21466] = 447000
        CHANCES[21467] = 808000
        CHANCES[21479] = 477000
        CHANCES[21480] = 863000
        CHANCES[21481] = 477000
        CHANCES[21482] = 863000
        CHANCES[21483] = 477000
        CHANCES[21484] = 863000
        CHANCES[21485] = 477000
        CHANCES[21486] = 863000
        CHANCES[21498] = 509000
        CHANCES[21499] = 920000
        CHANCES[21500] = 509000
        CHANCES[21501] = 920000
        CHANCES[21502] = 509000
        CHANCES[21503] = 920000
        CHANCES[21504] = 509000
        CHANCES[21505] = 920000
    }

    init {

        setItemsIds(TOP_QUALITY_MEAT)

        addStartNpc(TUNATUN)
        addTalkId(TUNATUN)

        for (npcId in CHANCES.keys)
            addKillId(npcId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31537-03.htm", ignoreCase = true)) {
            if (player.level >= 65) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
            } else {
                htmltext = "31537-02.htm"
                st.exitQuest(true)
            }
        } else if (StringUtil.isDigit(event)) {
            if (st.getQuestItemsCount(TOP_QUALITY_MEAT) >= 120) {
                htmltext = "31537-06.htm"
                st.takeItems(TOP_QUALITY_MEAT, -1)

                val reward = REWARDS[Integer.parseInt(event)]
                st.rewardItems(reward[0], reward[1])

                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else {
                st["cond"] = "1"
                htmltext = "31537-07.htm"
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "31537-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "31537-03a.htm"
                else if (cond == 2) {
                    if (st.getQuestItemsCount(TOP_QUALITY_MEAT) >= 120)
                        htmltext = "31537-04.htm"
                    else {
                        st["cond"] = "1"
                        htmltext = "31537-03a.htm"
                    }
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        if (st.dropItems(TOP_QUALITY_MEAT, 1, 120, CHANCES[npc.npcId] ?: 0))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q631_DeliciousTopChoiceMeat"

        // NPC
        private val TUNATUN = 31537

        // Item
        private val TOP_QUALITY_MEAT = 7546

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()

        // Rewards
        private val REWARDS = arrayOf(
            intArrayOf(4039, 15),
            intArrayOf(4043, 15),
            intArrayOf(4044, 15),
            intArrayOf(4040, 10),
            intArrayOf(4042, 10),
            intArrayOf(4041, 5)
        )
    }
}