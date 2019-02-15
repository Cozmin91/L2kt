package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q167_DwarvenKinship : Quest(167, "Dwarven Kinship") {
    init {

        setItemsIds(CARLON_LETTER, NORMAN_LETTER)

        addStartNpc(CARLON)
        addTalkId(CARLON, HAPROCK, NORMAN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30350-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(CARLON_LETTER, 1)
        } else if (event.equals("30255-03.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.takeItems(CARLON_LETTER, 1)
            st.giveItems(NORMAN_LETTER, 1)
            st.rewardItems(57, 2000)
        } else if (event.equals("30255-04.htm", ignoreCase = true)) {
            st.takeItems(CARLON_LETTER, 1)
            st.rewardItems(57, 3000)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        } else if (event.equals("30210-02.htm", ignoreCase = true)) {
            st.takeItems(NORMAN_LETTER, 1)
            st.rewardItems(57, 20000)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30350-02.htm" else "30350-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    CARLON -> if (cond == 1)
                        htmltext = "30350-05.htm"

                    HAPROCK -> if (cond == 1)
                        htmltext = "30255-01.htm"
                    else if (cond == 2)
                        htmltext = "30255-05.htm"

                    NORMAN -> if (cond == 2)
                        htmltext = "30210-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private val qn = "Q167_DwarvenKinship"

        // Items
        private val CARLON_LETTER = 1076
        private val NORMAN_LETTER = 1106

        // NPCs
        private val CARLON = 30350
        private val NORMAN = 30210
        private val HAPROCK = 30255
    }
}