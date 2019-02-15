package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q116_BeyondTheHillsOfWinter : Quest(116, "Beyond the Hills of Winter") {
    init {

        setItemsIds(GOODS)

        addStartNpc(FILAUR)
        addTalkId(FILAUR, OBI)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30535-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30535-05.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(GOODS, 1)
        } else if (event.equals("materials", ignoreCase = true)) {
            htmltext = "32052-02.htm"
            st.takeItems(GOODS, -1)
            st.rewardItems(SSD, 1650)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        } else if (event.equals("adena", ignoreCase = true)) {
            htmltext = "32052-02.htm"
            st.takeItems(GOODS, -1)
            st.giveItems(57, 16500)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext =
                    if (player.level < 30 || player.race != ClassRace.DWARF) "30535-00.htm" else "30535-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    FILAUR -> if (cond == 1) {
                        if (st.getQuestItemsCount(BANDAGE) >= 20 && st.getQuestItemsCount(ENERGY_STONE) >= 5 && st.getQuestItemsCount(
                                THIEF_KEY
                            ) >= 10
                        ) {
                            htmltext = "30535-03.htm"
                            st.takeItems(BANDAGE, 20)
                            st.takeItems(ENERGY_STONE, 5)
                            st.takeItems(THIEF_KEY, 10)
                        } else
                            htmltext = "30535-04.htm"
                    } else if (cond == 2)
                        htmltext = "30535-05.htm"

                    OBI -> if (cond == 2)
                        htmltext = "32052-00.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }
        return htmltext
    }

    companion object {
        private const val qn = "Q116_BeyondTheHillsOfWinter"

        // NPCs
        private const val FILAUR = 30535
        private const val OBI = 32052

        // Items
        private const val BANDAGE = 1833
        private const val ENERGY_STONE = 5589
        private const val THIEF_KEY = 1661
        private const val GOODS = 8098

        // Reward
        private const val SSD = 1463
    }
}