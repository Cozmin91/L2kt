package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q001_LettersOfLove : Quest(1, "Letters of Love") {
    init {

        setItemsIds(DARIN_LETTER, ROXXY_KERCHIEF, DARIN_RECEIPT, BAULRO_POTION)

        addStartNpc(DARIN)
        addTalkId(DARIN, ROXXY, BAULRO)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30048-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(DARIN_LETTER, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 2) "30048-01.htm" else "30048-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    DARIN -> if (cond == 1)
                        htmltext = "30048-07.htm"
                    else if (cond == 2) {
                        htmltext = "30048-08.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ROXXY_KERCHIEF, 1)
                        st.giveItems(DARIN_RECEIPT, 1)
                    } else if (cond == 3)
                        htmltext = "30048-09.htm"
                    else if (cond == 4) {
                        htmltext = "30048-10.htm"
                        st.takeItems(BAULRO_POTION, 1)
                        st.giveItems(NECKLACE, 1)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    ROXXY -> if (cond == 1) {
                        htmltext = "30006-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(DARIN_LETTER, 1)
                        st.giveItems(ROXXY_KERCHIEF, 1)
                    } else if (cond == 2)
                        htmltext = "30006-02.htm"
                    else if (cond > 2)
                        htmltext = "30006-03.htm"

                    BAULRO -> if (cond == 3) {
                        htmltext = "30033-01.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(DARIN_RECEIPT, 1)
                        st.giveItems(BAULRO_POTION, 1)
                    } else if (cond == 4)
                        htmltext = "30033-02.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q001_LettersOfLove"

        // Npcs
        private const val DARIN = 30048
        private const val ROXXY = 30006
        private const val BAULRO = 30033

        // Items
        private const val DARIN_LETTER = 687
        private const val ROXXY_KERCHIEF = 688
        private const val DARIN_RECEIPT = 1079
        private const val BAULRO_POTION = 1080

        // Reward
        private const val NECKLACE = 906
    }
}