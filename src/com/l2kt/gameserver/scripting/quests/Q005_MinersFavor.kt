package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q005_MinersFavor : Quest(5, "Miner's Favor") {
    init {

        setItemsIds(BOLTERS_LIST, MINING_BOOTS, MINERS_PICK, BOOMBOOM_POWDER, REDSTONE_BEER, BOLTERS_SMELLY_SOCKS)

        addStartNpc(BOLTER)
        addTalkId(BOLTER, SHARI, GARITA, REED, BRUNON)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30554-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(BOLTERS_LIST, 1)
            st.giveItems(BOLTERS_SMELLY_SOCKS, 1)
        } else if (event.equals("30526-02.htm", ignoreCase = true)) {
            st.takeItems(BOLTERS_SMELLY_SOCKS, 1)
            st.giveItems(MINERS_PICK, 1)

            if (st.hasQuestItems(MINING_BOOTS, BOOMBOOM_POWDER, REDSTONE_BEER)) {
                st["cond"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
            } else
                st.playSound(QuestState.SOUND_ITEMGET)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 2) "30554-01.htm" else "30554-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    BOLTER -> if (cond == 1)
                        htmltext = "30554-04.htm"
                    else if (cond == 2) {
                        htmltext = "30554-06.htm"
                        st.takeItems(BOLTERS_LIST, 1)
                        st.takeItems(BOOMBOOM_POWDER, 1)
                        st.takeItems(MINERS_PICK, 1)
                        st.takeItems(MINING_BOOTS, 1)
                        st.takeItems(REDSTONE_BEER, 1)
                        st.giveItems(NECKLACE, 1)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    SHARI -> if (cond == 1 && !st.hasQuestItems(BOOMBOOM_POWDER)) {
                        htmltext = "30517-01.htm"
                        st.giveItems(BOOMBOOM_POWDER, 1)

                        if (st.hasQuestItems(MINING_BOOTS, MINERS_PICK, REDSTONE_BEER)) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else
                        htmltext = "30517-02.htm"

                    GARITA -> if (cond == 1 && !st.hasQuestItems(MINING_BOOTS)) {
                        htmltext = "30518-01.htm"
                        st.giveItems(MINING_BOOTS, 1)

                        if (st.hasQuestItems(MINERS_PICK, BOOMBOOM_POWDER, REDSTONE_BEER)) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else
                        htmltext = "30518-02.htm"

                    REED -> if (cond == 1 && !st.hasQuestItems(REDSTONE_BEER)) {
                        htmltext = "30520-01.htm"
                        st.giveItems(REDSTONE_BEER, 1)

                        if (st.hasQuestItems(MINING_BOOTS, MINERS_PICK, BOOMBOOM_POWDER)) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else
                        htmltext = "30520-02.htm"

                    BRUNON -> if (cond == 1 && !st.hasQuestItems(MINERS_PICK))
                        htmltext = "30526-01.htm"
                    else
                        htmltext = "30526-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q005_MinersFavor"

        // NPCs
        private const val BOLTER = 30554
        private const val SHARI = 30517
        private const val GARITA = 30518
        private const val REED = 30520
        private const val BRUNON = 30526

        // Items
        private const val BOLTERS_LIST = 1547
        private const val MINING_BOOTS = 1548
        private const val MINERS_PICK = 1549
        private const val BOOMBOOM_POWDER = 1550
        private const val REDSTONE_BEER = 1551
        private const val BOLTERS_SMELLY_SOCKS = 1552

        // Reward
        private const val NECKLACE = 906
    }
}