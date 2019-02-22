package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q316_DestroyPlagueCarriers : Quest(316, "Destroy Plague Carriers") {
    init {

        setItemsIds(WERERAT_FANG, VAROOL_FOULCLAW_FANG)

        addStartNpc(30155) // Ellenia
        addTalkId(30155)

        addKillId(SUKAR_WERERAT, SUKAR_WERERAT_LEADER, VAROOL_FOULCLAW)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30155-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30155-08.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF)
                htmltext = "30155-00.htm"
            else if (player.level < 18)
                htmltext = "30155-02.htm"
            else
                htmltext = "30155-03.htm"

            Quest.STATE_STARTED -> {
                val ratFangs = st.getQuestItemsCount(WERERAT_FANG)
                val varoolFangs = st.getQuestItemsCount(VAROOL_FOULCLAW_FANG)

                if (ratFangs + varoolFangs == 0)
                    htmltext = "30155-05.htm"
                else {
                    htmltext = "30155-07.htm"
                    st.takeItems(WERERAT_FANG, -1)
                    st.takeItems(VAROOL_FOULCLAW_FANG, -1)
                    st.rewardItems(57, ratFangs * 30 + varoolFangs * 10000 + if (ratFangs > 10) 5000 else 0)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            SUKAR_WERERAT, SUKAR_WERERAT_LEADER -> st.dropItems(WERERAT_FANG, 1, 0, 400000)

            VAROOL_FOULCLAW -> st.dropItems(VAROOL_FOULCLAW_FANG, 1, 1, 200000)
        }

        return null
    }

    companion object {
        private val qn = "Q316_DestroyPlagueCarriers"

        // Items
        private val WERERAT_FANG = 1042
        private val VAROOL_FOULCLAW_FANG = 1043

        // Monsters
        private val SUKAR_WERERAT = 20040
        private val SUKAR_WERERAT_LEADER = 20047
        private val VAROOL_FOULCLAW = 27020
    }
}