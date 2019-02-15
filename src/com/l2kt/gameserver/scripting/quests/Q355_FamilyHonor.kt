package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q355_FamilyHonor : Quest(355, "Family Honor") {
    init {
        CHANCES[TIMAK_ORC_TROOP_LEADER] = intArrayOf(44, 54)
        CHANCES[TIMAK_ORC_TROOP_SHAMAN] = intArrayOf(36, 45)
        CHANCES[TIMAK_ORC_TROOP_WARRIOR] = intArrayOf(35, 43)
        CHANCES[TIMAK_ORC_TROOP_ARCHER] = intArrayOf(32, 42)
    }

    init {

        setItemsIds(GALIBREDO_BUST)

        addStartNpc(GALIBREDO)
        addTalkId(GALIBREDO, PATRIN)

        addKillId(TIMAK_ORC_TROOP_LEADER, TIMAK_ORC_TROOP_SHAMAN, TIMAK_ORC_TROOP_WARRIOR, TIMAK_ORC_TROOP_ARCHER)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30181-2.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30181-4b.htm", ignoreCase = true)) {
            val count = st.getQuestItemsCount(GALIBREDO_BUST)
            if (count > 0) {
                htmltext = "30181-4.htm"

                var reward = 2800 + count * 120
                if (count >= 100) {
                    htmltext = "30181-4a.htm"
                    reward += 5000
                }

                st.takeItems(GALIBREDO_BUST, count)
                st.rewardItems(57, reward)
            }
        } else if (event.equals("30929-7.htm", ignoreCase = true)) {
            if (st.hasQuestItems(WORK_OF_BERONA)) {
                st.takeItems(WORK_OF_BERONA, 1)

                val appraising = Rnd[100]
                if (appraising < 20)
                    htmltext = "30929-2.htm"
                else if (appraising < 40) {
                    htmltext = "30929-3.htm"
                    st.giveItems(STATUE_REPLICA, 1)
                } else if (appraising < 60) {
                    htmltext = "30929-4.htm"
                    st.giveItems(STATUE_ORIGINAL, 1)
                } else if (appraising < 80) {
                    htmltext = "30929-5.htm"
                    st.giveItems(STATUE_FORGERY, 1)
                } else {
                    htmltext = "30929-6.htm"
                    st.giveItems(STATUE_PROTOTYPE, 1)
                }
            }
        } else if (event.equals("30181-6.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 36) "30181-0a.htm" else "30181-0.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                GALIBREDO -> htmltext = if (st.hasQuestItems(GALIBREDO_BUST)) "30181-3a.htm" else "30181-3.htm"

                PATRIN -> htmltext = "30929-0.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val chances = CHANCES[npc.npcId] ?: return null
        val random = Rnd[100]

        if (random < chances[1])
            st.dropItemsAlways(if (random < chances[0]) GALIBREDO_BUST else WORK_OF_BERONA, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q355_FamilyHonor"

        // NPCs
        private val GALIBREDO = 30181
        private val PATRIN = 30929

        // Monsters
        private val TIMAK_ORC_TROOP_LEADER = 20767
        private val TIMAK_ORC_TROOP_SHAMAN = 20768
        private val TIMAK_ORC_TROOP_WARRIOR = 20769
        private val TIMAK_ORC_TROOP_ARCHER = 20770

        // Items
        private val GALIBREDO_BUST = 4252
        private val WORK_OF_BERONA = 4350
        private val STATUE_PROTOTYPE = 4351
        private val STATUE_ORIGINAL = 4352
        private val STATUE_REPLICA = 4353
        private val STATUE_FORGERY = 4354

        // Drop chances
        private val CHANCES = HashMap<Int, IntArray>()
    }
}