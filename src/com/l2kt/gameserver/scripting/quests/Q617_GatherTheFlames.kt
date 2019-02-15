package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q617_GatherTheFlames : Quest(617, "Gather the Flames") {
    init {
        CHANCES[21381] = 510000
        CHANCES[21653] = 510000
        CHANCES[21387] = 530000
        CHANCES[21655] = 530000
        CHANCES[21390] = 560000
        CHANCES[21656] = 690000
        CHANCES[21389] = 550000
        CHANCES[21388] = 530000
        CHANCES[21383] = 510000
        CHANCES[21392] = 560000
        CHANCES[21382] = 600000
        CHANCES[21654] = 520000
        CHANCES[21384] = 640000
        CHANCES[21394] = 510000
        CHANCES[21395] = 560000
        CHANCES[21385] = 520000
        CHANCES[21391] = 550000
        CHANCES[21393] = 580000
        CHANCES[21657] = 570000
        CHANCES[21386] = 520000
        CHANCES[21652] = 490000
        CHANCES[21378] = 490000
        CHANCES[21376] = 480000
        CHANCES[21377] = 480000
        CHANCES[21379] = 590000
        CHANCES[21380] = 490000
    }

    init {

        setItemsIds(TORCH)

        addStartNpc(VULCAN, HILDA)
        addTalkId(VULCAN, HILDA, ROONEY)

        for (mobs in CHANCES.keys)
            addKillId(mobs)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31539-03.htm", ignoreCase = true) || event.equals("31271-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31539-05.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(TORCH) >= 1000) {
                htmltext = "31539-07.htm"
                st.takeItems(TORCH, 1000)
                st.giveItems(REWARD[Rnd[REWARD.size]], 1)
            }
        } else if (event.equals("31539-08.htm", ignoreCase = true)) {
            st.takeItems(TORCH, -1)
            st.exitQuest(true)
        } else if (StringUtil.isDigit(event)) {
            if (st.getQuestItemsCount(TORCH) >= 1200) {
                htmltext = "32049-03.htm"
                st.takeItems(TORCH, 1200)
                st.giveItems(Integer.valueOf(event), 1)
            } else
                htmltext = "32049-02.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = npc.npcId.toString() + (if (player.level >= 74) "-01.htm" else "-02.htm")

            Quest.STATE_STARTED -> when (npc.npcId) {
                VULCAN -> htmltext = if (st.getQuestItemsCount(TORCH) >= 1000) "31539-04.htm" else "31539-05.htm"

                HILDA -> htmltext = "31271-04.htm"

                ROONEY -> htmltext = if (st.getQuestItemsCount(TORCH) >= 1200) "32049-01.htm" else "32049-02.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(TORCH, 1, 0, CHANCES[npc.npcId] ?: 0)

        return null
    }

    companion object {
        private val qn = "Q617_GatherTheFlames"

        // NPCs
        private val HILDA = 31271
        private val VULCAN = 31539
        private val ROONEY = 32049

        // Items
        private val TORCH = 7264

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()

        // Rewards
        private val REWARD = intArrayOf(6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580)
    }
}