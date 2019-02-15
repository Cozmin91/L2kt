package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q153_DeliverGoods : Quest(153, "Deliver Goods") {
    init {

        setItemsIds(
            DELIVERY_LIST,
            HEAVY_WOOD_BOX,
            CLOTH_BUNDLE,
            CLAY_POT,
            JACKSON_RECEIPT,
            SILVIA_RECEIPT,
            RANT_RECEIPT
        )

        addStartNpc(ARNOLD)
        addTalkId(JACKSON, SILVIA, ARNOLD, RANT)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30041-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(DELIVERY_LIST, 1)
            st.giveItems(CLAY_POT, 1)
            st.giveItems(CLOTH_BUNDLE, 1)
            st.giveItems(HEAVY_WOOD_BOX, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 2) "30041-00.htm" else "30041-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                ARNOLD -> if (st.getInt("cond") == 1)
                    htmltext = "30041-03.htm"
                else if (st.getInt("cond") == 2) {
                    htmltext = "30041-04.htm"
                    st.takeItems(DELIVERY_LIST, 1)
                    st.takeItems(JACKSON_RECEIPT, 1)
                    st.takeItems(SILVIA_RECEIPT, 1)
                    st.takeItems(RANT_RECEIPT, 1)
                    st.giveItems(RING_OF_KNOWLEDGE, 1)
                    st.giveItems(RING_OF_KNOWLEDGE, 1)
                    st.rewardExpAndSp(600, 0)
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(false)
                }

                JACKSON -> if (st.hasQuestItems(HEAVY_WOOD_BOX)) {
                    htmltext = "30002-01.htm"
                    st.takeItems(HEAVY_WOOD_BOX, 1)
                    st.giveItems(JACKSON_RECEIPT, 1)

                    if (st.hasQuestItems(SILVIA_RECEIPT, RANT_RECEIPT)) {
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else
                        st.playSound(QuestState.SOUND_ITEMGET)
                } else
                    htmltext = "30002-02.htm"

                SILVIA -> if (st.hasQuestItems(CLOTH_BUNDLE)) {
                    htmltext = "30003-01.htm"
                    st.takeItems(CLOTH_BUNDLE, 1)
                    st.giveItems(SILVIA_RECEIPT, 1)
                    st.giveItems(SOULSHOT_NO_GRADE, 3)

                    if (st.hasQuestItems(JACKSON_RECEIPT, RANT_RECEIPT)) {
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else
                        st.playSound(QuestState.SOUND_ITEMGET)
                } else
                    htmltext = "30003-02.htm"

                RANT -> if (st.hasQuestItems(CLAY_POT)) {
                    htmltext = "30054-01.htm"
                    st.takeItems(CLAY_POT, 1)
                    st.giveItems(RANT_RECEIPT, 1)

                    if (st.hasQuestItems(JACKSON_RECEIPT, SILVIA_RECEIPT)) {
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else
                        st.playSound(QuestState.SOUND_ITEMGET)
                } else
                    htmltext = "30054-02.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private val qn = "Q153_DeliverGoods"

        // NPCs
        private val JACKSON = 30002
        private val SILVIA = 30003
        private val ARNOLD = 30041
        private val RANT = 30054

        // Items
        private val DELIVERY_LIST = 1012
        private val HEAVY_WOOD_BOX = 1013
        private val CLOTH_BUNDLE = 1014
        private val CLAY_POT = 1015
        private val JACKSON_RECEIPT = 1016
        private val SILVIA_RECEIPT = 1017
        private val RANT_RECEIPT = 1018

        // Rewards
        private val SOULSHOT_NO_GRADE = 1835
        private val RING_OF_KNOWLEDGE = 875
    }
}