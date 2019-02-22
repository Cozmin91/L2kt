package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

/**
 * This quest supports both Q611 && Q612 onKill sections.
 */
class Q611_AllianceWithVarkaSilenos : Quest(611, "Alliance with Varka Silenos") {
    init {
        CHANCES[21324] = 500000
        CHANCES[21325] = 500000
        CHANCES[21327] = 509000
        CHANCES[21328] = 521000
        CHANCES[21329] = 519000
        CHANCES[21331] = 500000
        CHANCES[21332] = 500000
        CHANCES[21334] = 509000
        CHANCES[21335] = 518000
        CHANCES[21336] = 518000
        CHANCES[21338] = 527000
        CHANCES[21339] = 500000
        CHANCES[21340] = 500000
        CHANCES[21342] = 508000
        CHANCES[21343] = 628000
        CHANCES[21344] = 604000
        CHANCES[21345] = 627000
        CHANCES[21346] = 604000
        CHANCES[21347] = 649000
        CHANCES[21348] = 626000
        CHANCES[21349] = 626000
    }

    init {
        CHANCES_MOLAR[21324] = 500000
        CHANCES_MOLAR[21327] = 510000
        CHANCES_MOLAR[21328] = 522000
        CHANCES_MOLAR[21329] = 519000
        CHANCES_MOLAR[21331] = 529000
        CHANCES_MOLAR[21332] = 529000
        CHANCES_MOLAR[21334] = 539000
        CHANCES_MOLAR[21336] = 548000
        CHANCES_MOLAR[21338] = 558000
        CHANCES_MOLAR[21339] = 568000
        CHANCES_MOLAR[21340] = 568000
        CHANCES_MOLAR[21342] = 578000
        CHANCES_MOLAR[21343] = 664000
        CHANCES_MOLAR[21345] = 713000
        CHANCES_MOLAR[21347] = 738000
    }

