package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q328_SenseForBusiness : Quest(328, "Sense for Business") {
    init {
        CHANCES[20055] = 48
        CHANCES[20059] = 52
        CHANCES[20067] = 68
        CHANCES[20068] = 76
        CHANCES[20070] = 500000
        CHANCES[20072] = 510000
    }

    init {

        setItemsIds(MONSTER_EYE_LENS, MONSTER_EYE_CARCASS, BASILISK_GIZZARD)

        addStartNpc(30436) // Sarien
        addTalkId(30436)

        addKillId(20055, 20059, 20067, 20068, 20070, 20072)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30436-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30436-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 21) "30436-01.htm" else "30436-02.htm"

            Quest.STATE_STARTED -> {
                val carcasses = st.getQuestItemsCount(MONSTER_EYE_CARCASS)
                val lenses = st.getQuestItemsCount(MONSTER_EYE_LENS)
                val gizzards = st.getQuestItemsCount(BASILISK_GIZZARD)

                val all = carcasses + lenses + gizzards

                if (all == 0)
                    htmltext = "30436-04.htm"
                else {
                    htmltext = "30436-05.htm"
                    st.takeItems(MONSTER_EYE_CARCASS, -1)
                    st.takeItems(MONSTER_EYE_LENS, -1)
                    st.takeItems(BASILISK_GIZZARD, -1)
                    st.rewardItems(57, 25 * carcasses + 1000 * lenses + 60 * gizzards + if (all >= 10) 618 else 0)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId
        val chance = CHANCES[npcId] ?: 0

        if (npcId < 20069) {
            val rnd = Rnd[100]
            if (rnd < chance + 1)
                st.dropItemsAlways(if (rnd < chance) MONSTER_EYE_CARCASS else MONSTER_EYE_LENS, 1, 0)
        } else
            st.dropItems(BASILISK_GIZZARD, 1, 0, chance)

        return null
    }

    companion object {
        private val qn = "Q328_SenseForBusiness"

        // Items
        private val MONSTER_EYE_LENS = 1366
        private val MONSTER_EYE_CARCASS = 1347
        private val BASILISK_GIZZARD = 1348

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}