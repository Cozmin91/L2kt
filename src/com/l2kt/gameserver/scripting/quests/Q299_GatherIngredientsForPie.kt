package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q299_GatherIngredientsForPie : Quest(299, "Gather Ingredients for Pie") {
    init {

        setItemsIds(FRUIT_BASKET, AVELLAN_SPICE, HONEY_POUCH)

        addStartNpc(EMILY)
        addTalkId(EMILY, LARA, BRIGHT)

        addKillId(20934, 20935) // Wasp Worker, Wasp Leader
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30620-1.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30620-3.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(HONEY_POUCH, -1)
        } else if (event.equals("30063-1.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(AVELLAN_SPICE, 1)
        } else if (event.equals("30620-5.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(AVELLAN_SPICE, 1)
        } else if (event.equals("30466-1.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(FRUIT_BASKET, 1)
        } else if (event.equals("30620-7a.htm", ignoreCase = true)) {
            if (st.hasQuestItems(FRUIT_BASKET)) {
                htmltext = "30620-7.htm"
                st.takeItems(FRUIT_BASKET, 1)
                st.rewardItems(57, 25000)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else
                st["cond"] = "5"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 34) "30620-0a.htm" else "30620-0.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    EMILY -> if (cond == 1)
                        htmltext = "30620-1a.htm"
                    else if (cond == 2) {
                        if (st.getQuestItemsCount(HONEY_POUCH) >= 100)
                            htmltext = "30620-2.htm"
                        else {
                            htmltext = "30620-2a.htm"
                            st.exitQuest(true)
                        }
                    } else if (cond == 3)
                        htmltext = "30620-3a.htm"
                    else if (cond == 4) {
                        if (st.hasQuestItems(AVELLAN_SPICE))
                            htmltext = "30620-4.htm"
                        else {
                            htmltext = "30620-4a.htm"
                            st.exitQuest(true)
                        }
                    } else if (cond == 5)
                        htmltext = "30620-5a.htm"
                    else if (cond == 6)
                        htmltext = "30620-6.htm"

                    LARA -> if (cond == 3)
                        htmltext = "30063-0.htm"
                    else if (cond > 3)
                        htmltext = "30063-1a.htm"

                    BRIGHT -> if (cond == 5)
                        htmltext = "30466-0.htm"
                    else if (cond > 5)
                        htmltext = "30466-1a.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        if (st.dropItems(HONEY_POUCH, 1, 100, if (npc.npcId == 20934) 571000 else 625000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q299_GatherIngredientsForPie"

        // NPCs
        private val LARA = 30063
        private val BRIGHT = 30466
        private val EMILY = 30620

        // Items
        private val FRUIT_BASKET = 7136
        private val AVELLAN_SPICE = 7137
        private val HONEY_POUCH = 7138
    }
}