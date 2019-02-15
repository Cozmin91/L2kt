package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q326_VanquishRemnants : Quest(326, "Vanquish Remnants") {
    init {

        setItemsIds(RED_CROSS_BADGE, BLUE_CROSS_BADGE, BLACK_CROSS_BADGE)

        addStartNpc(30435) // Leopold
        addTalkId(30435)

        addKillId(20053, 20437, 20058, 20436, 20061, 20439, 20063, 20066, 20438)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30435-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30435-07.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 21) "30435-01.htm" else "30435-02.htm"

            Quest.STATE_STARTED -> {
                val redBadges = st.getQuestItemsCount(RED_CROSS_BADGE)
                val blueBadges = st.getQuestItemsCount(BLUE_CROSS_BADGE)
                val blackBadges = st.getQuestItemsCount(BLACK_CROSS_BADGE)

                val badgesSum = redBadges + blueBadges + blackBadges

                if (badgesSum > 0) {
                    st.takeItems(RED_CROSS_BADGE, -1)
                    st.takeItems(BLUE_CROSS_BADGE, -1)
                    st.takeItems(BLACK_CROSS_BADGE, -1)
                    st.rewardItems(
                        57,
                        redBadges * 46 + blueBadges * 52 + blackBadges * 58 + if (badgesSum >= 10) 4320 else 0
                    )

                    if (badgesSum >= 100) {
                        if (!st.hasQuestItems(BLACK_LION_MARK)) {
                            htmltext = "30435-06.htm"
                            st.giveItems(BLACK_LION_MARK, 1)
                            st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30435-09.htm"
                    } else
                        htmltext = "30435-05.htm"
                } else
                    htmltext = "30435-04.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20053, 20437, 20058 -> st.dropItems(RED_CROSS_BADGE, 1, 0, 330000)

            20436, 20061, 20439, 20063 -> st.dropItems(BLUE_CROSS_BADGE, 1, 0, 160000)

            20066, 20438 -> st.dropItems(BLACK_CROSS_BADGE, 1, 0, 120000)
        }

        return null
    }

    companion object {
        private val qn = "Q326_VanquishRemnants"

        // Items
        private val RED_CROSS_BADGE = 1359
        private val BLUE_CROSS_BADGE = 1360
        private val BLACK_CROSS_BADGE = 1361

        // Reward
        private val BLACK_LION_MARK = 1369
    }
}