package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q035_FindGlitteringJewelry : Quest(35, "Find Glittering Jewelry") {
    init {

        setItemsIds(ROUGH_JEWEL)

        addStartNpc(ELLIE)
        addTalkId(ELLIE, FELTON)

        addKillId(20135) // Alligator
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30091-1.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30879-1.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30091-3.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ROUGH_JEWEL, 10)
        } else if (event.equals("30091-5.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(ORIHARUKON) >= 5 && st.getQuestItemsCount(SILVER_NUGGET) >= 500 && st.getQuestItemsCount(
                    THONS
                ) >= 150
            ) {
                st.takeItems(ORIHARUKON, 5)
                st.takeItems(SILVER_NUGGET, 500)
                st.takeItems(THONS, 150)
                st.giveItems(JEWEL_BOX, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "30091-4a.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level >= 60) {
                val fwear = player.getQuestState("Q037_MakeFormalWear")
                if (fwear != null && fwear.getInt("cond") == 6)
                    htmltext = "30091-0.htm"
                else
                    htmltext = "30091-0a.htm"
            } else
                htmltext = "30091-0b.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ELLIE -> if (cond == 1 || cond == 2)
                        htmltext = "30091-1a.htm"
                    else if (cond == 3)
                        htmltext = "30091-2.htm"
                    else if (cond == 4)
                        htmltext =
                                if (st.getQuestItemsCount(ORIHARUKON) >= 5 && st.getQuestItemsCount(SILVER_NUGGET) >= 500 && st.getQuestItemsCount(
                                        THONS
                                    ) >= 150
                                ) "30091-4.htm" else "30091-4a.htm"

                    FELTON -> if (cond == 1)
                        htmltext = "30879-0.htm"
                    else if (cond > 1)
                        htmltext = "30879-1a.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "2") ?: return null

        if (st.dropItems(ROUGH_JEWEL, 1, 10, 500000))
            st["cond"] = "3"

        return null
    }

    companion object {
        private const val qn = "Q035_FindGlitteringJewelry"

        // NPCs
        private const val ELLIE = 30091
        private const val FELTON = 30879

        // Items
        private const val ROUGH_JEWEL = 7162
        private const val ORIHARUKON = 1893
        private const val SILVER_NUGGET = 1873
        private const val THONS = 4044

        // Reward
        private const val JEWEL_BOX = 7077
    }
}