package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q364_JovialAccordion : Quest(364, "Jovial Accordion") {
    init {

        setItemsIds(KEY_1, KEY_2, STOLEN_BEER, STOLEN_CLOTHES)

        addStartNpc(BARBADO)
        addTalkId(BARBADO, SWAN, SABRIN, XABER, CLOTH_CHEST, BEER_CHEST)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30959-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["items"] = "0"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30957-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(KEY_1, 1)
            st.giveItems(KEY_2, 1)
        } else if (event.equals("30960-04.htm", ignoreCase = true)) {
            if (st.hasQuestItems(KEY_2)) {
                st.takeItems(KEY_2, 1)
                if (Rnd.nextBoolean()) {
                    htmltext = "30960-02.htm"
                    st.giveItems(STOLEN_BEER, 1)
                    st.playSound(QuestState.SOUND_ITEMGET)
                }
            }
        } else if (event.equals("30961-04.htm", ignoreCase = true)) {
            if (st.hasQuestItems(KEY_1)) {
                st.takeItems(KEY_1, 1)
                if (Rnd.nextBoolean()) {
                    htmltext = "30961-02.htm"
                    st.giveItems(STOLEN_CLOTHES, 1)
                    st.playSound(QuestState.SOUND_ITEMGET)
                }
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30959-00.htm" else "30959-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                val stolenItems = st.getInt("items")

                when (npc.npcId) {
                    BARBADO -> if (cond == 1 || cond == 2)
                        htmltext = "30959-03.htm"
                    else if (cond == 3) {
                        htmltext = "30959-04.htm"
                        st.giveItems(ECHO, 1)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    SWAN -> if (cond == 1)
                        htmltext = "30957-01.htm"
                    else if (cond == 2) {
                        if (stolenItems > 0) {
                            st["cond"] = "3"
                            st.playSound(QuestState.SOUND_MIDDLE)

                            if (stolenItems == 2) {
                                htmltext = "30957-04.htm"
                                st.rewardItems(57, 100)
                            } else
                                htmltext = "30957-05.htm"
                        } else {
                            if (!st.hasQuestItems(KEY_1) && !st.hasQuestItems(KEY_2)) {
                                htmltext = "30957-06.htm"
                                st.playSound(QuestState.SOUND_FINISH)
                                st.exitQuest(true)
                            } else
                                htmltext = "30957-03.htm"
                        }
                    } else if (cond == 3)
                        htmltext = "30957-07.htm"

                    BEER_CHEST -> {
                        htmltext = "30960-03.htm"
                        if (cond == 2 && st.hasQuestItems(KEY_2))
                            htmltext = "30960-01.htm"
                    }

                    CLOTH_CHEST -> {
                        htmltext = "30961-03.htm"
                        if (cond == 2 && st.hasQuestItems(KEY_1))
                            htmltext = "30961-01.htm"
                    }

                    SABRIN -> if (st.hasQuestItems(STOLEN_BEER)) {
                        htmltext = "30060-01.htm"
                        st["items"] = (stolenItems + 1).toString()
                        st.playSound(QuestState.SOUND_ITEMGET)
                        st.takeItems(STOLEN_BEER, 1)
                    } else
                        htmltext = "30060-02.htm"

                    XABER -> if (st.hasQuestItems(STOLEN_CLOTHES)) {
                        htmltext = "30075-01.htm"
                        st["items"] = (stolenItems + 1).toString()
                        st.playSound(QuestState.SOUND_ITEMGET)
                        st.takeItems(STOLEN_CLOTHES, 1)
                    } else
                        htmltext = "30075-02.htm"
                }
            }
        }

        return htmltext
    }

    companion object {
        private val qn = "Q364_JovialAccordion"

        // NPCs
        private val BARBADO = 30959
        private val SWAN = 30957
        private val SABRIN = 30060
        private val XABER = 30075
        private val CLOTH_CHEST = 30961
        private val BEER_CHEST = 30960

        // Items
        private val KEY_1 = 4323
        private val KEY_2 = 4324
        private val STOLEN_BEER = 4321
        private val STOLEN_CLOTHES = 4322
        private val ECHO = 4421
    }
}