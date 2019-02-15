package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q300_HuntingLetoLizardman : Quest(300, "Hunting Leto Lizardman") {
    init {
        CHANCES[LETO_LIZARDMAN] = 300000
        CHANCES[LETO_LIZARDMAN_ARCHER] = 320000
        CHANCES[LETO_LIZARDMAN_SOLDIER] = 350000
        CHANCES[LETO_LIZARDMAN_WARRIOR] = 650000
        CHANCES[LETO_LIZARDMAN_OVERLORD] = 700000
    }

    init {

        setItemsIds(BRACELET)

        addStartNpc(30126) // Rath
        addTalkId(30126)

        addKillId(
            LETO_LIZARDMAN,
            LETO_LIZARDMAN_ARCHER,
            LETO_LIZARDMAN_SOLDIER,
            LETO_LIZARDMAN_WARRIOR,
            LETO_LIZARDMAN_OVERLORD
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30126-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30126-05.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(BRACELET) >= 60) {
                htmltext = "30126-06.htm"
                st.takeItems(BRACELET, -1)

                val luck = Rnd[3]
                if (luck == 0)
                    st.rewardItems(57, 30000)
                else if (luck == 1)
                    st.rewardItems(1867, 50)
                else if (luck == 2)
                    st.rewardItems(1872, 50)

                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 34) "30126-01.htm" else "30126-02.htm"

            Quest.STATE_STARTED -> htmltext = if (st.getInt("cond") == 1) "30126-04a.htm" else "30126-04.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        if (st.dropItems(BRACELET, 1, 60, CHANCES[npc.npcId] ?: 0))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q300_HuntingLetoLizardman"

        // Item
        private val BRACELET = 7139

        // Monsters
        private val LETO_LIZARDMAN = 20577
        private val LETO_LIZARDMAN_ARCHER = 20578
        private val LETO_LIZARDMAN_SOLDIER = 20579
        private val LETO_LIZARDMAN_WARRIOR = 20580
        private val LETO_LIZARDMAN_OVERLORD = 20582

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}