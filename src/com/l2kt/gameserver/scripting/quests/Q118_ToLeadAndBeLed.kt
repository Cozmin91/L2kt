package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q118_ToLeadAndBeLed : Quest(118, "To Lead and Be Led") {
    init {

        setItemsIds(BLOOD_OF_MAILLE_LIZARDMAN, LEG_OF_KING_ARANEID)

        addStartNpc(PINTER)
        addTalkId(PINTER)

        addKillId(MAILLE_LIZARDMAN, MAILLE_LIZARDMAN_SCOUT, MAILLE_LIZARDMAN_GUARD, KING_OF_THE_ARANEID)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30298-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["state"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30298-05d.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(BLOOD_OF_MAILLE_LIZARDMAN) > 9) {
                st["cond"] = "3"
                st["state"] = "2"
                st["stateEx"] = "1"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BLOOD_OF_MAILLE_LIZARDMAN, -1)
            }
        } else if (event.equals("30298-05e.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(BLOOD_OF_MAILLE_LIZARDMAN) > 9) {
                st["cond"] = "4"
                st["state"] = "2"
                st["stateEx"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BLOOD_OF_MAILLE_LIZARDMAN, -1)
            }
        } else if (event.equals("30298-05f.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(BLOOD_OF_MAILLE_LIZARDMAN) > 9) {
                st["cond"] = "5"
                st["state"] = "2"
                st["stateEx"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BLOOD_OF_MAILLE_LIZARDMAN, -1)
            }
        } else if (event.equals("30298-10.htm", ignoreCase = true)) {
            val academic = getApprentice(player)
            if (academic != null) {
                val st2 = academic.getQuestState(qn)
                if (st2 != null && st2.getInt("state") == 2) {
                    val stateEx = st2.getInt("stateEx")
                    if (stateEx == 1) {
                        if (st.getQuestItemsCount(CRYSTAL_D) > 921) {
                            st.takeItems(CRYSTAL_D, 922)
                            st2["cond"] = "6"
                            st2["state"] = "3"
                            st2.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            htmltext = "30298-11.htm"
                    } else {
                        if (st.getQuestItemsCount(CRYSTAL_D) > 770) {
                            st.takeItems(CRYSTAL_D, 771)
                            st2["cond"] = "6"
                            st2["state"] = "3"
                            st2.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            htmltext = "30298-11a.htm"
                    }
                }
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.sponsor > 0) {
                val st2 = player.getQuestState(qn2)
                if (st2 != null)
                    htmltext = if (st2.isCompleted) "30298-02a.htm" else "30298-02b.htm"
                else
                    htmltext = if (player.level > 18) "30298-01.htm" else "30298-02.htm"
            } else if (player.apprentice > 0) {
                val academic = getApprentice(player)
                if (academic != null) {
                    val st3 = academic.getQuestState(qn)
                    if (st3 != null) {
                        val state = st3.getInt("state")
                        if (state == 2)
                            htmltext = "30298-08.htm"
                        else if (state == 3)
                            htmltext = "30298-12.htm"
                        else
                            htmltext = "30298-14.htm"
                    }
                } else
                    htmltext = "30298-09.htm"
            }

            Quest.STATE_STARTED -> {
                val state = st.getInt("state")
                if (state == 1)
                    htmltext =
                            if (st.getQuestItemsCount(BLOOD_OF_MAILLE_LIZARDMAN) < 10) "30298-04.htm" else "30298-05.htm"
                else if (state == 2) {
                    val stateEx = st.getInt("stateEx")
                    if (player.sponsor == 0) {
                        if (stateEx == 1)
                            htmltext = "30298-06a.htm"
                        else if (stateEx == 2)
                            htmltext = "30298-06b.htm"
                        else if (stateEx == 3)
                            htmltext = "30298-06c.htm"
                    } else {
                        if (getSponsor(player)) {
                            if (stateEx == 1)
                                htmltext = "30298-06.htm"
                            else if (stateEx == 2)
                                htmltext = "30298-06d.htm"
                            else if (stateEx == 3)
                                htmltext = "30298-06e.htm"
                        } else
                            htmltext = "30298-07.htm"
                    }
                } else if (state == 3) {
                    st["cond"] = "7"
                    st["state"] = "4"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    htmltext = "30298-15.htm"
                } else if (state == 4) {
                    if (st.getQuestItemsCount(LEG_OF_KING_ARANEID) > 7) {
                        htmltext = "30298-17.htm"

                        st.takeItems(LEG_OF_KING_ARANEID, -1)
                        st.giveItems(CLAN_OATH_HELM, 1)

                        when (st.getInt("stateEx")) {
                            1 -> {
                                st.giveItems(CLAN_OATH_ARMOR, 1)
                                st.giveItems(CLAN_OATH_GAUNTLETS, 1)
                                st.giveItems(CLAN_OATH_SABATON, 1)
                            }

                            2 -> {
                                st.giveItems(CLAN_OATH_BRIGANDINE, 1)
                                st.giveItems(CLAN_OATH_LEATHER_GLOVES, 1)
                                st.giveItems(CLAN_OATH_BOOTS, 1)
                            }

                            3 -> {
                                st.giveItems(CLAN_OATH_AKETON, 1)
                                st.giveItems(CLAN_OATH_PADDED_GLOVES, 1)
                                st.giveItems(CLAN_OATH_SANDALS, 1)
                            }
                        }

                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    } else
                        htmltext = "30298-16.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (player!!.sponsor == 0) {
            st.exitQuest(true)
            return null
        }

        val cond = st.getInt("cond")
        when (npc.npcId) {
            MAILLE_LIZARDMAN, MAILLE_LIZARDMAN_SCOUT, MAILLE_LIZARDMAN_GUARD -> if (cond == 1 && st.dropItems(
                    BLOOD_OF_MAILLE_LIZARDMAN,
                    1,
                    10,
                    700000
                )
            )
                st["cond"] = "2"

            KING_OF_THE_ARANEID -> if (cond == 7 && getSponsor(player) && st.dropItems(
                    LEG_OF_KING_ARANEID,
                    1,
                    8,
                    700000
                )
            )
                st["cond"] = "8"
        }

        return null
    }

    companion object {
        private const val qn = "Q118_ToLeadAndBeLed"
        private const val qn2 = "Q123_TheLeaderAndTheFollower"

        // Npc
        private const val PINTER = 30298

        // Mobs
        private const val MAILLE_LIZARDMAN = 20919
        private const val MAILLE_LIZARDMAN_SCOUT = 20920
        private const val MAILLE_LIZARDMAN_GUARD = 20921
        private const val KING_OF_THE_ARANEID = 20927

        // Items
        private const val BLOOD_OF_MAILLE_LIZARDMAN = 8062
        private const val LEG_OF_KING_ARANEID = 8063
        private const val CRYSTAL_D = 1458

        // Rewards
        private const val CLAN_OATH_HELM = 7850
        private const val CLAN_OATH_ARMOR = 7851
        private const val CLAN_OATH_GAUNTLETS = 7852
        private const val CLAN_OATH_SABATON = 7853
        private const val CLAN_OATH_BRIGANDINE = 7854
        private const val CLAN_OATH_LEATHER_GLOVES = 7855
        private const val CLAN_OATH_BOOTS = 7856
        private const val CLAN_OATH_AKETON = 7857
        private const val CLAN_OATH_PADDED_GLOVES = 7858
        private const val CLAN_OATH_SANDALS = 7859
    }
}