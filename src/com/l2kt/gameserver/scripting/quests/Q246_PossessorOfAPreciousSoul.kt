package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q246_PossessorOfAPreciousSoul : Quest(246, "Possessor of a Precious Soul - 3") {
    init {

        setItemsIds(WATERBINDER, EVERGREEN, RAIN_SONG, RELIC_BOX)

        addStartNpc(CARADINE)
        addTalkId(CARADINE, OSSIAN, LADD)

        addKillId(PILGRIM_OF_SPLENDOR, JUDGE_OF_SPLENDOR, BARAKIEL)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext: String? = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // Caradine
        if (event.equals("31740-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.takeItems(CARADINE_LETTER_1, 1)
        } else if (event.equals("31741-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31741-05.htm", ignoreCase = true)) {
            if (st.hasQuestItems(WATERBINDER, EVERGREEN)) {
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(WATERBINDER, 1)
                st.takeItems(EVERGREEN, 1)
            } else
                htmltext = null
        } else if (event.equals("31741-08.htm", ignoreCase = true)) {
            if (st.hasQuestItems(RAIN_SONG)) {
                st["cond"] = "6"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(RAIN_SONG, 1)
                st.giveItems(RELIC_BOX, 1)
            } else
                htmltext = null
        } else if (event.equals("30721-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(RELIC_BOX)) {
                st.takeItems(RELIC_BOX, 1)
                st.giveItems(CARADINE_LETTER_2, 1)
                st.rewardExpAndSp(719843, 0)
                player.broadcastPacket(SocialAction(player, 3))
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = null
        }// Ladd
        // Ossian

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (st.hasQuestItems(CARADINE_LETTER_1))
                htmltext = if (!player.isSubClassActive || player.level < 65) "31740-02.htm" else "31740-01.htm"

            Quest.STATE_STARTED -> run{
                if (!player.isSubClassActive)
                    return@run

                val cond = st.getInt("cond")
                when (npc.npcId) {
                    CARADINE -> if (cond == 1)
                        htmltext = "31740-05.htm"

                    OSSIAN -> if (cond == 1)
                        htmltext = "31741-01.htm"
                    else if (cond == 2)
                        htmltext = "31741-03.htm"
                    else if (cond == 3) {
                        if (st.hasQuestItems(WATERBINDER, EVERGREEN))
                            htmltext = "31741-04.htm"
                    } else if (cond == 4)
                        htmltext = "31741-06.htm"
                    else if (cond == 5) {
                        if (st.hasQuestItems(RAIN_SONG))
                            htmltext = "31741-07.htm"
                    } else if (cond == 6)
                        htmltext = "31741-09.htm"

                    LADD -> if (cond == 6 && st.hasQuestItems(RELIC_BOX))
                        htmltext = "30721-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer
        val npcId = npc.npcId

        if (npcId == BARAKIEL) {
            for (st in getPartyMembers(player, npc, "cond", "4")) {
                if (!st.player.isSubClassActive)
                    continue

                if (!st.hasQuestItems(RAIN_SONG)) {
                    st["cond"] = "5"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(RAIN_SONG, 1)
                }
            }
        } else {
            if (!player!!.isSubClassActive)
                return null

            val st = checkPlayerCondition(player, npc, "cond", "2") ?: return null

            if (Rnd[10] < 2) {
                val neklaceOrRing = if (npcId == PILGRIM_OF_SPLENDOR) WATERBINDER else EVERGREEN

                if (!st.hasQuestItems(neklaceOrRing)) {
                    st.giveItems(neklaceOrRing, 1)

                    if (!st.hasQuestItems(if (npcId == PILGRIM_OF_SPLENDOR) EVERGREEN else WATERBINDER))
                        st.playSound(QuestState.SOUND_ITEMGET)
                    else {
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    }
                }
            }
        }
        return null
    }

    companion object {
        private val qn = "Q246_PossessorOfAPreciousSoul"

        // NPCs
        private val CARADINE = 31740
        private val OSSIAN = 31741
        private val LADD = 30721

        // Items
        private val WATERBINDER = 7591
        private val EVERGREEN = 7592
        private val RAIN_SONG = 7593
        private val RELIC_BOX = 7594
        private val CARADINE_LETTER_1 = 7678
        private val CARADINE_LETTER_2 = 7679

        // Mobs
        private val PILGRIM_OF_SPLENDOR = 21541
        private val JUDGE_OF_SPLENDOR = 21544
        private val BARAKIEL = 25325
    }
}