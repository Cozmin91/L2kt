package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

/**
 * This quest supports both Q605 && Q606 onKill sections.
 */
class Q605_AllianceWithKetraOrcs : Quest(605, "Alliance with Ketra Orcs") {
    init {
        CHANCES[21350] = 500000
        CHANCES[21351] = 500000
        CHANCES[21353] = 509000
        CHANCES[21354] = 521000
        CHANCES[21355] = 519000
        CHANCES[21357] = 500000
        CHANCES[21358] = 500000
        CHANCES[21360] = 509000
        CHANCES[21361] = 518000
        CHANCES[21362] = 518000
        CHANCES[21364] = 527000
        CHANCES[21365] = 500000
        CHANCES[21366] = 500000
        CHANCES[21368] = 508000
        CHANCES[21369] = 628000
        CHANCES[21370] = 604000
        CHANCES[21371] = 627000
        CHANCES[21372] = 604000
        CHANCES[21373] = 649000
        CHANCES[21374] = 626000
        CHANCES[21375] = 626000
    }

    init {
        CHANCES_MANE[21350] = 500000
        CHANCES_MANE[21353] = 510000
        CHANCES_MANE[21354] = 522000
        CHANCES_MANE[21355] = 519000
        CHANCES_MANE[21357] = 529000
        CHANCES_MANE[21358] = 529000
        CHANCES_MANE[21360] = 539000
        CHANCES_MANE[21362] = 548000
        CHANCES_MANE[21364] = 558000
        CHANCES_MANE[21365] = 568000
        CHANCES_MANE[21366] = 568000
        CHANCES_MANE[21368] = 568000
        CHANCES_MANE[21369] = 664000
        CHANCES_MANE[21371] = 713000
        CHANCES_MANE[21373] = 738000
    }

