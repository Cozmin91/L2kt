package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q037_MakeFormalWear : Quest(37, "Make Formal Wear") {
    init {

        setItemsIds(SIGNET_RING, ICE_WINE, BOX_OF_COOKIES)

        addStartNpc(ALEXIS)
        addTalkId(ALEXIS, LEIKAR, JEREMY, MIST)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30842-1.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31520-1.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(SIGNET_RING, 1)
        } else if (event.equals("31521-1.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SIGNET_RING, 1)
            st.giveItems(ICE_WINE, 1)
        } else if (event.equals("31627-1.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ICE_WINE, 1)
        } else if (event.equals("31521-3.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(BOX_OF_COOKIES, 1)
        } else if (event.equals("31520-3.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BOX_OF_COOKIES, 1)
        } else if (event.equals("31520-5.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(JEWEL_BOX, 1)
            st.takeItems(MYSTERIOUS_CLOTH, 1)
            st.takeItems(SEWING_KIT, 1)
        } else if (event.equals("31520-7.htm", ignoreCase = true)) {
            st.takeItems(DRESS_SHOES_BOX, 1)
            st.giveItems(FORMAL_WEAR, 1)
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 60) "30842-0a.htm" else "30842-0.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ALEXIS -> if (cond == 1)
                        htmltext = "30842-2.htm"

                    LEIKAR -> if (cond == 1)
                        htmltext = "31520-0.htm"
                    else if (cond == 2)
                        htmltext = "31520-1a.htm"
                    else if (cond == 5 || cond == 6) {
                        if (st.hasQuestItems(MYSTERIOUS_CLOTH, JEWEL_BOX, SEWING_KIT))
                            htmltext = "31520-4.htm"
                        else if (st.hasQuestItems(BOX_OF_COOKIES))
                            htmltext = "31520-2.htm"
                        else
                            htmltext = "31520-3a.htm"
                    } else if (cond == 7)
                        htmltext = if (st.hasQuestItems(DRESS_SHOES_BOX)) "31520-6.htm" else "31520-5a.htm"

                    JEREMY -> if (st.hasQuestItems(SIGNET_RING))
                        htmltext = "31521-0.htm"
                    else if (cond == 3)
                        htmltext = "31521-1a.htm"
                    else if (cond == 4)
                        htmltext = "31521-2.htm"
                    else if (cond > 4)
                        htmltext = "31521-3a.htm"

                    MIST -> if (cond == 3)
                        htmltext = "31627-0.htm"
                    else if (cond > 3)
                        htmltext = "31627-2.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q037_MakeFormalWear"

        // NPCs
        private const val ALEXIS = 30842
        private const val LEIKAR = 31520
        private const val JEREMY = 31521
        private const val MIST = 31627

        // Items
        private const val MYSTERIOUS_CLOTH = 7076
        private const val JEWEL_BOX = 7077
        private const val SEWING_KIT = 7078
        private const val DRESS_SHOES_BOX = 7113
        private const val SIGNET_RING = 7164
        private const val ICE_WINE = 7160
        private const val BOX_OF_COOKIES = 7159

        // Reward
        private const val FORMAL_WEAR = 6408
    }
}