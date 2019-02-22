package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q263_OrcSubjugation : Quest(263, "Orc Subjugation") {
    init {

        setItemsIds(ORC_AMULET, ORC_NECKLACE)

        addStartNpc(30346) // Kayleen
        addTalkId(30346)

        addKillId(20385, 20386, 20387, 20388)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30346-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30346-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.DARK_ELF)
                htmltext = "30346-00.htm"
            else if (player.level < 8)
                htmltext = "30346-01.htm"
            else
                htmltext = "30346-02.htm"

            Quest.STATE_STARTED -> {
                val amulet = st.getQuestItemsCount(ORC_AMULET)
                val necklace = st.getQuestItemsCount(ORC_NECKLACE)

                if (amulet == 0 && necklace == 0)
                    htmltext = "30346-04.htm"
                else {
                    htmltext = "30346-05.htm"
                    st.takeItems(ORC_AMULET, -1)
                    st.takeItems(ORC_NECKLACE, -1)
                    st.rewardItems(57, amulet * 20 + necklace * 30)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(if (npc.npcId == 20385) ORC_AMULET else ORC_NECKLACE, 1, 0, 500000)

        return null
    }

    companion object {
        private val qn = "Q263_OrcSubjugation"

        // Items
        private val ORC_AMULET = 1116
        private val ORC_NECKLACE = 1117
    }
}