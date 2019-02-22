package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q123_TheLeaderAndTheFollower : Quest(123, "The Leader and the Follower") {
    init {

        setItemsIds(BRUIN_LIZARDMAN_BLOOD, PICOT_ARANEID_LEG)

        addStartNpc(NEWYEAR)
        addTalkId(NEWYEAR)

        addKillId(BRUIN_LIZARDMAN, PICOT_ARENEID)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31961-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["state"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31961-05d.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(BRUIN_LIZARDMAN_BLOOD) > 9) {
                st["cond"] = "3"
                st["state"] = "2"
                st["stateEx"] = "1"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BRUIN_LIZARDMAN_BLOOD, -1)
            }
        } else if (event.equals("31961-05e.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(BRUIN_LIZARDMAN_BLOOD) > 9) {
                st["cond"] = "4"
                st["state"] = "2"
                st["stateEx"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BRUIN_LIZARDMAN_BLOOD, -1)
            }
        } else if (event.equals("31961-05f.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(BRUIN_LIZARDMAN_BLOOD) > 9) {
                st["cond"] = "5"
                st["state"] = "2"
                st["stateEx"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BRUIN_LIZARDMAN_BLOOD, -1)
            }
        } else if (event.equals("31961-10.htm", ignoreCase = true)) {
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
                            htmltext = "31961-11.htm"
                    } else {
                        if (st.getQuestItemsCount(CRYSTAL_D) > 770) {
                            st.takeItems(CRYSTAL_D, 771)
                            st2["cond"] = "6"
                            st2["state"] = "3"
                            st2.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            htmltext = "31961-11a.htm"
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
                    htmltext = if (st2.isCompleted) "31961-02a.htm" else "31961-02b.htm"
                else
                    htmltext = if (player.level > 18) "31961-01.htm" else "31961-02.htm"
            } else if (player.apprentice > 0) {
                val academic = getApprentice(player)
                if (academic != null) {
                    val st3 = academic.getQuestState(qn)
                    if (st3 != null) {
                        val state = st3.getInt("state")
                        if (state == 2)
                            htmltext = "31961-08.htm"
                        else if (state == 3)
                            htmltext = "31961-12.htm"
                        else
                            htmltext = "31961-14.htm"
                    }
                } else
                    htmltext = "31961-09.htm"
            }

            Quest.STATE_STARTED -> {
                val state = st.getInt("state")
                if (state == 1)
                    htmltext = if (st.getQuestItemsCount(BRUIN_LIZARDMAN_BLOOD) < 10) "31961-04.htm" else "31961-05.htm"
                else if (state == 2) {
                    val stateEx = st.getInt("stateEx")
                    if (player.sponsor == 0) {
                        if (stateEx == 1)
                            htmltext = "31961-06a.htm"
                        else if (stateEx == 2)
                            htmltext = "31961-06b.htm"
                        else if (stateEx == 3)
                            htmltext = "31961-06c.htm"
                    } else {
                        if (getSponsor(player)) {
                            if (stateEx == 1)
                                htmltext = "31961-06.htm"
                            else if (stateEx == 2)
                                htmltext = "31961-06d.htm"
                            else if (stateEx == 3)
                                htmltext = "31961-06e.htm"
                        } else
                            htmltext = "31961-07.htm"
                    }
                } else if (state == 3) {
                    st["cond"] = "7"
                    st["state"] = "4"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    htmltext = "31961-15.htm"
                } else if (state == 4) {
                    if (st.getQuestItemsCount(PICOT_ARANEID_LEG) > 7) {
                        htmltext = "31961-17.htm"

                        st.takeItems(PICOT_ARANEID_LEG, -1)
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
                        htmltext = "31961-16.htm"
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
            BRUIN_LIZARDMAN -> if (cond == 1 && st.dropItems(BRUIN_LIZARDMAN_BLOOD, 1, 10, 700000))
                st["cond"] = "2"

            PICOT_ARENEID -> if (cond == 7 && getSponsor(player) && st.dropItems(PICOT_ARANEID_LEG, 1, 8, 700000))
                st["cond"] = "8"
        }

        return null
    }

    companion object {
        private const val qn = "Q123_TheLeaderAndTheFollower"
        private const val qn2 = "Q118_ToLeadAndBeLed"

        // NPC
        private const val NEWYEAR = 31961

        // Mobs
        private const val BRUIN_LIZARDMAN = 27321
        private const val PICOT_ARENEID = 27322

        // Items
        private const val BRUIN_LIZARDMAN_BLOOD = 8549
        private const val PICOT_ARANEID_LEG = 8550
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