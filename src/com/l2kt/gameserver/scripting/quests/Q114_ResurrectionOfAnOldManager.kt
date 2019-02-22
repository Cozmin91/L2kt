package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.ExShowScreenMessage
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q114_ResurrectionOfAnOldManager : Quest(114, "Resurrection of an Old Manager") {
    init {

        setItemsIds(LETTER, DETECTOR, DETECTOR_2, STARSTONE, STARSTONE_2)

        addStartNpc(YUMI)
        addTalkId(YUMI, WENDY, BOX, STONE, NEWYEAR)

        addKillId(GOLEM)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("32041-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["talk"] = "0"
            st["golemSpawned"] = "0"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32041-06.htm", ignoreCase = true))
            st["talk"] = "1"
        else if (event.equals("32041-07.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st["talk"] = "0"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32041-10.htm", ignoreCase = true)) {
            val choice = st.getInt("choice")

            if (choice == 1)
                htmltext = "32041-10.htm"
            else if (choice == 2)
                htmltext = "32041-10a.htm"
            else if (choice == 3)
                htmltext = "32041-10b.htm"
        } else if (event.equals("32041-11.htm", ignoreCase = true))
            st["talk"] = "1"
        else if (event.equals("32041-18.htm", ignoreCase = true))
            st["talk"] = "2"
        else if (event.equals("32041-20.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st["talk"] = "0"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32041-25.htm", ignoreCase = true)) {
            st["cond"] = "17"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(DETECTOR, 1)
        } else if (event.equals("32041-28.htm", ignoreCase = true)) {
            st["talk"] = "1"
            st.takeItems(DETECTOR_2, 1)
        } else if (event.equals("32041-31.htm", ignoreCase = true)) {
            if (st.getInt("choice") > 1)
                htmltext = "32041-37.htm"
        } else if (event.equals("32041-32.htm", ignoreCase = true)) {
            st["cond"] = "21"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(LETTER, 1)
        } else if (event.equals("32041-36.htm", ignoreCase = true)) {
            st["cond"] = "20"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32046-02.htm", ignoreCase = true)) {
            st["cond"] = "19"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32046-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        } else if (event.equals("32047-01.htm", ignoreCase = true)) {
            val talk = st.getInt("talk")
            val talk1 = st.getInt("talk1")

            if (talk == 1 && talk1 == 1)
                htmltext = "32047-04.htm"
            else if (talk == 2 && talk1 == 2 && st.getInt("talk2") == 2)
                htmltext = "32047-08.htm"
        } else if (event.equals("32047-02.htm", ignoreCase = true)) {
            if (st.getInt("talk") == 0)
                st["talk"] = "1"
        } else if (event.equals("32047-03.htm", ignoreCase = true)) {
            if (st.getInt("talk1") == 0)
                st["talk1"] = "1"
        } else if (event.equals("32047-05.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st["talk"] = "0"
            st["choice"] = "1"
            st.unset("talk1")
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32047-06.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st["talk"] = "0"
            st["choice"] = "2"
            st.unset("talk1")
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32047-07.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st["talk"] = "0"
            st["choice"] = "3"
            st.unset("talk1")
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32047-13.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32047-13a.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32047-15.htm", ignoreCase = true)) {
            if (st.getInt("talk") == 0)
                st["talk"] = "1"
        } else if (event.equals("32047-15a.htm", ignoreCase = true)) {
            if (st.getInt("golemSpawned") == 0) {
                val golem = addSpawn(GOLEM, 96977, -110625, -3322, 0, true, 0, true)
                golem!!.broadcastNpcSay("You, " + player.name + ", you attacked Wendy. Prepare to die!")
                (golem as Attackable).addDamageHate(player, 0, 999)
                golem.ai.setIntention(CtrlIntention.ATTACK, player)

                st["golemSpawned"] = "1"
                startQuestTimer("golemDespawn", 900000, golem, player, false)
            } else
                htmltext = "32047-19a.htm"
        } else if (event.equals("32047-17a.htm", ignoreCase = true)) {
            st["cond"] = "12"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32047-20.htm", ignoreCase = true))
            st["talk"] = "2"
        else if (event.equals("32047-23.htm", ignoreCase = true)) {
            st["cond"] = "13"
            st["talk"] = "0"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32047-25.htm", ignoreCase = true)) {
            st["cond"] = "15"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(STARSTONE, 1)
        } else if (event.equals("32047-30.htm", ignoreCase = true))
            st["talk"] = "2"
        else if (event.equals("32047-33.htm", ignoreCase = true)) {
            val cond = st.getInt("cond")

            if (cond == 7) {
                st["cond"] = "8"
                st["talk"] = "0"
                st.playSound(QuestState.SOUND_MIDDLE)
            } else if (cond == 8) {
                st["cond"] = "9"
                htmltext = "32047-34.htm"
                st.playSound(QuestState.SOUND_MIDDLE)
            }
        } else if (event.equals("32047-34.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32047-38.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(57) >= 3000) {
                st["cond"] = "26"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(57, 3000)
                st.giveItems(STARSTONE_2, 1)
            } else
                htmltext = "32047-39.htm"
        } else if (event.equals("32050-02.htm", ignoreCase = true)) {
            st["talk"] = "1"
            st.playSound("ItemSound.armor_wood_3")
        } else if (event.equals("32050-04.htm", ignoreCase = true)) {
            st["cond"] = "14"
            st["talk"] = "0"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(STARSTONE, 1)
        } else if (event.equals("31961-02.htm", ignoreCase = true)) {
            st["cond"] = "22"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LETTER, 1)
            st.giveItems(STARSTONE_2, 1)
        } else if (event.equals("golemDespawn", ignoreCase = true)) {
            st.unset("golemSpawned")
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
            Quest.STATE_CREATED -> {
                val pavelReq = player.getQuestState("Q121_PavelTheGiant")
                htmltext =
                        if (pavelReq == null || !pavelReq.isCompleted || player.level < 49) "32041-00.htm" else "32041-01.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                val talk = st.getInt("talk")

                when (npc.npcId) {
                    YUMI -> if (cond == 1) {
                        if (talk == 0)
                            htmltext = "32041-02.htm"
                        else
                            htmltext = "32041-06.htm"
                    } else if (cond == 2)
                        htmltext = "32041-08.htm"
                    else if (cond > 2 && cond < 6) {
                        if (talk == 0)
                            htmltext = "32041-09.htm"
                        else if (talk == 1)
                            htmltext = "32041-11.htm"
                        else
                            htmltext = "32041-18.htm"
                    } else if (cond == 6)
                        htmltext = "32041-21.htm"
                    else if (cond == 9 || cond == 12 || cond == 16)
                        htmltext = "32041-22.htm"
                    else if (cond == 17)
                        htmltext = "32041-26.htm"
                    else if (cond == 19) {
                        if (talk == 0)
                            htmltext = "32041-27.htm"
                        else
                            htmltext = "32041-28.htm"
                    } else if (cond == 20)
                        htmltext = "32041-36.htm"
                    else if (cond == 21)
                        htmltext = "32041-33.htm"
                    else if (cond == 22 || cond == 26) {
                        htmltext = "32041-34.htm"
                        st["cond"] = "27"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 27)
                        htmltext = "32041-35.htm"

                    WENDY -> if (cond == 2) {
                        if (talk == 0 && st.getInt("talk1") == 0)
                            htmltext = "32047-01.htm"
                        else if (talk == 1 && st.getInt("talk1") == 1)
                            htmltext = "32047-04.htm"
                    } else if (cond == 3)
                        htmltext = "32047-09.htm"
                    else if (cond == 4 || cond == 5)
                        htmltext = "32047-09a.htm"
                    else if (cond == 6) {
                        val choice = st.getInt("choice")

                        if (choice == 1) {
                            if (talk == 0)
                                htmltext = "32047-10.htm"
                            else if (talk == 1)
                                htmltext = "32047-20.htm"
                        } else if (choice == 2)
                            htmltext = "32047-10a.htm"
                        else if (choice == 3) {
                            if (talk == 0)
                                htmltext = "32047-14.htm"
                            else if (talk == 1)
                                htmltext = "32047-15.htm"
                            else
                                htmltext = "32047-20.htm"
                        }
                    } else if (cond == 7) {
                        if (talk == 0)
                            htmltext = "32047-14.htm"
                        else if (talk == 1)
                            htmltext = "32047-15.htm"
                        else
                            htmltext = "32047-20.htm"
                    } else if (cond == 8)
                        htmltext = "32047-30.htm"
                    else if (cond == 9)
                        htmltext = "32047-27.htm"
                    else if (cond == 10)
                        htmltext = "32047-14a.htm"
                    else if (cond == 11)
                        htmltext = "32047-16a.htm"
                    else if (cond == 12)
                        htmltext = "32047-18a.htm"
                    else if (cond == 13)
                        htmltext = "32047-23.htm"
                    else if (cond == 14)
                        htmltext = "32047-24.htm"
                    else if (cond == 15) {
                        htmltext = "32047-26.htm"
                        st["cond"] = "16"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 16)
                        htmltext = "32047-27.htm"
                    else if (cond == 20)
                        htmltext = "32047-35.htm"
                    else if (cond == 26)
                        htmltext = "32047-40.htm"

                    BOX -> if (cond == 13) {
                        if (talk == 0)
                            htmltext = "32050-01.htm"
                        else
                            htmltext = "32050-03.htm"
                    } else if (cond == 14)
                        htmltext = "32050-05.htm"

                    STONE -> if (st.getInt("cond") == 17) {
                        st["cond"] = "18"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(DETECTOR, 1)
                        st.giveItems(DETECTOR_2, 1)
                        player.sendPacket(
                            ExShowScreenMessage(
                                "The radio signal detector is responding. # A suspicious pile of stones catches your eye.",
                                4500
                            )
                        )
                        return null
                    } else if (cond == 18)
                        htmltext = "32046-01.htm"
                    else if (cond == 19)
                        htmltext = "32046-02.htm"
                    else if (cond == 27)
                        htmltext = "32046-03.htm"

                    NEWYEAR -> if (cond == 21)
                        htmltext = "31961-01.htm"
                    else if (cond == 22)
                        htmltext = "31961-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "10") ?: return null

        npc.broadcastNpcSay("This enemy is far too powerful for me to fight. I must withdraw!")

        st["cond"] = "11"
        st.unset("golemSpawned")
        st.playSound(QuestState.SOUND_MIDDLE)

        return null
    }

    companion object {
        private const val qn = "Q114_ResurrectionOfAnOldManager"

        // NPCs
        private const val NEWYEAR = 31961
        private const val YUMI = 32041
        private const val STONE = 32046
        private const val WENDY = 32047
        private const val BOX = 32050

        // Items
        private const val LETTER = 8288
        private const val DETECTOR = 8090
        private const val DETECTOR_2 = 8091
        private const val STARSTONE = 8287
        private const val STARSTONE_2 = 8289

        // Mobs
        private const val GOLEM = 27318
    }
}