    init {

        setItemsIds(VARKA_BADGE_SOLDIER, VARKA_BADGE_OFFICER, VARKA_BADGE_CAPTAIN)

        addStartNpc(31371) // Wahkan
        addTalkId(31371)

        for (mobs in CHANCES.keys)
            addKillId(mobs)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31371-03a.htm", ignoreCase = true)) {
            if (player.isAlliedWithVarka)
                htmltext = "31371-02a.htm"
            else {
                st.state = Quest.STATE_STARTED
                st.playSound(QuestState.SOUND_ACCEPT)
                for (i in KETRA_ALLIANCE_1..KETRA_ALLIANCE_5) {
                    if (st.hasQuestItems(i)) {
                        st["cond"] = (i - 7209).toString()
                        player.allianceWithVarkaKetra = i - 7210
                        return "31371-0" + (i - 7207) + ".htm"
                    }
                }
                st["cond"] = "1"
            }
        } else if (event.equals("31371-10-1.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(VARKA_BADGE_SOLDIER) >= 100) {
                st["cond"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(VARKA_BADGE_SOLDIER, -1)
                st.giveItems(KETRA_ALLIANCE_1, 1)
                player.allianceWithVarkaKetra = 1
            } else
                htmltext = "31371-03b.htm"
        } else if (event.equals("31371-10-2.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(VARKA_BADGE_SOLDIER) >= 200 && st.getQuestItemsCount(VARKA_BADGE_OFFICER) >= 100) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(VARKA_BADGE_SOLDIER, -1)
                st.takeItems(VARKA_BADGE_OFFICER, -1)
                st.takeItems(KETRA_ALLIANCE_1, -1)
                st.giveItems(KETRA_ALLIANCE_2, 1)
                player.allianceWithVarkaKetra = 2
            } else
                htmltext = "31371-12.htm"
        } else if (event.equals("31371-10-3.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(VARKA_BADGE_SOLDIER) >= 300 && st.getQuestItemsCount(VARKA_BADGE_OFFICER) >= 200 && st.getQuestItemsCount(
                    VARKA_BADGE_CAPTAIN
                ) >= 100
            ) {
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(VARKA_BADGE_SOLDIER, -1)
                st.takeItems(VARKA_BADGE_OFFICER, -1)
                st.takeItems(VARKA_BADGE_CAPTAIN, -1)
                st.takeItems(KETRA_ALLIANCE_2, -1)
                st.giveItems(KETRA_ALLIANCE_3, 1)
                player.allianceWithVarkaKetra = 3
            } else
                htmltext = "31371-15.htm"
        } else if (event.equals("31371-10-4.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(VARKA_BADGE_SOLDIER) >= 300 && st.getQuestItemsCount(VARKA_BADGE_OFFICER) >= 300 && st.getQuestItemsCount(
                    VARKA_BADGE_CAPTAIN
                ) >= 200 && st.getQuestItemsCount(TOTEM_OF_VALOR) >= 1
            ) {
                st["cond"] = "5"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(VARKA_BADGE_SOLDIER, -1)
                st.takeItems(VARKA_BADGE_OFFICER, -1)
                st.takeItems(VARKA_BADGE_CAPTAIN, -1)
                st.takeItems(TOTEM_OF_VALOR, -1)
                st.takeItems(KETRA_ALLIANCE_3, -1)
                st.giveItems(KETRA_ALLIANCE_4, 1)
                player.allianceWithVarkaKetra = 4
            } else
                htmltext = "31371-21.htm"
        } else if (event.equals("31371-20.htm", ignoreCase = true)) {
            st.takeItems(KETRA_ALLIANCE_1, -1)
            st.takeItems(KETRA_ALLIANCE_2, -1)
            st.takeItems(KETRA_ALLIANCE_3, -1)
            st.takeItems(KETRA_ALLIANCE_4, -1)
            st.takeItems(KETRA_ALLIANCE_5, -1)
            st.takeItems(TOTEM_OF_VALOR, -1)
            st.takeItems(TOTEM_OF_WISDOM, -1)
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
                htmltext = "31371-01.htm"
            else {
                htmltext = "31371-02b.htm"
                st.exitQuest(true)
                player.allianceWithVarkaKetra = 0
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1) {
                    if (st.getQuestItemsCount(VARKA_BADGE_SOLDIER) < 100)
                        htmltext = "31371-03b.htm"
                    else
                        htmltext = "31371-09.htm"
                } else if (cond == 2) {
                    if (st.getQuestItemsCount(VARKA_BADGE_SOLDIER) < 200 || st.getQuestItemsCount(VARKA_BADGE_OFFICER) < 100)
                        htmltext = "31371-12.htm"
                    else
                        htmltext = "31371-13.htm"
                } else if (cond == 3) {
                    if (st.getQuestItemsCount(VARKA_BADGE_SOLDIER) < 300 || st.getQuestItemsCount(VARKA_BADGE_OFFICER) < 200 || st.getQuestItemsCount(
                            VARKA_BADGE_CAPTAIN
                        ) < 100
                    )
                        htmltext = "31371-15.htm"
                    else
                        htmltext = "31371-16.htm"
                } else if (cond == 4) {
                    if (st.getQuestItemsCount(VARKA_BADGE_SOLDIER) < 300 || st.getQuestItemsCount(VARKA_BADGE_OFFICER) < 300 || st.getQuestItemsCount(
                            VARKA_BADGE_CAPTAIN
                        ) < 200 || !st.hasQuestItems(TOTEM_OF_VALOR)
                    )
                        htmltext = "31371-21.htm"
                    else
                        htmltext = "31371-22.htm"
                } else if (cond == 5) {
                    if (st.getQuestItemsCount(VARKA_BADGE_SOLDIER) < 400 || st.getQuestItemsCount(VARKA_BADGE_OFFICER) < 400 || st.getQuestItemsCount(
                            VARKA_BADGE_CAPTAIN
                        ) < 200 || !st.hasQuestItems(TOTEM_OF_WISDOM)
                    )
                        htmltext = "31371-17.htm"
                    else {
                        htmltext = "31371-10-5.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(VARKA_BADGE_SOLDIER, 400)
                        st.takeItems(VARKA_BADGE_OFFICER, 400)
                        st.takeItems(VARKA_BADGE_CAPTAIN, 200)
                        st.takeItems(TOTEM_OF_WISDOM, -1)
                        st.takeItems(KETRA_ALLIANCE_4, -1)
                        st.giveItems(KETRA_ALLIANCE_5, 1)
                        player.allianceWithVarkaKetra = 5
                    }
                } else if (cond == 6)
                    htmltext = "31371-08.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId

        // Support for Q606.
        val st2 = st.player.getQuestState(qn2)
        if (st2 != null && Rnd.nextBoolean() && CHANCES_MANE.containsKey(npcId)) {
            st2.dropItems(VARKA_MANE, 1, 0, CHANCES_MANE[npcId]!!)
            return null
        }

        val cond = st.getInt("cond")
        if (cond == 6)
            return null

        when (npcId) {
            21350, 21351, 21353, 21354, 21355 -> if (cond == 1)
                st.dropItems(VARKA_BADGE_SOLDIER, 1, 100, CHANCES[npcId]!!)
            else if (cond == 2)
                st.dropItems(VARKA_BADGE_SOLDIER, 1, 200, CHANCES[npcId]!!)
            else if (cond == 3 || cond == 4)
                st.dropItems(VARKA_BADGE_SOLDIER, 1, 300, CHANCES[npcId]!!)
            else if (cond == 5)
                st.dropItems(VARKA_BADGE_SOLDIER, 1, 400, CHANCES[npcId]!!)

            21357, 21358, 21360, 21361, 21362, 21364, 21369, 21370 -> if (cond == 2)
                st.dropItems(VARKA_BADGE_OFFICER, 1, 100, CHANCES[npcId]!!)
            else if (cond == 3)
                st.dropItems(VARKA_BADGE_OFFICER, 1, 200, CHANCES[npcId]!!)
            else if (cond == 4)
                st.dropItems(VARKA_BADGE_OFFICER, 1, 300, CHANCES[npcId]!!)
            else if (cond == 5)
                st.dropItems(VARKA_BADGE_OFFICER, 1, 400, CHANCES[npcId]!!)

            21365, 21366, 21368, 21371, 21372, 21373, 21374, 21375 -> if (cond == 3)
                st.dropItems(VARKA_BADGE_CAPTAIN, 1, 100, CHANCES[npcId]!!)
            else if (cond == 4 || cond == 5)
                st.dropItems(VARKA_BADGE_CAPTAIN, 1, 200, CHANCES[npcId]!!)
        }

        return null
    }

    companion object {
        private val qn = "Q605_AllianceWithKetraOrcs"
        private val qn2 = "Q606_WarWithVarkaSilenos"

        private val CHANCES = HashMap<Int, Int>()

        private val CHANCES_MANE = HashMap<Int, Int>()

        // Quest Items
        private val VARKA_BADGE_SOLDIER = 7216
        private val VARKA_BADGE_OFFICER = 7217
        private val VARKA_BADGE_CAPTAIN = 7218

        private val KETRA_ALLIANCE_1 = 7211
        private val KETRA_ALLIANCE_2 = 7212
        private val KETRA_ALLIANCE_3 = 7213
        private val KETRA_ALLIANCE_4 = 7214
        private val KETRA_ALLIANCE_5 = 7215

        private val TOTEM_OF_VALOR = 7219
        private val TOTEM_OF_WISDOM = 7220

        private val VARKA_MANE = 7233
    }
}