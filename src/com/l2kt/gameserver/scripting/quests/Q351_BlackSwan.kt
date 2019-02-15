package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q351_BlackSwan : Quest(351, "Black Swan") {
    init {

        setItemsIds(ORDER_OF_GOSTA, BARREL_OF_LEAGUE, LIZARD_FANG)

        addStartNpc(GOSTA)
        addTalkId(GOSTA, IASON_HEINE, ROMAN)

        addKillId(20784, 20785, 21639, 21640)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30916-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ORDER_OF_GOSTA, 1)
        } else if (event.equals("30969-02a.htm", ignoreCase = true)) {
            val lizardFangs = st.getQuestItemsCount(LIZARD_FANG)
            if (lizardFangs > 0) {
                htmltext = "30969-02.htm"

                st.takeItems(LIZARD_FANG, -1)
                st.rewardItems(57, lizardFangs * 20)
            }
        } else if (event.equals("30969-03a.htm", ignoreCase = true)) {
            val barrels = st.getQuestItemsCount(BARREL_OF_LEAGUE)
            if (barrels > 0) {
                htmltext = "30969-03.htm"

                st.takeItems(BARREL_OF_LEAGUE, -1)
                st.rewardItems(BILL_OF_IASON_HEINE, barrels)

                // Heine explains than player can speak with Roman in order to exchange bills for rewards.
                if (st.getInt("cond") == 1) {
                    st["cond"] = "2"
                    st.playSound(QuestState.SOUND_MIDDLE)
                }
            }
        } else if (event.equals("30969-06.htm", ignoreCase = true)) {
            // If no more quest items finish the quest for real, else send a "Return" type HTM.
            if (!st.hasQuestItems(BARREL_OF_LEAGUE, LIZARD_FANG)) {
                htmltext = "30969-07.htm"
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 32) "30916-00.htm" else "30916-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                GOSTA -> htmltext = "30916-04.htm"

                IASON_HEINE -> htmltext = "30969-01.htm"

                ROMAN -> htmltext = if (st.hasQuestItems(BILL_OF_IASON_HEINE)) "30897-01.htm" else "30897-02.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val random = Rnd[4]
        if (random < 3) {
            st.dropItemsAlways(LIZARD_FANG, if (random < 2) 1 else 2, 0)
            st.dropItems(BARREL_OF_LEAGUE, 1, 0, 50000)
        } else
            st.dropItems(BARREL_OF_LEAGUE, 1, 0, if (npc.npcId > 20785) 30000 else 40000)

        return null
    }

    companion object {
        private val qn = "Q351_BlackSwan"

        // NPCs
        private val GOSTA = 30916
        private val IASON_HEINE = 30969
        private val ROMAN = 30897

        // Items
        private val ORDER_OF_GOSTA = 4296
        private val LIZARD_FANG = 4297
        private val BARREL_OF_LEAGUE = 4298
        private val BILL_OF_IASON_HEINE = 4310
    }
}