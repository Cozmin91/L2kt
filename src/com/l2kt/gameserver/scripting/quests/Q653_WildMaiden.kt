package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q653_WildMaiden : Quest(653, "Wild Maiden") {

    // Current position
    private var _currentPosition = 0

    init {

        addStartNpc(SUKI)
        addTalkId(SUKI, GALIBREDO)

        addSpawn(SUKI, 66578, 72351, -3731, 0, false, 0, false)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("32013-03.htm", ignoreCase = true)) {
            if (st.hasQuestItems(SCROLL_OF_ESCAPE)) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.takeItems(SCROLL_OF_ESCAPE, 1)

                npc!!.broadcastPacket(MagicSkillUse(npc, npc, 2013, 1, 3500, 0))
                startQuestTimer("apparition_npc", 4000, npc, player, false)
            } else {
                htmltext = "32013-03a.htm"
                st.exitQuest(true)
            }
        } else if (event.equals("apparition_npc", ignoreCase = true)) {
            var chance = Rnd[4]

            // Loop to avoid to spawn to the same place.
            while (chance == _currentPosition)
                chance = Rnd[4]

            // Register new position.
            _currentPosition = chance

            npc!!.deleteMe()
            addSpawn(SUKI, SPAWNS[chance], false, 0, false)
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 36) "32013-01.htm" else "32013-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                GALIBREDO -> {
                    htmltext = "30181-01.htm"
                    st.rewardItems(57, 2883)
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                }

                SUKI -> htmltext = "32013-04a.htm"
            }
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q653_WildMaiden"

        // NPCs
        private const val SUKI = 32013
        private const val GALIBREDO = 30181

        // Item
        private const val SCROLL_OF_ESCAPE = 736

        // Table of possible spawns
        private val SPAWNS = arrayOf(
            SpawnLocation(66578, 72351, -3731, 0),
            SpawnLocation(77189, 73610, -3708, 2555),
            SpawnLocation(71809, 67377, -3675, 29130),
            SpawnLocation(69166, 88825, -3447, 43886)
        )
    }
}