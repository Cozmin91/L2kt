package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q340_SubjugationOfLizardmen : Quest(340, "Subjugation of Lizardmen") {
    init {

        setItemsIds(CARGO, HOLY, ROSARY, TOTEM)

        addStartNpc(WEISZ)
        addTalkId(WEISZ, ADONIUS, LEVIAN, CHEST)

        addKillId(20008, 20010, 20014, 20024, 20027, 20030, 25146)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30385-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30385-07.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CARGO, -1)
        } else if (event.equals("30385-09.htm", ignoreCase = true)) {
            st.takeItems(CARGO, -1)
            st.rewardItems(57, 4090)
        } else if (event.equals("30385-10.htm", ignoreCase = true)) {
            st.takeItems(CARGO, -1)
            st.rewardItems(57, 4090)
            st.exitQuest(true)
        } else if (event.equals("30375-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30037-02.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30989-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(TOTEM, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 17) "30385-01.htm" else "30385-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    WEISZ -> if (cond == 1)
                        htmltext = if (st.getQuestItemsCount(CARGO) < 30) "30385-05.htm" else "30385-06.htm"
                    else if (cond == 2)
                        htmltext = "30385-11.htm"
                    else if (cond == 7) {
                        htmltext = "30385-13.htm"
                        st.rewardItems(57, 14700)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    ADONIUS -> if (cond == 2)
                        htmltext = "30375-01.htm"
                    else if (cond == 3) {
                        if (st.hasQuestItems(ROSARY, HOLY)) {
                            htmltext = "30375-04.htm"
                            st["cond"] = "4"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(HOLY, -1)
                            st.takeItems(ROSARY, -1)
                        } else
                            htmltext = "30375-03.htm"
                    } else if (cond == 4)
                        htmltext = "30375-05.htm"

                    LEVIAN -> if (cond == 4)
                        htmltext = "30037-01.htm"
                    else if (cond == 5)
                        htmltext = "30037-03.htm"
                    else if (cond == 6) {
                        htmltext = "30037-04.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(TOTEM, -1)
                    } else if (cond == 7)
                        htmltext = "30037-05.htm"

                    CHEST -> if (cond == 5)
                        htmltext = "30989-01.htm"
                    else
                        htmltext = "30989-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20008 -> if (st.getInt("cond") == 1)
                st.dropItems(CARGO, 1, 30, 500000)

            20010 -> if (st.getInt("cond") == 1)
                st.dropItems(CARGO, 1, 30, 520000)

            20014 -> if (st.getInt("cond") == 1)
                st.dropItems(CARGO, 1, 30, 550000)

            20024, 20027, 20030 -> if (st.getInt("cond") == 3) {
                if (st.dropItems(HOLY, 1, 1, 100000))
                    st.dropItems(ROSARY, 1, 1, 100000)
            }

            25146 -> addSpawn(CHEST, npc, false, 30000, false)
        }
        return null
    }

    companion object {
        private val qn = "Q340_SubjugationOfLizardmen"

        // NPCs
        private val WEISZ = 30385
        private val ADONIUS = 30375
        private val LEVIAN = 30037
        private val CHEST = 30989

        // Items
        private val CARGO = 4255
        private val HOLY = 4256
        private val ROSARY = 4257
        private val TOTEM = 4258
    }
}