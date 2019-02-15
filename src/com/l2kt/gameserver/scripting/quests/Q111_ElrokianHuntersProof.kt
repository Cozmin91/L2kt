package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q111_ElrokianHuntersProof : Quest(111, "Elrokian Hunter's Proof") {
    init {

        setItemsIds(FRAGMENT, EXPEDITION_LETTER, CLAW, BONE, SKIN, PRACTICE_TRAP)

        addStartNpc(MARQUEZ)
        addTalkId(MARQUEZ, MUSHIKA, ASAMAH, KIRIKASHIN)

        addKillId(
            22196,
            22197,
            22198,
            22218,
            22200,
            22201,
            22202,
            22219,
            22208,
            22209,
            22210,
            22221,
            22203,
            22204,
            22205,
            22220
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("32113-002.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32115-002.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32113-009.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32113-018.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(FRAGMENT, -1)
            st.giveItems(EXPEDITION_LETTER, 1)
        } else if (event.equals("32116-003.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound("EtcSound.elcroki_song_full")
        } else if (event.equals("32116-005.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32115-004.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32115-006.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32116-007.htm", ignoreCase = true)) {
            st.takeItems(PRACTICE_TRAP, 1)
            st.giveItems(8763, 1)
            st.giveItems(8764, 100)
            st.rewardItems(57, 1022636)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 75) "32113-000.htm" else "32113-001.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    MARQUEZ -> if (cond == 1 || cond == 2)
                        htmltext = "32113-002.htm"
                    else if (cond == 3)
                        htmltext = "32113-003.htm"
                    else if (cond == 4)
                        htmltext = "32113-009.htm"
                    else if (cond == 5)
                        htmltext = "32113-010.htm"

                    MUSHIKA -> {
                        if (cond == 1) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                        htmltext = "32114-001.htm"
                    }

                    ASAMAH -> if (cond == 2)
                        htmltext = "32115-001.htm"
                    else if (cond == 3)
                        htmltext = "32115-002.htm"
                    else if (cond == 8)
                        htmltext = "32115-003.htm"
                    else if (cond == 9)
                        htmltext = "32115-004.htm"
                    else if (cond == 10)
                        htmltext = "32115-006.htm"
                    else if (cond == 11) {
                        htmltext = "32115-007.htm"
                        st["cond"] = "12"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BONE, -1)
                        st.takeItems(CLAW, -1)
                        st.takeItems(SKIN, -1)
                        st.giveItems(PRACTICE_TRAP, 1)
                    }

                    KIRIKASHIN -> if (cond < 6)
                        htmltext = "32116-008.htm"
                    else if (cond == 6) {
                        htmltext = "32116-001.htm"
                        st.takeItems(EXPEDITION_LETTER, 1)
                    } else if (cond == 7)
                        htmltext = "32116-004.htm"
                    else if (cond == 12)
                        htmltext = "32116-006.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            22196, 22197, 22198, 22218 -> if (st.getInt("cond") == 4 && st.dropItems(FRAGMENT, 1, 50, 250000))
                st["cond"] = "5"

            22200, 22201, 22202, 22219 -> if (st.getInt("cond") == 10 && st.dropItems(CLAW, 1, 10, 650000))
                if (st.getQuestItemsCount(BONE) >= 10 && st.getQuestItemsCount(SKIN) >= 10)
                    st["cond"] = "11"

            22208, 22209, 22210, 22221 -> if (st.getInt("cond") == 10 && st.dropItems(SKIN, 1, 10, 650000))
                if (st.getQuestItemsCount(CLAW) >= 10 && st.getQuestItemsCount(BONE) >= 10)
                    st["cond"] = "11"

            22203, 22204, 22205, 22220 -> if (st.getInt("cond") == 10 && st.dropItems(BONE, 1, 10, 650000))
                if (st.getQuestItemsCount(CLAW) >= 10 && st.getQuestItemsCount(SKIN) >= 10)
                    st["cond"] = "11"
        }

        return null
    }

    companion object {
        private const val qn = "Q111_ElrokianHuntersProof"

        // NPCs
        private const val MARQUEZ = 32113
        private const val MUSHIKA = 32114
        private const val ASAMAH = 32115
        private const val KIRIKASHIN = 32116

        // Items
        private const val FRAGMENT = 8768
        private const val EXPEDITION_LETTER = 8769
        private const val CLAW = 8770
        private const val BONE = 8771
        private const val SKIN = 8772
        private const val PRACTICE_TRAP = 8773
    }
}