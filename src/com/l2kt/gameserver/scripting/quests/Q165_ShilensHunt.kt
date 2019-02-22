package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q165_ShilensHunt : Quest(165, "Shilen's Hunt") {
    init {
        CHANCES[ASHEN_WOLF] = 1000000
        CHANCES[YOUNG_BROWN_KELTIR] = 333333
        CHANCES[BROWN_KELTIR] = 333333
        CHANCES[ELDER_BROWN_KELTIR] = 666667
    }

    init {

        setItemsIds(DARK_BEZOAR)

        addStartNpc(30348) // Nelsya
        addTalkId(30348)

        addKillId(ASHEN_WOLF, YOUNG_BROWN_KELTIR, BROWN_KELTIR, ELDER_BROWN_KELTIR)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30348-03.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> if (player.race != ClassRace.DARK_ELF)
                htmltext = "30348-00.htm"
            else if (player.level < 3)
                htmltext = "30348-01.htm"
            else
                htmltext = "30348-02.htm"

            Quest.STATE_STARTED -> if (st.getQuestItemsCount(DARK_BEZOAR) >= 13) {
                htmltext = "30348-05.htm"
                st.takeItems(DARK_BEZOAR, -1)
                st.rewardItems(LESSER_HEALING_POTION, 5)
                st.rewardExpAndSp(1000, 0)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "30348-04.htm"

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(DARK_BEZOAR, 1, 13, CHANCES[npc.npcId] ?: 0))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q165_ShilensHunt"

        // Monsters
        private val ASHEN_WOLF = 20456
        private val YOUNG_BROWN_KELTIR = 20529
        private val BROWN_KELTIR = 20532
        private val ELDER_BROWN_KELTIR = 20536

        // Items
        private val DARK_BEZOAR = 1160
        private val LESSER_HEALING_POTION = 1060

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}