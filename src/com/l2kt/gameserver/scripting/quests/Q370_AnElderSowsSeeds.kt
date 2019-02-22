package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q370_AnElderSowsSeeds : Quest(370, "An Elder Sows Seeds") {
    init {
        CHANCES[20082] = 86000
        CHANCES[20084] = 94000
        CHANCES[20086] = 90000
        CHANCES[20089] = 100000
        CHANCES[20090] = 202000
    }

    init {

        setItemsIds(SPELLBOOK_PAGE, CHAPTER_OF_FIRE, CHAPTER_OF_WATER, CHAPTER_OF_WIND, CHAPTER_OF_EARTH)

        addStartNpc(CASIAN)
        addTalkId(CASIAN)

        addKillId(20082, 20084, 20086, 20089, 20090)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30612-3.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30612-6.htm", ignoreCase = true)) {
            if (st.hasQuestItems(CHAPTER_OF_FIRE, CHAPTER_OF_WATER, CHAPTER_OF_WIND, CHAPTER_OF_EARTH)) {
                htmltext = "30612-8.htm"
                st.takeItems(CHAPTER_OF_FIRE, 1)
                st.takeItems(CHAPTER_OF_WATER, 1)
                st.takeItems(CHAPTER_OF_WIND, 1)
                st.takeItems(CHAPTER_OF_EARTH, 1)
                st.rewardItems(57, 3600)
            }
        } else if (event.equals("30612-9.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 28) "30612-0a.htm" else "30612-0.htm"

            Quest.STATE_STARTED -> htmltext = "30612-4.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(SPELLBOOK_PAGE, 1, 0, CHANCES[npc.npcId] ?: 0)

        return null
    }

    companion object {
        private val qn = "Q370_AnElderSowsSeeds"

        // NPC
        private val CASIAN = 30612

        // Items
        private val SPELLBOOK_PAGE = 5916
        private val CHAPTER_OF_FIRE = 5917
        private val CHAPTER_OF_WATER = 5918
        private val CHAPTER_OF_WIND = 5919
        private val CHAPTER_OF_EARTH = 5920

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}