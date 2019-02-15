package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q652_AnAgedExAdventurer : Quest(652, "An Aged Ex-Adventurer") {

    // Current position
    private var _currentPosition = 0

    init {

        addStartNpc(TANTAN)
        addTalkId(TANTAN, SARA)

        addSpawn(TANTAN, 78355, -1325, -3659, 0, false, 0, false)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("32012-02.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(SOULSHOT_C) >= 100) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.takeItems(SOULSHOT_C, 100)

                npc!!.ai.setIntention(CtrlIntention.MOVE_TO, Location(85326, 7869, -3620))
                startQuestTimer("apparition_npc", 6000, npc, player, false)
            } else {
                htmltext = "32012-02a.htm"
                st.exitQuest(true)
            }
        } else if (event.equals("apparition_npc", ignoreCase = true)) {
            var chance = Rnd[5]

            // Loop to avoid to spawn to the same place.
            while (chance == _currentPosition)
                chance = Rnd[5]

            // Register new position.
            _currentPosition = chance

            npc!!.deleteMe()
            addSpawn(TANTAN, SPAWNS[chance], false, 0, false)
            return null
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 46) "32012-00.htm" else "32012-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                SARA -> {
                    if (Rnd[100] < 50) {
                        htmltext = "30180-01.htm"
                        st.rewardItems(57, 5026)
                        st.giveItems(ENCHANT_ARMOR_D, 1)
                    } else {
                        htmltext = "30180-02.htm"
                        st.rewardItems(57, 10000)
                    }
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                }

                TANTAN -> htmltext = "32012-04a.htm"
            }
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q652_AnAgedExAdventurer"

        // NPCs
        private const val TANTAN = 32012
        private const val SARA = 30180

        // Item
        private const val SOULSHOT_C = 1464

        // Reward
        private const val ENCHANT_ARMOR_D = 956

        // Table of possible spawns
        private val SPAWNS = arrayOf(
            SpawnLocation(78355, -1325, -3659, 0),
            SpawnLocation(79890, -6132, -2922, 0),
            SpawnLocation(90012, -7217, -3085, 0),
            SpawnLocation(94500, -10129, -3290, 0),
            SpawnLocation(96534, -1237, -3677, 0)
        )
    }
}