package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q151_CureForFeverDisease : Quest(151, "Cure for Fever Disease") {
    init {

        setItemsIds(FEVER_MEDICINE, POISON_SAC)

        addStartNpc(ELIAS)
        addTalkId(ELIAS, YOHANES)

        addKillId(20103, 20106, 20108)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30050-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30050-01.htm" else "30050-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ELIAS -> if (cond == 1)
                        htmltext = "30050-04.htm"
                    else if (cond == 2)
                        htmltext = "30050-05.htm"
                    else if (cond == 3) {
                        htmltext = "30050-06.htm"
                        st.takeItems(FEVER_MEDICINE, 1)
                        st.giveItems(102, 1)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    YOHANES -> if (cond == 2) {
                        htmltext = "30032-01.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(POISON_SAC, 1)
                        st.giveItems(FEVER_MEDICINE, 1)
                    } else if (cond == 3)
                        htmltext = "30032-02.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(POISON_SAC, 1, 1, 200000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q151_CureForFeverDisease"

        // Items
        private val POISON_SAC = 703
        private val FEVER_MEDICINE = 704

        // NPCs
        private val ELIAS = 30050
        private val YOHANES = 30032
    }
}