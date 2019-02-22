package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q241_PossessorOfAPreciousSoul : Quest(241, "Possessor of a Precious Soul - 1") {
    init {

        setItemsIds(
            LEGEND_OF_SEVENTEEN,
            MALRUK_SUCCUBUS_CLAW,
            ECHO_CRYSTAL,
            POETRY_BOOK,
            CRIMSON_MOSS,
            RAHORAKTI_MEDICINE
        )

        addStartNpc(TALIEN)
        addTalkId(TALIEN, GABRIELLE, GILMORE, KANTABILON, STEDMIEL, VIRGIL, OGMAR, RAHORAKTI, KASSANDRA, CARADINE, NOEL)

        addKillId(
            BARAHAM,
            MALRUK_SUCCUBUS_1,
            MALRUK_SUCCUBUS_2,
            MALRUK_SUCCUBUS_TUREN_1,
            MALRUK_SUCCUBUS_TUREN_2,
            SPLINTER_STAKATO,
            SPLINTER_STAKATO_WALKER,
            SPLINTER_STAKATO_SOLDIER,
            SPLINTER_STAKATO_DRONE_1,
            SPLINTER_STAKATO_DRONE_2
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // Talien
        if (event.equals("31739-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31739-07.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LEGEND_OF_SEVENTEEN, 1)
        } else if (event.equals("31739-10.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ECHO_CRYSTAL, 1)
        } else if (event.equals("31739-13.htm", ignoreCase = true)) {
            st["cond"] = "11"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(POETRY_BOOK, 1)
        } else if (event.equals("30753-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30754-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31042-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31042-05.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MALRUK_SUCCUBUS_CLAW, -1)
            st.giveItems(ECHO_CRYSTAL, 1)
        } else if (event.equals("30692-02.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(POETRY_BOOK, 1)
        } else if (event.equals("31742-02.htm", ignoreCase = true)) {
            st["cond"] = "12"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31742-05.htm", ignoreCase = true)) {
            st["cond"] = "18"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31744-02.htm", ignoreCase = true)) {
            st["cond"] = "13"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31336-02.htm", ignoreCase = true)) {
            st["cond"] = "14"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31336-05.htm", ignoreCase = true)) {
            st["cond"] = "16"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRIMSON_MOSS, -1)
            st.giveItems(RAHORAKTI_MEDICINE, 1)
        } else if (event.equals("31743-02.htm", ignoreCase = true)) {
            st["cond"] = "17"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(RAHORAKTI_MEDICINE, 1)
        } else if (event.equals("31740-02.htm", ignoreCase = true)) {
            st["cond"] = "19"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31740-05.htm", ignoreCase = true)) {
            st.giveItems(VIRGIL_LETTER, 1)
            st.rewardExpAndSp(263043, 0)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        } else if (event.equals("31272-02.htm", ignoreCase = true)) {
            st["cond"] = "20"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31272-05.htm", ignoreCase = true)) {
            if (st.hasQuestItems(HELLFIRE_OIL) && st.getQuestItemsCount(LUNARGENT) >= 5) {
                st["cond"] = "21"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(LUNARGENT, 5)
                st.takeItems(HELLFIRE_OIL, 1)
            } else
                htmltext = "31272-07.htm"
        }// Noel
        // Caradine
        // Kassandra
        // Rahorakti
        // Ogmar
        // Virgil
        // Stedmiel
        // Kantabilon
        // Gilmore
        // Gabrielle
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            STATE_CREATED -> htmltext =
                    if (!player.isSubClassActive || player.level < 50) "31739-02.htm" else "31739-01.htm"

            STATE_STARTED -> run{
                if (!player.isSubClassActive)
                    return@run

                val cond = st.getInt("cond")
                when (npc.npcId) {
                    TALIEN -> if (cond == 1)
                        htmltext = "31739-04.htm"
                    else if (cond == 2 || cond == 3)
                        htmltext = "31739-05.htm"
                    else if (cond == 4)
                        htmltext = "31739-06.htm"
                    else if (cond == 5)
                        htmltext = "31739-08.htm"
                    else if (cond == 8)
                        htmltext = "31739-09.htm"
                    else if (cond == 9)
                        htmltext = "31739-11.htm"
                    else if (cond == 10)
                        htmltext = "31739-12.htm"
                    else if (cond == 11)
                        htmltext = "31739-14.htm"

                    GABRIELLE -> if (cond == 1)
                        htmltext = "30753-01.htm"
                    else if (cond == 2)
                        htmltext = "30753-03.htm"

                    GILMORE -> if (cond == 2)
                        htmltext = "30754-01.htm"
                    else if (cond == 3)
                        htmltext = "30754-03.htm"

                    KANTABILON -> if (cond == 5)
                        htmltext = "31042-01.htm"
                    else if (cond == 6)
                        htmltext = "31042-03.htm"
                    else if (cond == 7)
                        htmltext = "31042-04.htm"
                    else if (cond == 8)
                        htmltext = "31042-06.htm"

                    STEDMIEL -> if (cond == 9)
                        htmltext = "30692-01.htm"
                    else if (cond == 10)
                        htmltext = "30692-03.htm"

                    VIRGIL -> if (cond == 11)
                        htmltext = "31742-01.htm"
                    else if (cond == 12)
                        htmltext = "31742-03.htm"
                    else if (cond == 17)
                        htmltext = "31742-04.htm"
                    else if (cond == 18)
                        htmltext = "31742-06.htm"

                    OGMAR -> if (cond == 12)
                        htmltext = "31744-01.htm"
                    else if (cond == 13)
                        htmltext = "31744-03.htm"

                    RAHORAKTI -> if (cond == 13)
                        htmltext = "31336-01.htm"
                    else if (cond == 14)
                        htmltext = "31336-03.htm"
                    else if (cond == 15)
                        htmltext = "31336-04.htm"
                    else if (cond == 16)
                        htmltext = "31336-06.htm"

                    KASSANDRA -> if (cond == 16)
                        htmltext = "31743-01.htm"
                    else if (cond == 17)
                        htmltext = "31743-03.htm"

                    CARADINE -> if (cond == 18)
                        htmltext = "31740-01.htm"
                    else if (cond == 19)
                        htmltext = "31740-03.htm"
                    else if (cond == 21)
                        htmltext = "31740-04.htm"

                    NOEL -> if (cond == 19)
                        htmltext = "31272-01.htm"
                    else if (cond == 20) {
                        if (st.hasQuestItems(HELLFIRE_OIL) && st.getQuestItemsCount(LUNARGENT) >= 5)
                            htmltext = "31272-04.htm"
                        else
                            htmltext = "31272-03.htm"
                    } else if (cond == 21)
                        htmltext = "31272-06.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED)
        if (st == null || !player!!.isSubClassActive)
            return null

        when (npc.npcId) {
            BARAHAM -> if (st.getInt("cond") == 3) {
                st["cond"] = "4"
                st.giveItems(LEGEND_OF_SEVENTEEN, 1)
                st.playSound(QuestState.SOUND_MIDDLE)
            }

            MALRUK_SUCCUBUS_1, MALRUK_SUCCUBUS_2 -> if (st.getInt("cond") == 6 && st.dropItems(
                    MALRUK_SUCCUBUS_CLAW,
                    1,
                    10,
                    100000
                )
            )
                st["cond"] = "7"

            MALRUK_SUCCUBUS_TUREN_1, MALRUK_SUCCUBUS_TUREN_2 -> if (st.getInt("cond") == 6 && st.dropItems(
                    MALRUK_SUCCUBUS_CLAW,
                    1,
                    10,
                    120000
                )
            )
                st["cond"] = "7"

            SPLINTER_STAKATO, SPLINTER_STAKATO_WALKER, SPLINTER_STAKATO_SOLDIER, SPLINTER_STAKATO_DRONE_1, SPLINTER_STAKATO_DRONE_2 -> if (st.getInt(
                    "cond"
                ) == 14 && st.dropItems(CRIMSON_MOSS, 1, 5, 100000)
            )
                st["cond"] = "15"
        }
        return null
    }

    companion object {
        private val qn = "Q241_PossessorOfAPreciousSoul"

        // NPCs
        private val TALIEN = 31739
        private val GABRIELLE = 30753
        private val GILMORE = 30754
        private val KANTABILON = 31042
        private val STEDMIEL = 30692
        private val VIRGIL = 31742
        private val OGMAR = 31744
        private val RAHORAKTI = 31336
        private val KASSANDRA = 31743
        private val CARADINE = 31740
        private val NOEL = 31272

        // Monsters
        private val BARAHAM = 27113
        private val MALRUK_SUCCUBUS_1 = 20244
        private val MALRUK_SUCCUBUS_TUREN_1 = 20245
        private val MALRUK_SUCCUBUS_2 = 20283
        private val MALRUK_SUCCUBUS_TUREN_2 = 20284
        private val SPLINTER_STAKATO = 21508
        private val SPLINTER_STAKATO_WALKER = 21509
        private val SPLINTER_STAKATO_SOLDIER = 21510
        private val SPLINTER_STAKATO_DRONE_1 = 21511
        private val SPLINTER_STAKATO_DRONE_2 = 21512

        // Items
        private val LEGEND_OF_SEVENTEEN = 7587
        private val MALRUK_SUCCUBUS_CLAW = 7597
        private val ECHO_CRYSTAL = 7589
        private val POETRY_BOOK = 7588
        private val CRIMSON_MOSS = 7598
        private val RAHORAKTI_MEDICINE = 7599
        private val LUNARGENT = 6029
        private val HELLFIRE_OIL = 6033
        private val VIRGIL_LETTER = 7677
    }
}