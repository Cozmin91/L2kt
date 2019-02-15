package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q163_LegacyOfThePoet : Quest(163, "Legacy of the Poet") {
    init {

        setItemsIds(*RUMIELS_POEMS)

        addStartNpc(STARDEN)
        addTalkId(STARDEN)

        addKillId(20372, 20373)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30220-07.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race == ClassRace.DARK_ELF)
                htmltext = "30220-00.htm"
            else if (player.level < 11)
                htmltext = "30220-02.htm"
            else
                htmltext = "30220-03.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 2) {
                htmltext = "30220-09.htm"

                for (poem in RUMIELS_POEMS)
                    st.takeItems(poem, -1)

                st.rewardItems(57, 13890)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "30220-08.htm"

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropMultipleItems(DROPLIST))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q163_LegacyOfThePoet"

        // NPC
        private val STARDEN = 30220

        // Items
        private val RUMIELS_POEMS = intArrayOf(1038, 1039, 1040, 1041)

        // Droplist
        private val DROPLIST = arrayOf(
            intArrayOf(RUMIELS_POEMS[0], 1, 1, 100000),
            intArrayOf(RUMIELS_POEMS[1], 1, 1, 200000),
            intArrayOf(RUMIELS_POEMS[2], 1, 1, 200000),
            intArrayOf(RUMIELS_POEMS[3], 1, 1, 400000)
        )
    }
}