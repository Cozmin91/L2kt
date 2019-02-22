package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q294_CovertBusiness : Quest(294, "Covert Business") {
    init {

        setItemsIds(BAT_FANG)

        addStartNpc(30534) // Keef
        addTalkId(30534)

        addKillId(20370, 20480) // Barded Bat, Blade Bat
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30534-03.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> if (player.race != ClassRace.DWARF)
                htmltext = "30534-00.htm"
            else if (player.level < 10)
                htmltext = "30534-01.htm"
            else
                htmltext = "30534-02.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30534-04.htm"
            else {
                htmltext = "30534-05.htm"
                st.takeItems(BAT_FANG, -1)
                st.giveItems(RING_OF_RACCOON, 1)
                st.rewardExpAndSp(0, 600)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        var count = 1
        val chance = Rnd[10]
        val isBarded = npc.npcId == 20370

        if (chance < 3)
            count++
        else if (chance < (if (isBarded) 5 else 6))
            count += 2
        else if (isBarded && chance < 7)
            count += 3

        if (st.dropItemsAlways(BAT_FANG, count, 100))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q294_CovertBusiness"

        // Item
        private val BAT_FANG = 1491

        // Reward
        private val RING_OF_RACCOON = 1508
    }
}