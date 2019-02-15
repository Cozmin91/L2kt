package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q004_LongliveThePaagrioLord : Quest(4, "Long live the Pa'agrio Lord!") {
    init {
        NPC_GIFTS[30585] = 1542
        NPC_GIFTS[30566] = 1541
        NPC_GIFTS[30562] = 1543
        NPC_GIFTS[30560] = 1544
        NPC_GIFTS[30559] = 1545
        NPC_GIFTS[30587] = 1546
    }

    init {

        setItemsIds(1541, 1542, 1543, 1544, 1545, 1546)

        addStartNpc(30578) // Nakusin
        addTalkId(30578, 30585, 30566, 30562, 30560, 30559, 30587)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30578-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ORC)
                htmltext = "30578-00.htm"
            else if (player.level < 2)
                htmltext = "30578-01.htm"
            else
                htmltext = "30578-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                val npcId = npc.npcId

                if (npcId == 30578) {
                    if (cond == 1)
                        htmltext = "30578-04.htm"
                    else if (cond == 2) {
                        htmltext = "30578-06.htm"
                        st.giveItems(4, 1)
                        for (item in NPC_GIFTS.values)
                            st.takeItems(item, -1)

                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }
                } else {
                    val i = NPC_GIFTS[npcId]!!
                    if (st.hasQuestItems(i))
                        htmltext = npcId.toString() + "-02.htm"
                    else {
                        st.giveItems(i, 1)
                        htmltext = npcId.toString() + "-01.htm"

                        var count = 0
                        for (item in NPC_GIFTS.values)
                            count += st.getQuestItemsCount(item)

                        if (count == 6) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    }
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q004_LongliveThePaagrioLord"

        private val NPC_GIFTS = HashMap<Int, Int>()
    }
}