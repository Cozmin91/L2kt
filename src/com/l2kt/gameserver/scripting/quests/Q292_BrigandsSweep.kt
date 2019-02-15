package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q292_BrigandsSweep : Quest(292, "Brigands Sweep") {
    init {

        setItemsIds(GOBLIN_NECKLACE, GOBLIN_PENDANT, GOBLIN_LORD_PENDANT, SUSPICIOUS_MEMO, SUSPICIOUS_CONTRACT)

        addStartNpc(SPIRON)
        addTalkId(SPIRON, BALANKI)

        addKillId(GOBLIN_BRIGAND, GOBLIN_BRIGAND_LEADER, GOBLIN_BRIGAND_LIEUTENANT, GOBLIN_SNOOPER, GOBLIN_LORD)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30532-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30532-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.DWARF)
                htmltext = "30532-00.htm"
            else if (player.level < 5)
                htmltext = "30532-01.htm"
            else
                htmltext = "30532-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                SPIRON -> {
                    val goblinNecklaces = st.getQuestItemsCount(GOBLIN_NECKLACE)
                    val goblinPendants = st.getQuestItemsCount(GOBLIN_PENDANT)
                    val goblinLordPendants = st.getQuestItemsCount(GOBLIN_LORD_PENDANT)
                    val suspiciousMemos = st.getQuestItemsCount(SUSPICIOUS_MEMO)

                    val countAll = goblinNecklaces + goblinPendants + goblinLordPendants

                    val hasContract = st.hasQuestItems(SUSPICIOUS_CONTRACT)

                    if (countAll == 0)
                        htmltext = "30532-04.htm"
                    else {
                        if (hasContract)
                            htmltext = "30532-10.htm"
                        else if (suspiciousMemos > 0) {
                            if (suspiciousMemos > 1)
                                htmltext = "30532-09.htm"
                            else
                                htmltext = "30532-08.htm"
                        } else
                            htmltext = "30532-05.htm"

                        st.takeItems(GOBLIN_NECKLACE, -1)
                        st.takeItems(GOBLIN_PENDANT, -1)
                        st.takeItems(GOBLIN_LORD_PENDANT, -1)

                        if (hasContract) {
                            st["cond"] = "1"
                            st.takeItems(SUSPICIOUS_CONTRACT, -1)
                        }

                        st.rewardItems(
                            57,
                            12 * goblinNecklaces + 36 * goblinPendants + 33 * goblinLordPendants + (if (countAll >= 10) 1000 else 0) + if (hasContract) 1120 else 0
                        )
                    }
                }

                BALANKI -> if (!st.hasQuestItems(SUSPICIOUS_CONTRACT))
                    htmltext = "30533-01.htm"
                else {
                    htmltext = "30533-02.htm"
                    st["cond"] = "1"
                    st.takeItems(SUSPICIOUS_CONTRACT, -1)
                    st.rewardItems(57, 1500)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val chance = Rnd[10]

        if (chance > 5) {
            when (npc.npcId) {
                GOBLIN_BRIGAND, GOBLIN_SNOOPER, GOBLIN_BRIGAND_LIEUTENANT -> st.dropItemsAlways(GOBLIN_NECKLACE, 1, 0)

                GOBLIN_BRIGAND_LEADER -> st.dropItemsAlways(GOBLIN_PENDANT, 1, 0)

                GOBLIN_LORD -> st.dropItemsAlways(GOBLIN_LORD_PENDANT, 1, 0)
            }
        } else if (chance > 4 && st.getInt("cond") == 1 && st.dropItemsAlways(SUSPICIOUS_MEMO, 1, 3)) {
            st["cond"] = "2"
            st.takeItems(SUSPICIOUS_MEMO, -1)
            st.giveItems(SUSPICIOUS_CONTRACT, 1)
        }

        return null
    }

    companion object {
        private val qn = "Q292_BrigandsSweep"

        // NPCs
        private val SPIRON = 30532
        private val BALANKI = 30533

        // Items
        private val GOBLIN_NECKLACE = 1483
        private val GOBLIN_PENDANT = 1484
        private val GOBLIN_LORD_PENDANT = 1485
        private val SUSPICIOUS_MEMO = 1486
        private val SUSPICIOUS_CONTRACT = 1487

        // Monsters
        private val GOBLIN_BRIGAND = 20322
        private val GOBLIN_BRIGAND_LEADER = 20323
        private val GOBLIN_BRIGAND_LIEUTENANT = 20324
        private val GOBLIN_SNOOPER = 20327
        private val GOBLIN_LORD = 20528
    }
}