package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q344_1000YearsTheEndOfLamentation : Quest(344, "1000 Years, the End of Lamentation") {
    init {
        CHANCES[20236] = 380000
        CHANCES[20237] = 490000
        CHANCES[20238] = 460000
        CHANCES[20239] = 490000
        CHANCES[20240] = 530000
        CHANCES[20272] = 380000
        CHANCES[20273] = 490000
        CHANCES[20274] = 460000
        CHANCES[20275] = 490000
        CHANCES[20276] = 530000
    }

    init {

        setItemsIds(ARTICLE_DEAD_HERO, OLD_KEY, OLD_HILT, OLD_TOTEM, CRUCIFIX)

        addStartNpc(GILMORE)
        addTalkId(GILMORE, RODEMAI, ORVEN, GARVARENTZ, KAIEN)

        addKillId(20236, 20237, 20238, 20239, 20240, 20272, 20273, 20274, 20275, 20276)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30754-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30754-07.htm", ignoreCase = true)) {
            if (st["success"] != null) {
                st["cond"] = "1"
                st.unset("success")
                st.playSound(QuestState.SOUND_MIDDLE)
            }
        } else if (event.equals("30754-08.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30754-06.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(ARTICLE_DEAD_HERO))
                htmltext = "30754-06a.htm"
            else {
                val amount = st.getQuestItemsCount(ARTICLE_DEAD_HERO)

                st.takeItems(ARTICLE_DEAD_HERO, -1)
                st.giveItems(57, amount * 60)

                // Special item, % based on actual number of qItems.
                if (Rnd[1000] < Math.min(10, Math.max(1, amount / 10)))
                    htmltext = "30754-10.htm"
            }
        } else if (event.equals("30754-11.htm", ignoreCase = true)) {
            val random = Rnd[4]
            if (random < 1) {
                htmltext = "30754-12.htm"
                st.giveItems(OLD_KEY, 1)
            } else if (random < 2) {
                htmltext = "30754-13.htm"
                st.giveItems(OLD_HILT, 1)
            } else if (random < 3) {
                htmltext = "30754-14.htm"
                st.giveItems(OLD_TOTEM, 1)
            } else
                st.giveItems(CRUCIFIX, 1)

            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 48) "30754-01.htm" else "30754-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    GILMORE -> if (cond == 1)
                        htmltext = if (st.hasQuestItems(ARTICLE_DEAD_HERO)) "30754-05.htm" else "30754-09.htm"
                    else if (cond == 2)
                        htmltext = if (st["success"] != null) "30754-16.htm" else "30754-15.htm"

                    else -> if (cond == 2) {
                        if (st["success"] != null)
                            htmltext = npc.npcId.toString() + "-02.htm"
                        else {
                            rewards(st, npc.npcId)
                            htmltext = npc.npcId.toString() + "-01.htm"
                        }
                    }
                }
            }
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        st.dropItems(ARTICLE_DEAD_HERO, 1, 0, CHANCES[npc.npcId] ?: 0)

        return null
    }

    companion object {
        private val qn = "Q344_1000YearsTheEndOfLamentation"

        // NPCs
        private val GILMORE = 30754
        private val RODEMAI = 30756
        private val ORVEN = 30857
        private val KAIEN = 30623
        private val GARVARENTZ = 30704

        // Items
        private val ARTICLE_DEAD_HERO = 4269
        private val OLD_KEY = 4270
        private val OLD_HILT = 4271
        private val OLD_TOTEM = 4272
        private val CRUCIFIX = 4273

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()

        private fun rewards(st: QuestState, npcId: Int) {
            when (npcId) {
                ORVEN -> if (st.hasQuestItems(CRUCIFIX)) {
                    st["success"] = "1"
                    st.takeItems(CRUCIFIX, -1)

                    val chance = Rnd[100]
                    if (chance < 80)
                        st.giveItems(1875, 19)
                    else if (chance < 95)
                        st.giveItems(952, 5)
                    else
                        st.giveItems(2437, 1)
                }

                GARVARENTZ -> if (st.hasQuestItems(OLD_TOTEM)) {
                    st["success"] = "1"
                    st.takeItems(OLD_TOTEM, -1)

                    val chance = Rnd[100]
                    if (chance < 55)
                        st.giveItems(1882, 70)
                    else if (chance < 99)
                        st.giveItems(1881, 50)
                    else
                        st.giveItems(191, 1)
                }

                KAIEN -> if (st.hasQuestItems(OLD_HILT)) {
                    st["success"] = "1"
                    st.takeItems(OLD_HILT, -1)

                    val chance = Rnd[100]
                    if (chance < 60)
                        st.giveItems(1874, 25)
                    else if (chance < 85)
                        st.giveItems(1887, 10)
                    else if (chance < 99)
                        st.giveItems(951, 1)
                    else
                        st.giveItems(133, 1)
                }

                RODEMAI -> if (st.hasQuestItems(OLD_KEY)) {
                    st["success"] = "1"
                    st.takeItems(OLD_KEY, -1)

                    val chance = Rnd[100]
                    if (chance < 80)
                        st.giveItems(1879, 55)
                    else if (chance < 95)
                        st.giveItems(951, 1)
                    else
                        st.giveItems(885, 1)
                }
            }
        }
    }
}