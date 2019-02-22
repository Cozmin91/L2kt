package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q032_AnObviousLie : Quest(32, "An Obvious Lie") {
    init {

        setItemsIds(MAP, MEDICINAL_HERB)

        addStartNpc(MAXIMILIAN)
        addTalkId(MAXIMILIAN, GENTLER, MIKI_THE_CAT)

        addKillId(20135) // Alligator
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30120-1.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30094-1.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(MAP, 1)
        } else if (event.equals("31706-1.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MAP, 1)
        } else if (event.equals("30094-4.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MEDICINAL_HERB, 20)
        } else if (event.equals("30094-7.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(SPIRIT_ORE) < 500)
                htmltext = "30094-5.htm"
            else {
                st["cond"] = "6"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(SPIRIT_ORE, 500)
            }
        } else if (event.equals("31706-4.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30094-10.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30094-13.htm", ignoreCase = true))
            st.playSound(QuestState.SOUND_MIDDLE)
        else if (event.equals("cat", ignoreCase = true)) {
            if (st.getQuestItemsCount(THREAD) < 1000 || st.getQuestItemsCount(SUEDE) < 500)
                htmltext = "30094-11.htm"
            else {
                htmltext = "30094-14.htm"
                st.takeItems(SUEDE, 500)
                st.takeItems(THREAD, 1000)
                st.giveItems(CAT_EARS, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            }
        } else if (event.equals("racoon", ignoreCase = true)) {
            if (st.getQuestItemsCount(THREAD) < 1000 || st.getQuestItemsCount(SUEDE) < 500)
                htmltext = "30094-11.htm"
            else {
                htmltext = "30094-14.htm"
                st.takeItems(SUEDE, 500)
                st.takeItems(THREAD, 1000)
                st.giveItems(RACOON_EARS, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            }
        } else if (event.equals("rabbit", ignoreCase = true)) {
            if (st.getQuestItemsCount(THREAD) < 1000 || st.getQuestItemsCount(SUEDE) < 500)
                htmltext = "30094-11.htm"
            else {
                htmltext = "30094-14.htm"
                st.takeItems(SUEDE, 500)
                st.takeItems(THREAD, 1000)
                st.giveItems(RABBIT_EARS, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 45) "30120-0a.htm" else "30120-0.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    MAXIMILIAN -> htmltext = "30120-2.htm"

                    GENTLER -> if (cond == 1)
                        htmltext = "30094-0.htm"
                    else if (cond == 2 || cond == 3)
                        htmltext = "30094-2.htm"
                    else if (cond == 4)
                        htmltext = "30094-3.htm"
                    else if (cond == 5)
                        htmltext = if (st.getQuestItemsCount(SPIRIT_ORE) < 500) "30094-5.htm" else "30094-6.htm"
                    else if (cond == 6)
                        htmltext = "30094-8.htm"
                    else if (cond == 7)
                        htmltext = "30094-9.htm"
                    else if (cond == 8)
                        htmltext =
                                if (st.getQuestItemsCount(THREAD) < 1000 || st.getQuestItemsCount(SUEDE) < 500) "30094-11.htm" else "30094-12.htm"

                    MIKI_THE_CAT -> if (cond == 2)
                        htmltext = "31706-0.htm"
                    else if (cond > 2 && cond < 6)
                        htmltext = "31706-2.htm"
                    else if (cond == 6)
                        htmltext = "31706-3.htm"
                    else if (cond > 6)
                        htmltext = "31706-5.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "3") ?: return null

        if (st.dropItemsAlways(MEDICINAL_HERB, 1, 20))
            st["cond"] = "4"

        return null
    }

    companion object {
        private const val qn = "Q032_AnObviousLie"

        // Items
        private const val SUEDE = 1866
        private const val THREAD = 1868
        private const val SPIRIT_ORE = 3031
        private const val MAP = 7165
        private const val MEDICINAL_HERB = 7166

        // Rewards
        private const val CAT_EARS = 6843
        private const val RACOON_EARS = 7680
        private const val RABBIT_EARS = 7683

        // NPCs
        private const val GENTLER = 30094
        private const val MAXIMILIAN = 30120
        private const val MIKI_THE_CAT = 31706
    }
}