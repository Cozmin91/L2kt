package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q407_PathToAnElvenScout : Quest(407, "Path to an Elven Scout") {
    init {

        setItemsIds(
            REISA_LETTER,
            PRIAS_TORN_LETTER_1,
            PRIAS_TORN_LETTER_2,
            PRIAS_TORN_LETTER_3,
            PRIAS_TORN_LETTER_4,
            MORETTI_HERB,
            MORETTI_LETTER,
            PRIAS_LETTER,
            HONORARY_GUARD,
            RUSTED_KEY
        )

        addStartNpc(REISA)
        addTalkId(REISA, MORETTI, BABENCO, PRIAS)

        addKillId(20053, 27031)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30328-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.ELVEN_FIGHTER)
                htmltext = if (player.classId == ClassId.ELVEN_SCOUT) "30328-02a.htm" else "30328-02.htm"
            else if (player.level < 19)
                htmltext = "30328-03.htm"
            else if (st.hasQuestItems(REISA_RECOMMENDATION))
                htmltext = "30328-04.htm"
            else {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.giveItems(REISA_LETTER, 1)
            }
        } else if (event.equals("30337-03.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(REISA_LETTER, -1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30328-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    REISA -> if (cond == 1)
                        htmltext = "30328-06.htm"
                    else if (cond > 1 && cond < 8)
                        htmltext = "30328-08.htm"
                    else if (cond == 8) {
                        htmltext = "30328-07.htm"
                        st.takeItems(HONORARY_GUARD, -1)
                        st.giveItems(REISA_RECOMMENDATION, 1)
                        st.rewardExpAndSp(3200, 1000)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    MORETTI -> if (cond == 1)
                        htmltext = "30337-01.htm"
                    else if (cond == 2)
                        htmltext = if (!st.hasQuestItems(PRIAS_TORN_LETTER_1)) "30337-04.htm" else "30337-05.htm"
                    else if (cond == 3) {
                        htmltext = "30337-06.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(PRIAS_TORN_LETTER_1, -1)
                        st.takeItems(PRIAS_TORN_LETTER_2, -1)
                        st.takeItems(PRIAS_TORN_LETTER_3, -1)
                        st.takeItems(PRIAS_TORN_LETTER_4, -1)
                        st.giveItems(MORETTI_HERB, 1)
                        st.giveItems(MORETTI_LETTER, 1)
                    } else if (cond > 3 && cond < 7)
                        htmltext = "30337-09.htm"
                    else if (cond == 7 && st.hasQuestItems(PRIAS_LETTER)) {
                        htmltext = "30337-07.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(PRIAS_LETTER, -1)
                        st.giveItems(HONORARY_GUARD, 1)
                    } else if (cond == 8)
                        htmltext = "30337-08.htm"

                    BABENCO -> if (cond == 2)
                        htmltext = "30334-01.htm"

                    PRIAS -> if (cond == 4) {
                        htmltext = "30426-01.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 5)
                        htmltext = "30426-01.htm"
                    else if (cond == 6) {
                        htmltext = "30426-02.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(RUSTED_KEY, -1)
                        st.takeItems(MORETTI_HERB, -1)
                        st.takeItems(MORETTI_LETTER, -1)
                        st.giveItems(PRIAS_LETTER, 1)
                    } else if (cond == 7)
                        htmltext = "30426-04.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")
        if (npc.npcId == 20053) {
            if (cond == 2) {
                if (!st.hasQuestItems(PRIAS_TORN_LETTER_1)) {
                    st.playSound(QuestState.SOUND_ITEMGET)
                    st.giveItems(PRIAS_TORN_LETTER_1, 1)
                } else if (!st.hasQuestItems(PRIAS_TORN_LETTER_2)) {
                    st.playSound(QuestState.SOUND_ITEMGET)
                    st.giveItems(PRIAS_TORN_LETTER_2, 1)
                } else if (!st.hasQuestItems(PRIAS_TORN_LETTER_3)) {
                    st.playSound(QuestState.SOUND_ITEMGET)
                    st.giveItems(PRIAS_TORN_LETTER_3, 1)
                } else if (!st.hasQuestItems(PRIAS_TORN_LETTER_4)) {
                    st["cond"] = "3"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(PRIAS_TORN_LETTER_4, 1)
                }
            }
        } else if ((cond == 4 || cond == 5) && st.dropItems(RUSTED_KEY, 1, 1, 600000))
            st["cond"] = "6"

        return null
    }

    companion object {
        private val qn = "Q407_PathToAnElvenScout"

        // Items
        private val REISA_LETTER = 1207
        private val PRIAS_TORN_LETTER_1 = 1208
        private val PRIAS_TORN_LETTER_2 = 1209
        private val PRIAS_TORN_LETTER_3 = 1210
        private val PRIAS_TORN_LETTER_4 = 1211
        private val MORETTI_HERB = 1212
        private val MORETTI_LETTER = 1214
        private val PRIAS_LETTER = 1215
        private val HONORARY_GUARD = 1216
        private val REISA_RECOMMENDATION = 1217
        private val RUSTED_KEY = 1293

        // NPCs
        private val REISA = 30328
        private val BABENCO = 30334
        private val MORETTI = 30337
        private val PRIAS = 30426
    }
}