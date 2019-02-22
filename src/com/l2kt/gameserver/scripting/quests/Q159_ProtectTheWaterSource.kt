package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q159_ProtectTheWaterSource : Quest(159, "Protect the Water Source") {
    init {

        setItemsIds(PLAGUE_DUST, HYACINTH_CHARM_1, HYACINTH_CHARM_2)

        addStartNpc(30154) // Asterios
        addTalkId(30154)

        addKillId(27017) // Plague Zombie
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30154-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(HYACINTH_CHARM_1, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF)
                htmltext = "30154-00.htm"
            else if (player.level < 12)
                htmltext = "30154-02.htm"
            else
                htmltext = "30154-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "30154-05.htm"
                else if (cond == 2) {
                    htmltext = "30154-06.htm"
                    st["cond"] = "3"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(PLAGUE_DUST, -1)
                    st.takeItems(HYACINTH_CHARM_1, 1)
                    st.giveItems(HYACINTH_CHARM_2, 1)
                } else if (cond == 3)
                    htmltext = "30154-07.htm"
                else if (cond == 4) {
                    htmltext = "30154-08.htm"
                    st.takeItems(HYACINTH_CHARM_2, 1)
                    st.takeItems(PLAGUE_DUST, -1)
                    st.rewardItems(57, 18250)
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(false)
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (st.getInt("cond") == 1 && st.dropItems(PLAGUE_DUST, 1, 1, 400000))
            st["cond"] = "2"
        else if (st.getInt("cond") == 3 && st.dropItems(PLAGUE_DUST, 1, 5, 400000))
            st["cond"] = "4"

        return null
    }

    companion object {
        private val qn = "Q159_ProtectTheWaterSource"

        // Items
        private val PLAGUE_DUST = 1035
        private val HYACINTH_CHARM_1 = 1071
        private val HYACINTH_CHARM_2 = 1072
    }
}