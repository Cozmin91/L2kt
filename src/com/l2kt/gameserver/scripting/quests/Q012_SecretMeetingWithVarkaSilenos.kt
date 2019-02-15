package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q012_SecretMeetingWithVarkaSilenos : Quest(12, "Secret Meeting With Varka Silenos") {
    init {

        setItemsIds(MUNITIONS_BOX)

        addStartNpc(CADMON)
        addTalkId(CADMON, HELMUT, NARAN_ASHANUK)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31296-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31258-02.htm", ignoreCase = true)) {
            st.giveItems(MUNITIONS_BOX, 1)
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31378-02.htm", ignoreCase = true)) {
            st.takeItems(MUNITIONS_BOX, 1)
            st.rewardExpAndSp(79761, 0)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 74) "31296-02.htm" else "31296-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    CADMON -> if (cond == 1)
                        htmltext = "31296-04.htm"

                    HELMUT -> if (cond == 1)
                        htmltext = "31258-01.htm"
                    else if (cond == 2)
                        htmltext = "31258-03.htm"

                    NARAN_ASHANUK -> if (cond == 2)
                        htmltext = "31378-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q012_SecretMeetingWithVarkaSilenos"

        // NPCs
        private const val CADMON = 31296
        private const val HELMUT = 31258
        private const val NARAN_ASHANUK = 31378

        // Items
        private const val MUNITIONS_BOX = 7232
    }
}