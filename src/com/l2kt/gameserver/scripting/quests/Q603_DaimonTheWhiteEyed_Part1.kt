package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q603_DaimonTheWhiteEyed_Part1 : Quest(603, "Daimon the White-Eyed - Part 1") {
    init {
        CHANCES[CANYON_BANDERSNATCH_SLAVE] = 500000
        CHANCES[BUFFALO_SLAVE] = 519000
        CHANCES[GRENDEL_SLAVE] = 673000
    }

    init {

        setItemsIds(EVIL_SPIRIT_BEADS, BROKEN_CRYSTAL)

        addStartNpc(EYE_OF_ARGOS)
        addTalkId(
            EYE_OF_ARGOS,
            MYSTERIOUS_TABLET_1,
            MYSTERIOUS_TABLET_2,
            MYSTERIOUS_TABLET_3,
            MYSTERIOUS_TABLET_4,
            MYSTERIOUS_TABLET_5
        )

        addKillId(BUFFALO_SLAVE, GRENDEL_SLAVE, CANYON_BANDERSNATCH_SLAVE)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // Eye of Argos
        if (event.equals("31683-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31683-06.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(BROKEN_CRYSTAL) > 4) {
                st["cond"] = "7"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BROKEN_CRYSTAL, -1)
            } else
                htmltext = "31683-07.htm"
        } else if (event.equals("31683-10.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(EVIL_SPIRIT_BEADS) > 199) {
                st.takeItems(EVIL_SPIRIT_BEADS, -1)
                st.giveItems(UNFINISHED_SUMMON_CRYSTAL, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else {
                st["cond"] = "7"
                htmltext = "31683-11.htm"
            }
        } else if (event.equals("31548-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(BROKEN_CRYSTAL, 1)
        } else if (event.equals("31549-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(BROKEN_CRYSTAL, 1)
        } else if (event.equals("31550-02.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(BROKEN_CRYSTAL, 1)
        } else if (event.equals("31551-02.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(BROKEN_CRYSTAL, 1)
        } else if (event.equals("31552-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(BROKEN_CRYSTAL, 1)
        }// Mysterious tablets

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 73) "31683-02.htm" else "31683-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    EYE_OF_ARGOS -> if (cond < 6)
                        htmltext = "31683-04.htm"
                    else if (cond == 6)
                        htmltext = "31683-05.htm"
                    else if (cond == 7)
                        htmltext = "31683-08.htm"
                    else if (cond == 8)
                        htmltext = "31683-09.htm"

                    MYSTERIOUS_TABLET_1 -> if (cond == 1)
                        htmltext = "31548-01.htm"
                    else
                        htmltext = "31548-03.htm"

                    MYSTERIOUS_TABLET_2 -> if (cond == 2)
                        htmltext = "31549-01.htm"
                    else if (cond > 2)
                        htmltext = "31549-03.htm"

                    MYSTERIOUS_TABLET_3 -> if (cond == 3)
                        htmltext = "31550-01.htm"
                    else if (cond > 3)
                        htmltext = "31550-03.htm"

                    MYSTERIOUS_TABLET_4 -> if (cond == 4)
                        htmltext = "31551-01.htm"
                    else if (cond > 4)
                        htmltext = "31551-03.htm"

                    MYSTERIOUS_TABLET_5 -> if (cond == 5)
                        htmltext = "31552-01.htm"
                    else if (cond > 5)
                        htmltext = "31552-03.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "7") ?: return null

        if (st.dropItems(EVIL_SPIRIT_BEADS, 1, 200, CHANCES[npc.npcId] ?: 0))
            st["cond"] = "8"

        return null
    }

    companion object {
        private val qn = "Q603_DaimonTheWhiteEyed_Part1"

        // Items
        private val EVIL_SPIRIT_BEADS = 7190
        private val BROKEN_CRYSTAL = 7191
        private val UNFINISHED_SUMMON_CRYSTAL = 7192

        // NPCs
        private val EYE_OF_ARGOS = 31683
        private val MYSTERIOUS_TABLET_1 = 31548
        private val MYSTERIOUS_TABLET_2 = 31549
        private val MYSTERIOUS_TABLET_3 = 31550
        private val MYSTERIOUS_TABLET_4 = 31551
        private val MYSTERIOUS_TABLET_5 = 31552

        // Monsters
        private val CANYON_BANDERSNATCH_SLAVE = 21297
        private val BUFFALO_SLAVE = 21299
        private val GRENDEL_SLAVE = 21304

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}