    init {

        setItemsIds(KETRA_BADGE_SOLDIER, KETRA_BADGE_OFFICER, KETRA_BADGE_CAPTAIN)

        addStartNpc(31378) // Naran Ashanuk
        addTalkId(31378)

        for (mobs in CHANCES.keys)
            addKillId(mobs)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31378-03a.htm", ignoreCase = true)) {
            if (player.isAlliedWithKetra)
                htmltext = "31378-02a.htm"
            else {
                st.state = Quest.STATE_STARTED
                st.playSound(QuestState.SOUND_ACCEPT)
                for (i in VARKA_ALLIANCE_1..VARKA_ALLIANCE_5) {
                    if (st.hasQuestItems(i)) {
                        st["cond"] = (i - 7219).toString()
                        player.allianceWithVarkaKetra = 7220 - i
                        return "31378-0" + (i - 7217) + ".htm"
                    }
                }
                st["cond"] = "1"
            }
        } else if (event.equals("31378-10-1.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(KETRA_BADGE_SOLDIER) >= 100) {
                st["cond"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(KETRA_BADGE_SOLDIER, -1)
                st.giveItems(VARKA_ALLIANCE_1, 1)
                player.allianceWithVarkaKetra = -1
            } else
                htmltext = "31378-03b.htm"
        } else if (event.equals("31378-10-2.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(KETRA_BADGE_SOLDIER) >= 200 && st.getQuestItemsCount(KETRA_BADGE_OFFICER) >= 100) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(KETRA_BADGE_SOLDIER, -1)
                st.takeItems(KETRA_BADGE_OFFICER, -1)
                st.takeItems(VARKA_ALLIANCE_1, -1)
                st.giveItems(VARKA_ALLIANCE_2, 1)
                player.allianceWithVarkaKetra = -2
            } else
                htmltext = "31378-12.htm"
        } else if (event.equals("31378-10-3.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(KETRA_BADGE_SOLDIER) >= 300 && st.getQuestItemsCount(KETRA_BADGE_OFFICER) >= 200 && st.getQuestItemsCount(
                    KETRA_BADGE_CAPTAIN
                ) >= 100
            ) {
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(KETRA_BADGE_SOLDIER, -1)
                st.takeItems(KETRA_BADGE_OFFICER, -1)
                st.takeItems(KETRA_BADGE_CAPTAIN, -1)
                st.takeItems(VARKA_ALLIANCE_2, -1)
                st.giveItems(VARKA_ALLIANCE_3, 1)
                player.allianceWithVarkaKetra = -3
            } else
                htmltext = "31378-15.htm"
        } else if (event.equals("31378-10-4.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(KETRA_BADGE_SOLDIER) >= 300 && st.getQuestItemsCount(KETRA_BADGE_OFFICER) >= 300 && st.getQuestItemsCount(
                    KETRA_BADGE_CAPTAIN
                ) >= 200 && st.getQuestItemsCount(VALOR_FEATHER) >= 1
            ) {
                st["cond"] = "5"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(KETRA_BADGE_SOLDIER, -1)
                st.takeItems(KETRA_BADGE_OFFICER, -1)
                st.takeItems(KETRA_BADGE_CAPTAIN, -1)
                st.takeItems(VALOR_FEATHER, -1)
                st.takeItems(VARKA_ALLIANCE_3, -1)
                st.giveItems(VARKA_ALLIANCE_4, 1)
                player.allianceWithVarkaKetra = -4
            } else
                htmltext = "31378-21.htm"
        } else if (event.equals("31378-20.htm", ignoreCase = true)) {
            st.takeItems(VARKA_ALLIANCE_1, -1)
            st.takeItems(VARKA_ALLIANCE_2, -1)
            st.takeItems(VARKA_ALLIANCE_3, -1)
            st.takeItems(VARKA_ALLIANCE_4, -1)
            st.takeItems(VARKA_ALLIANCE_5, -1)
            st.takeItems(VALOR_FEATHER, -1)
            st.takeItems(WISDOM_FEATHER, -1)
            player.allianceWithVarkaKetra = 0
            st.exitQuest(true)
        }// Leave quest
        // Stage 4
        // Stage 3
        // Stage 2
        // Stage 1

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level >= 74)
                htmltext = "31378-01.htm"
            else {
                htmltext = "31378-02b.htm"
                st.exitQuest(true)
                player.allianceWithVarkaKetra = 0
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1) {
                    if (st.getQuestItemsCount(KETRA_BADGE_SOLDIER) < 100)
                        htmltext = "31378-03b.htm"
                    else
                        htmltext = "31378-09.htm"
                } else if (cond == 2) {
                    if (st.getQuestItemsCount(KETRA_BADGE_SOLDIER) < 200 || st.getQuestItemsCount(KETRA_BADGE_OFFICER) < 100)
                        htmltext = "31378-12.htm"
                    else
                        htmltext = "31378-13.htm"
                } else if (cond == 3) {
                    if (st.getQuestItemsCount(KETRA_BADGE_SOLDIER) < 300 || st.getQuestItemsCount(KETRA_BADGE_OFFICER) < 200 || st.getQuestItemsCount(
                            KETRA_BADGE_CAPTAIN
                        ) < 100
                    )
                        htmltext = "31378-15.htm"
                    else
                        htmltext = "31378-16.htm"
                } else if (cond == 4) {
                    if (st.getQuestItemsCount(KETRA_BADGE_SOLDIER) < 300 || st.getQuestItemsCount(KETRA_BADGE_OFFICER) < 300 || st.getQuestItemsCount(
                            KETRA_BADGE_CAPTAIN
                        ) < 200 || !st.hasQuestItems(VALOR_FEATHER)
                    )
                        htmltext = "31378-21.htm"
                    else
                        htmltext = "31378-22.htm"
                } else if (cond == 5) {
                    if (st.getQuestItemsCount(KETRA_BADGE_SOLDIER) < 400 || st.getQuestItemsCount(KETRA_BADGE_OFFICER) < 400 || st.getQuestItemsCount(
                            KETRA_BADGE_CAPTAIN
                        ) < 200 || !st.hasQuestItems(WISDOM_FEATHER)
                    )
                        htmltext = "31378-17.htm"
                    else {
                        htmltext = "31378-10-5.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(KETRA_BADGE_SOLDIER, 400)
                        st.takeItems(KETRA_BADGE_OFFICER, 400)
                        st.takeItems(KETRA_BADGE_CAPTAIN, 200)
                        st.takeItems(WISDOM_FEATHER, -1)
                        st.takeItems(VARKA_ALLIANCE_4, -1)
                        st.giveItems(VARKA_ALLIANCE_5, 1)
                        player.allianceWithVarkaKetra = -5
                    }
                } else if (cond == 6)
                    htmltext = "31378-08.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId

        // Support for Q612.
        val st2 = st.player.getQuestState(qn2)
        if (st2 != null && Rnd.nextBoolean() && CHANCES_MOLAR.containsKey(npcId)) {
            st2.dropItems(MOLAR_OF_KETRA_ORC, 1, 0, CHANCES_MOLAR[npcId]!!)
            return null
        }

        val cond = st.getInt("cond")
        if (cond == 6)
            return null

        if(CHANCES[npcId] == null) {
            return null
        }

        when (npcId) {
            21324, 21325, 21327, 21328, 21329 -> if (cond == 1)
                st.dropItems(KETRA_BADGE_SOLDIER, 1, 100, CHANCES[npcId]!!)
            else if (cond == 2)
                st.dropItems(KETRA_BADGE_SOLDIER, 1, 200, CHANCES[npcId]!!)
            else if (cond == 3 || cond == 4)
                st.dropItems(KETRA_BADGE_SOLDIER, 1, 300, CHANCES[npcId]!!)
            else if (cond == 5)
                st.dropItems(KETRA_BADGE_SOLDIER, 1, 400, CHANCES[npcId]!!)

            21331, 21332, 21334, 21335, 21336, 21338, 21343, 21344 -> if (cond == 2)
                st.dropItems(KETRA_BADGE_OFFICER, 1, 100, CHANCES[npcId]!!)
            else if (cond == 3)
                st.dropItems(KETRA_BADGE_OFFICER, 1, 200, CHANCES[npcId]!!)
            else if (cond == 4)
                st.dropItems(KETRA_BADGE_OFFICER, 1, 300, CHANCES[npcId]!!)
            else if (cond == 5)
                st.dropItems(KETRA_BADGE_OFFICER, 1, 400, CHANCES[npcId]!!)

            21339, 21340, 21342, 21345, 21346, 21347, 21348, 21349 -> if (cond == 3)
                st.dropItems(KETRA_BADGE_CAPTAIN, 1, 100, CHANCES[npcId]!!)
            else if (cond == 4 || cond == 5)
                st.dropItems(KETRA_BADGE_CAPTAIN, 1, 200, CHANCES[npcId]!!)
        }

        return null
    }

    companion object {
        private val qn = "Q611_AllianceWithVarkaSilenos"
        private val qn2 = "Q612_WarWithKetraOrcs"

        private val CHANCES = HashMap<Int, Int>()

        private val CHANCES_MOLAR = HashMap<Int, Int>()

        // Quest Items
        private val KETRA_BADGE_SOLDIER = 7226
        private val KETRA_BADGE_OFFICER = 7227
        private val KETRA_BADGE_CAPTAIN = 7228

        private val VARKA_ALLIANCE_1 = 7221
        private val VARKA_ALLIANCE_2 = 7222
        private val VARKA_ALLIANCE_3 = 7223
        private val VARKA_ALLIANCE_4 = 7224
        private val VARKA_ALLIANCE_5 = 7225

        private val VALOR_FEATHER = 7229
        private val WISDOM_FEATHER = 7230

        private val MOLAR_OF_KETRA_ORC = 7234
    }
}