package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q235_MimirsElixir : Quest(235, "Mimir's Elixir") {
    init {

        setItemsIds(PURE_SILVER, TRUE_GOLD, SAGE_STONE, BLOOD_FIRE, MAGISTER_MIXING_STONE, MIMIR_ELIXIR)

        addStartNpc(LADD)
        addTalkId(LADD, JOAN, MIXING_URN)

        addKillId(20965, 21090)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30721-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30721-12.htm", ignoreCase = true) && st.hasQuestItems(TRUE_GOLD)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(MAGISTER_MIXING_STONE, 1)
        } else if (event.equals("30721-16.htm", ignoreCase = true) && st.hasQuestItems(MIMIR_ELIXIR)) {
            player.broadcastPacket(MagicSkillUse(player, player, 4339, 1, 1, 1))

            st.takeItems(MAGISTER_MIXING_STONE, -1)
            st.takeItems(MIMIR_ELIXIR, -1)
            st.takeItems(STAR_OF_DESTINY, -1)
            st.giveItems(SCROLL_ENCHANT_WEAPON_A, 1)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        } else if (event.equals("30718-03.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31149-02.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(MAGISTER_MIXING_STONE))
                htmltext = "31149-havent.htm"
        } else if (event.equals("31149-03.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(MAGISTER_MIXING_STONE, PURE_SILVER))
                htmltext = "31149-havent.htm"
        } else if (event.equals("31149-05.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(MAGISTER_MIXING_STONE, PURE_SILVER, TRUE_GOLD))
                htmltext = "31149-havent.htm"
        } else if (event.equals("31149-07.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(MAGISTER_MIXING_STONE, PURE_SILVER, TRUE_GOLD, BLOOD_FIRE))
                htmltext = "31149-havent.htm"
        } else if (event.equals("31149-success.htm", ignoreCase = true)) {
            if (st.hasQuestItems(MAGISTER_MIXING_STONE, PURE_SILVER, TRUE_GOLD, BLOOD_FIRE)) {
                st["cond"] = "8"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(PURE_SILVER, -1)
                st.takeItems(TRUE_GOLD, -1)
                st.takeItems(BLOOD_FIRE, -1)
                st.giveItems(MIMIR_ELIXIR, 1)
            } else
                htmltext = "31149-havent.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 75)
                htmltext = "30721-01b.htm"
            else if (!st.hasQuestItems(STAR_OF_DESTINY))
                htmltext = "30721-01a.htm"
            else
                htmltext = "30721-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    LADD -> if (cond == 1) {
                        if (st.hasQuestItems(PURE_SILVER)) {
                            htmltext = "30721-08.htm"
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            htmltext = "30721-07.htm"
                    } else if (cond < 5)
                        htmltext = "30721-10.htm"
                    else if (cond == 5 && st.hasQuestItems(TRUE_GOLD))
                        htmltext = "30721-11.htm"
                    else if (cond == 6 || cond == 7)
                        htmltext = "30721-13.htm"
                    else if (cond == 8 && st.hasQuestItems(MIMIR_ELIXIR))
                        htmltext = "30721-14.htm"

                    JOAN -> if (cond == 2)
                        htmltext = "30718-01.htm"
                    else if (cond == 3)
                        htmltext = "30718-04.htm"
                    else if (cond == 4 && st.hasQuestItems(SAGE_STONE)) {
                        htmltext = "30718-05.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SAGE_STONE, -1)
                        st.giveItems(TRUE_GOLD, 1)
                    } else if (cond > 4)
                        htmltext = "30718-06.htm"

                    // The urn gives the same first htm. Bypasses' events will do all the job.
                    MIXING_URN -> htmltext = "31149-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20965 -> if (st.getInt("cond") == 3 && st.dropItems(SAGE_STONE, 1, 1, 200000))
                st["cond"] = "4"

            21090 -> if (st.getInt("cond") == 6 && st.dropItems(BLOOD_FIRE, 1, 1, 200000))
                st["cond"] = "7"
        }

        return null
    }

    companion object {
        private val qn = "Q235_MimirsElixir"

        // Items
        private val STAR_OF_DESTINY = 5011
        private val PURE_SILVER = 6320
        private val TRUE_GOLD = 6321
        private val SAGE_STONE = 6322
        private val BLOOD_FIRE = 6318
        private val MIMIR_ELIXIR = 6319
        private val MAGISTER_MIXING_STONE = 5905

        // Reward
        private val SCROLL_ENCHANT_WEAPON_A = 729

        // NPCs
        private val JOAN = 30718
        private val LADD = 30721
        private val MIXING_URN = 31149
    }
}