package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.UserInfo
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q422_RepentYourSins : Quest(422, "Repent Your Sins") {
    init {

        setItemsIds(
            RATMAN_SCAVENGER_SKULL,
            TUREK_WAR_HOUND_TAIL,
            TYRANT_KINGPIN_HEART,
            TRISALIM_TARANTULA_VENOM_SAC,
            MANUAL_OF_MANACLES,
            PENITENT_MANACLES,
            QITEM_PENITENT_MANACLES
        )

        addStartNpc(BLACK_JUDGE)
        addTalkId(BLACK_JUDGE, KATARI, PIOTUR, CASIAN, JOAN, PUSHKIN)

        addKillId(20039, 20494, 20193, 20561)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("Start", ignoreCase = true)) {
            st["cond"] = "1"
            if (player.level <= 20) {
                htmltext = "30981-03.htm"
                st["cond"] = "2"
            } else if (player.level >= 20 && player.level <= 30) {
                htmltext = "30981-04.htm"
                st["cond"] = "3"
            } else if (player.level >= 30 && player.level <= 40) {
                htmltext = "30981-05.htm"
                st["cond"] = "4"
            } else {
                htmltext = "30981-06.htm"
                st["cond"] = "5"
            }
            st.state = Quest.STATE_STARTED
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30981-11.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(PENITENT_MANACLES)) {
                val cond = st.getInt("cond")

                // Case you return back the qitem to Black Judge. She rewards you with the pet item.
                if (cond == 15) {
                    st["cond"] = "16"
                    st["level"] = player.level.toString()
                    st.playSound(QuestState.SOUND_ITEMGET)
                    st.takeItems(QITEM_PENITENT_MANACLES, -1)
                    st.giveItems(PENITENT_MANACLES, 1)
                } else if (cond == 16) {
                    st["level"] = player.level.toString()
                    st.playSound(QuestState.SOUND_ITEMGET)
                    st.takeItems(LEFT_PENITENT_MANACLES, -1)
                    st.giveItems(PENITENT_MANACLES, 1)
                }// Case you return back to Black Judge with leftover of previous quest.
            }
        } else if (event.equals("30981-19.htm", ignoreCase = true)) {
            if (st.hasQuestItems(LEFT_PENITENT_MANACLES)) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "16"
                st.playSound(QuestState.SOUND_ACCEPT)
            }
        } else if (event.equals("Pk", ignoreCase = true)) {
            val pet = player.pet

            // If Sin Eater is currently summoned, show a warning.
            if (pet != null && pet.npcId == 12564)
                htmltext = "30981-16.htm"
            else if (findSinEaterLvl(player) > st.getInt("level")) {
                st.takeItems(PENITENT_MANACLES, 1)
                st.giveItems(LEFT_PENITENT_MANACLES, 1)

                val removePkAmount = Rnd[10] + 1

                // Player's PKs are lower than random amount ; finish the quest.
                if (player.pkKills <= removePkAmount) {
                    htmltext = "30981-15.htm"
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)

                    player.pkKills = 0
                    player.sendPacket(UserInfo(player))
                } else {
                    htmltext = "30981-14.htm"
                    st["level"] = player.level.toString()
                    st.playSound(QuestState.SOUND_MIDDLE)

                    player.pkKills = player.pkKills - removePkAmount
                    player.sendPacket(UserInfo(player))
                }// Player's PK are bigger than random amount ; continue the quest.
            }// If Sin Eater level is bigger than registered level, decrease PK counter by 1-10.
        } else if (event.equals("Quit", ignoreCase = true)) {
            htmltext = "30981-20.htm"

            st.takeItems(RATMAN_SCAVENGER_SKULL, -1)
            st.takeItems(TUREK_WAR_HOUND_TAIL, -1)
            st.takeItems(TYRANT_KINGPIN_HEART, -1)
            st.takeItems(TRISALIM_TARANTULA_VENOM_SAC, -1)

            st.takeItems(MANUAL_OF_MANACLES, -1)
            st.takeItems(PENITENT_MANACLES, -1)
            st.takeItems(QITEM_PENITENT_MANACLES, -1)

            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.alreadyCompletedMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.pkKills >= 1)
                htmltext = if (st.hasQuestItems(LEFT_PENITENT_MANACLES)) "30981-18.htm" else "30981-02.htm"
            else
                htmltext = "30981-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    BLACK_JUDGE -> if (cond <= 9)
                        htmltext = "30981-07.htm"
                    else if (cond > 9 && cond < 14) {
                        htmltext = "30981-08.htm"
                        st["cond"] = "14"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(MANUAL_OF_MANACLES, 1)
                    } else if (cond == 14)
                        htmltext = "30981-09.htm"
                    else if (cond == 15)
                        htmltext = "30981-10.htm"
                    else if (cond == 16) {
                        if (st.hasQuestItems(PENITENT_MANACLES))
                            htmltext =
                                    if (findSinEaterLvl(player) > st.getInt("level")) "30981-13.htm" else "30981-12.htm"
                        else
                            htmltext = "30981-18.htm"
                    }

                    KATARI -> if (cond == 2) {
                        htmltext = "30668-01.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 6) {
                        if (st.getQuestItemsCount(RATMAN_SCAVENGER_SKULL) < 10)
                            htmltext = "30668-02.htm"
                        else {
                            htmltext = "30668-03.htm"
                            st["cond"] = "10"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(RATMAN_SCAVENGER_SKULL, -1)
                        }
                    } else if (cond == 10)
                        htmltext = "30668-04.htm"

                    PIOTUR -> if (cond == 3) {
                        htmltext = "30597-01.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 7) {
                        if (st.getQuestItemsCount(TUREK_WAR_HOUND_TAIL) < 10)
                            htmltext = "30597-02.htm"
                        else {
                            htmltext = "30597-03.htm"
                            st["cond"] = "11"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(TUREK_WAR_HOUND_TAIL, -1)
                        }
                    } else if (cond == 11)
                        htmltext = "30597-04.htm"

                    CASIAN -> if (cond == 4) {
                        htmltext = "30612-01.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 8) {
                        if (!st.hasQuestItems(TYRANT_KINGPIN_HEART))
                            htmltext = "30612-02.htm"
                        else {
                            htmltext = "30612-03.htm"
                            st["cond"] = "12"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(TYRANT_KINGPIN_HEART, -1)
                        }
                    } else if (cond == 12)
                        htmltext = "30612-04.htm"

                    JOAN -> if (cond == 5) {
                        htmltext = "30718-01.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 9) {
                        if (st.getQuestItemsCount(TRISALIM_TARANTULA_VENOM_SAC) < 3)
                            htmltext = "30718-02.htm"
                        else {
                            htmltext = "30718-03.htm"
                            st["cond"] = "13"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(TRISALIM_TARANTULA_VENOM_SAC, -1)
                        }
                    } else if (cond == 13)
                        htmltext = "30718-04.htm"

                    PUSHKIN -> if (cond == 14 && st.getQuestItemsCount(MANUAL_OF_MANACLES) == 1) {
                        if (st.getQuestItemsCount(SILVER_NUGGET) < 10 || st.getQuestItemsCount(STEEL) < 5 || st.getQuestItemsCount(
                                ADAMANTINE_NUGGET
                            ) < 2 || st.getQuestItemsCount(COKES) < 10 || st.getQuestItemsCount(BLACKSMITH_FRAME) < 1
                        )
                            htmltext = "30300-02.htm"
                        else {
                            htmltext = "30300-01.htm"
                            st["cond"] = "15"
                            st.playSound(QuestState.SOUND_MIDDLE)

                            st.takeItems(MANUAL_OF_MANACLES, 1)
                            st.takeItems(SILVER_NUGGET, 10)
                            st.takeItems(ADAMANTINE_NUGGET, 2)
                            st.takeItems(COKES, 10)
                            st.takeItems(STEEL, 5)
                            st.takeItems(BLACKSMITH_FRAME, 1)

                            st.giveItems(QITEM_PENITENT_MANACLES, 1)
                        }
                    } else if (st.hasAtLeastOneQuestItem(
                            QITEM_PENITENT_MANACLES,
                            PENITENT_MANACLES,
                            LEFT_PENITENT_MANACLES
                        )
                    )
                        htmltext = "30300-03.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20039 -> if (st.getInt("cond") == 6)
                st.dropItemsAlways(RATMAN_SCAVENGER_SKULL, 1, 10)

            20494 -> if (st.getInt("cond") == 7)
                st.dropItemsAlways(TUREK_WAR_HOUND_TAIL, 1, 10)

            20193 -> if (st.getInt("cond") == 8)
                st.dropItemsAlways(TYRANT_KINGPIN_HEART, 1, 1)

            20561 -> if (st.getInt("cond") == 9)
                st.dropItemsAlways(TRISALIM_TARANTULA_VENOM_SAC, 1, 3)
        }

        return null
    }

    companion object {
        private val qn = "Q422_RepentYourSins"

        // Items
        private val RATMAN_SCAVENGER_SKULL = 4326
        private val TUREK_WAR_HOUND_TAIL = 4327
        private val TYRANT_KINGPIN_HEART = 4328
        private val TRISALIM_TARANTULA_VENOM_SAC = 4329

        private val QITEM_PENITENT_MANACLES = 4330
        private val MANUAL_OF_MANACLES = 4331
        private val PENITENT_MANACLES = 4425
        private val LEFT_PENITENT_MANACLES = 4426

        private val SILVER_NUGGET = 1873
        private val ADAMANTINE_NUGGET = 1877
        private val BLACKSMITH_FRAME = 1892
        private val COKES = 1879
        private val STEEL = 1880

        // NPCs
        private val BLACK_JUDGE = 30981
        private val KATARI = 30668
        private val PIOTUR = 30597
        private val CASIAN = 30612
        private val JOAN = 30718
        private val PUSHKIN = 30300

        private fun findSinEaterLvl(player: Player): Int {
            return player.inventory!!.getItemByItemId(PENITENT_MANACLES)!!.enchantLevel
        }
    }
}