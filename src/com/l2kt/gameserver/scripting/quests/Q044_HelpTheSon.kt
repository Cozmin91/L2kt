package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q044_HelpTheSon : Quest(44, "Help the Son!") {
    init {

        setItemsIds(GEMSTONE_FRAGMENT, GEMSTONE)

        addStartNpc(LUNDY)
        addTalkId(LUNDY, DRIKUS)

        addKillId(MAILLE, MAILLE_SCOUT, MAILLE_GUARD)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30827-01.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30827-03.htm", ignoreCase = true) && st.hasQuestItems(WORK_HAMMER)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(WORK_HAMMER, 1)
        } else if (event.equals("30827-05.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GEMSTONE_FRAGMENT, 30)
            st.giveItems(GEMSTONE, 1)
        } else if (event.equals("30505-06.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GEMSTONE, 1)
        } else if (event.equals("30827-07.htm", ignoreCase = true)) {
            st.giveItems(PET_TICKET, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 24) "30827-00a.htm" else "30827-00.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    LUNDY -> if (cond == 1)
                        htmltext = if (!st.hasQuestItems(WORK_HAMMER)) "30827-01a.htm" else "30827-02.htm"
                    else if (cond == 2)
                        htmltext = "30827-03a.htm"
                    else if (cond == 3)
                        htmltext = "30827-04.htm"
                    else if (cond == 4)
                        htmltext = "30827-05a.htm"
                    else if (cond == 5)
                        htmltext = "30827-06.htm"

                    DRIKUS -> if (cond == 4)
                        htmltext = "30505-05.htm"
                    else if (cond == 5)
                        htmltext = "30505-06a.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "2") ?: return null

        if (st.dropItemsAlways(GEMSTONE_FRAGMENT, 1, 30))
            st["cond"] = "3"

        return null
    }

    companion object {
        private const val qn = "Q044_HelpTheSon"

        // Npcs
        private const val LUNDY = 30827
        private const val DRIKUS = 30505

        // Items
        private const val WORK_HAMMER = 168
        private const val GEMSTONE_FRAGMENT = 7552
        private const val GEMSTONE = 7553
        private const val PET_TICKET = 7585

        // Monsters
        private const val MAILLE = 20919
        private const val MAILLE_SCOUT = 20920
        private const val MAILLE_GUARD = 20921
    }
}