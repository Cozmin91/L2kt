package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q651_RunawayYouth : Quest(651, "Runaway Youth") {

    // Current position
    private var _currentPosition = 0

    init {

        addStartNpc(IVAN)
        addTalkId(IVAN, BATIDAE)

        addSpawn(IVAN, 118600, -161235, -1119, 0, false, 0, false)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("32014-04.htm", ignoreCase = true)) {
            if (st.hasQuestItems(SCROLL_OF_ESCAPE)) {
                htmltext = "32014-03.htm"
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.takeItems(SCROLL_OF_ESCAPE, 1)

                npc!!.broadcastPacket(MagicSkillUse(npc, npc, 2013, 1, 3500, 0))
                startQuestTimer("apparition_npc", 4000, npc, player, false)
            } else
                st.exitQuest(true)
        } else if (event.equals("apparition_npc", ignoreCase = true)) {
            var chance = Rnd[3]

            // Loop to avoid to spawn to the same place.
            while (chance == _currentPosition)
                chance = Rnd[3]

            // Register new position.
            _currentPosition = chance

            npc!!.deleteMe()
            addSpawn(IVAN, SPAWNS[chance], false, 0, false)
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 26) "32014-01.htm" else "32014-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                BATIDAE -> {
                    htmltext = "31989-01.htm"
                    st.rewardItems(57, 2883)
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                }

                IVAN -> htmltext = "32014-04a.htm"
            }
        }

        return htmltext
    }

    companion object {
        private val qn = "Q651_RunawayYouth"

        // NPCs
        private val IVAN = 32014
        private val BATIDAE = 31989

        // Item
        private val SCROLL_OF_ESCAPE = 736

        // Table of possible spawns
        private val SPAWNS = arrayOf(
            SpawnLocation(118600, -161235, -1119, 0),
            SpawnLocation(108380, -150268, -2376, 0),
            SpawnLocation(123254, -148126, -3425, 0)
        )
    }
}