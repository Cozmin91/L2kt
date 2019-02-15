package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q275_DarkWingedSpies : Quest(275, "Dark Winged Spies") {
    init {

        setItemsIds(DARKWING_BAT_FANG, VARANGKA_PARASITE)

        addStartNpc(30567) // Tantus
        addTalkId(30567)

        addKillId(DARKWING_BAT, VARANGKA_TRACKER)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30567-03.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> if (player.race != ClassRace.ORC)
                htmltext = "30567-00.htm"
            else if (player.level < 11)
                htmltext = "30567-01.htm"
            else
                htmltext = "30567-02.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30567-04.htm"
            else {
                htmltext = "30567-05.htm"
                st.takeItems(DARKWING_BAT_FANG, -1)
                st.takeItems(VARANGKA_PARASITE, -1)
                st.rewardItems(57, 4200)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        when (npc.npcId) {
            DARKWING_BAT -> if (st.dropItemsAlways(DARKWING_BAT_FANG, 1, 70))
                st["cond"] = "2"
            else if (Rnd[100] < 10 && st.getQuestItemsCount(DARKWING_BAT_FANG) > 10 && st.getQuestItemsCount(
                    DARKWING_BAT_FANG
                ) < 66
            ) {
                // Spawn of Varangka Tracker on the npc position.
                addSpawn(VARANGKA_TRACKER, npc, true, 0, true)

                st.giveItems(VARANGKA_PARASITE, 1)
            }

            VARANGKA_TRACKER -> if (st.hasQuestItems(VARANGKA_PARASITE)) {
                st.takeItems(VARANGKA_PARASITE, -1)

                if (st.dropItemsAlways(DARKWING_BAT_FANG, 5, 70))
                    st["cond"] = "2"
            }
        }

        return null
    }

    companion object {
        private val qn = "Q275_DarkWingedSpies"

        // Monsters
        private val DARKWING_BAT = 20316
        private val VARANGKA_TRACKER = 27043

        // Items
        private val DARKWING_BAT_FANG = 1478
        private val VARANGKA_PARASITE = 1479
    }
}