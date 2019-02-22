package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q402_PathToAHumanKnight : Quest(402, "Path to a Human Knight") {
    init {

        setItemsIds(
            MARK_OF_ESQUIRE,
            COIN_OF_LORDS_1,
            COIN_OF_LORDS_2,
            COIN_OF_LORDS_3,
            COIN_OF_LORDS_4,
            COIN_OF_LORDS_5,
            COIN_OF_LORDS_6,
            GLUDIO_GUARD_MARK_1,
            BUGBEAR_NECKLACE,
            EINHASAD_CHURCH_MARK_1,
            EINHASAD_CRUCIFIX,
            GLUDIO_GUARD_MARK_2,
            SPIDER_LEG,
            EINHASAD_CHURCH_MARK_2,
            LIZARDMAN_TOTEM,
            GLUDIO_GUARD_MARK_3,
            GIANT_SPIDER_HUSK,
            EINHASAD_CHURCH_MARK_3,
            LIZARDMAN_TOTEM,
            GLUDIO_GUARD_MARK_3,
            GIANT_SPIDER_HUSK,
            EINHASAD_CHURCH_MARK_3,
            HORRIBLE_SKULL
        )

        addStartNpc(SIR_KLAUS_VASPER)
        addTalkId(
            SIR_KLAUS_VASPER,
            BATHIS,
            RAYMOND,
            BEZIQUE,
            LEVIAN,
            GILBERT,
            BIOTIN,
            SIR_AARON_TANFORD,
            SIR_COLLIN_WINDAWOOD
        )

        addKillId(20775, 27024, 20038, 20043, 20050, 20030, 20027, 20024, 20103, 20106, 20108, 20404)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30417-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.HUMAN_FIGHTER)
                htmltext = if (player.classId == ClassId.KNIGHT) "30417-02a.htm" else "30417-03.htm"
            else if (player.level < 19)
                htmltext = "30417-02.htm"
            else if (st.hasQuestItems(SWORD_OF_RITUAL))
                htmltext = "30417-04.htm"
        } else if (event.equals("30417-08.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(MARK_OF_ESQUIRE, 1)
        } else if (event.equals("30332-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(GLUDIO_GUARD_MARK_1, 1)
        } else if (event.equals("30289-03.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(EINHASAD_CHURCH_MARK_1, 1)
        } else if (event.equals("30379-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(GLUDIO_GUARD_MARK_2, 1)
        } else if (event.equals("30037-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(EINHASAD_CHURCH_MARK_2, 1)
        } else if (event.equals("30039-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(GLUDIO_GUARD_MARK_3, 1)
        } else if (event.equals("30031-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(EINHASAD_CHURCH_MARK_3, 1)
        } else if (event.equals("30417-13.htm", ignoreCase = true) || event.equals("30417-14.htm", ignoreCase = true)) {
            val coinCount =
                st.getQuestItemsCount(COIN_OF_LORDS_1) + st.getQuestItemsCount(COIN_OF_LORDS_2) + st.getQuestItemsCount(
                    COIN_OF_LORDS_3
                ) + st.getQuestItemsCount(COIN_OF_LORDS_4) + st.getQuestItemsCount(COIN_OF_LORDS_5) + st.getQuestItemsCount(
                    COIN_OF_LORDS_6
                )

            st.takeItems(COIN_OF_LORDS_1, -1)
            st.takeItems(COIN_OF_LORDS_2, -1)
            st.takeItems(COIN_OF_LORDS_3, -1)
            st.takeItems(COIN_OF_LORDS_4, -1)
            st.takeItems(COIN_OF_LORDS_5, -1)
            st.takeItems(COIN_OF_LORDS_6, -1)
            st.takeItems(MARK_OF_ESQUIRE, 1)
            st.giveItems(SWORD_OF_RITUAL, 1)
            st.rewardExpAndSp(3200, 1500 + 1920 * (coinCount - 3))
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30417-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                SIR_KLAUS_VASPER -> {
                    val coins =
                        st.getQuestItemsCount(COIN_OF_LORDS_1) + st.getQuestItemsCount(COIN_OF_LORDS_2) + st.getQuestItemsCount(
                            COIN_OF_LORDS_3
                        ) + st.getQuestItemsCount(COIN_OF_LORDS_4) + st.getQuestItemsCount(COIN_OF_LORDS_5) + st.getQuestItemsCount(
                            COIN_OF_LORDS_6
                        )
                    if (coins < 3)
                        htmltext = "30417-09.htm"
                    else if (coins == 3)
                        htmltext = "30417-10.htm"
                    else if (coins > 3 && coins < 6)
                        htmltext = "30417-11.htm"
                    else if (coins == 6) {
                        htmltext = "30417-12.htm"
                        st.takeItems(COIN_OF_LORDS_1, -1)
                        st.takeItems(COIN_OF_LORDS_2, -1)
                        st.takeItems(COIN_OF_LORDS_3, -1)
                        st.takeItems(COIN_OF_LORDS_4, -1)
                        st.takeItems(COIN_OF_LORDS_5, -1)
                        st.takeItems(COIN_OF_LORDS_6, -1)
                        st.takeItems(MARK_OF_ESQUIRE, 1)
                        st.giveItems(SWORD_OF_RITUAL, 1)
                        st.rewardExpAndSp(3200, 7260)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }
                }

                BATHIS -> if (st.hasQuestItems(COIN_OF_LORDS_1))
                    htmltext = "30332-05.htm"
                else if (st.hasQuestItems(GLUDIO_GUARD_MARK_1)) {
                    if (st.getQuestItemsCount(BUGBEAR_NECKLACE) < 10)
                        htmltext = "30332-03.htm"
                    else {
                        htmltext = "30332-04.htm"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BUGBEAR_NECKLACE, -1)
                        st.takeItems(GLUDIO_GUARD_MARK_1, 1)
                        st.giveItems(COIN_OF_LORDS_1, 1)
                    }
                } else
                    htmltext = "30332-01.htm"

                RAYMOND -> if (st.hasQuestItems(COIN_OF_LORDS_2))
                    htmltext = "30289-06.htm"
                else if (st.hasQuestItems(EINHASAD_CHURCH_MARK_1)) {
                    if (st.getQuestItemsCount(EINHASAD_CRUCIFIX) < 12)
                        htmltext = "30289-04.htm"
                    else {
                        htmltext = "30289-05.htm"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(EINHASAD_CRUCIFIX, -1)
                        st.takeItems(EINHASAD_CHURCH_MARK_1, 1)
                        st.giveItems(COIN_OF_LORDS_2, 1)
                    }
                } else
                    htmltext = "30289-01.htm"

                BEZIQUE -> if (st.hasQuestItems(COIN_OF_LORDS_3))
                    htmltext = "30379-05.htm"
                else if (st.hasQuestItems(GLUDIO_GUARD_MARK_2)) {
                    if (st.getQuestItemsCount(SPIDER_LEG) < 20)
                        htmltext = "30379-03.htm"
                    else {
                        htmltext = "30379-04.htm"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SPIDER_LEG, -1)
                        st.takeItems(GLUDIO_GUARD_MARK_2, 1)
                        st.giveItems(COIN_OF_LORDS_3, 1)
                    }
                } else
                    htmltext = "30379-01.htm"

                LEVIAN -> if (st.hasQuestItems(COIN_OF_LORDS_4))
                    htmltext = "30037-05.htm"
                else if (st.hasQuestItems(EINHASAD_CHURCH_MARK_2)) {
                    if (st.getQuestItemsCount(LIZARDMAN_TOTEM) < 20)
                        htmltext = "30037-03.htm"
                    else {
                        htmltext = "30037-04.htm"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LIZARDMAN_TOTEM, -1)
                        st.takeItems(EINHASAD_CHURCH_MARK_2, 1)
                        st.giveItems(COIN_OF_LORDS_4, 1)
                    }
                } else
                    htmltext = "30037-01.htm"

                GILBERT -> if (st.hasQuestItems(COIN_OF_LORDS_5))
                    htmltext = "30039-05.htm"
                else if (st.hasQuestItems(GLUDIO_GUARD_MARK_3)) {
                    if (st.getQuestItemsCount(GIANT_SPIDER_HUSK) < 20)
                        htmltext = "30039-03.htm"
                    else {
                        htmltext = "30039-04.htm"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(GIANT_SPIDER_HUSK, -1)
                        st.takeItems(GLUDIO_GUARD_MARK_3, 1)
                        st.giveItems(COIN_OF_LORDS_5, 1)
                    }
                } else
                    htmltext = "30039-01.htm"

                BIOTIN -> if (st.hasQuestItems(COIN_OF_LORDS_6))
                    htmltext = "30031-05.htm"
                else if (st.hasQuestItems(EINHASAD_CHURCH_MARK_3)) {
                    if (st.getQuestItemsCount(HORRIBLE_SKULL) < 10)
                        htmltext = "30031-03.htm"
                    else {
                        htmltext = "30031-04.htm"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(HORRIBLE_SKULL, -1)
                        st.takeItems(EINHASAD_CHURCH_MARK_3, 1)
                        st.giveItems(COIN_OF_LORDS_6, 1)
                    }
                } else
                    htmltext = "30031-01.htm"

                SIR_AARON_TANFORD -> htmltext = "30653-01.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20775 // Bugbear Raider
            -> if (st.hasQuestItems(GLUDIO_GUARD_MARK_1))
                st.dropItemsAlways(BUGBEAR_NECKLACE, 1, 10)

            27024 // Undead Priest
            -> if (st.hasQuestItems(EINHASAD_CHURCH_MARK_1))
                st.dropItems(EINHASAD_CRUCIFIX, 1, 12, 500000)

            20038 // Poison Spider
                , 20043 // Arachnid Tracker
                , 20050 // Arachnid Predator
            -> if (st.hasQuestItems(GLUDIO_GUARD_MARK_2))
                st.dropItemsAlways(SPIDER_LEG, 1, 20)

            20030 // Langk Lizardman
                , 20027 // Langk Lizardman Scout
                , 20024 // Langk Lizardman Warrior
            -> if (st.hasQuestItems(EINHASAD_CHURCH_MARK_2))
                st.dropItems(LIZARDMAN_TOTEM, 1, 20, 500000)

            20103 // Giant Spider
                , 20106 // Talon Spider
                , 20108 // Blade Spider
            -> if (st.hasQuestItems(GLUDIO_GUARD_MARK_3))
                st.dropItems(GIANT_SPIDER_HUSK, 1, 20, 400000)

            20404 // Silent Horror
            -> if (st.hasQuestItems(EINHASAD_CHURCH_MARK_3))
                st.dropItems(HORRIBLE_SKULL, 1, 10, 400000)
        }

        return null
    }

    companion object {
        private val qn = "Q402_PathToAHumanKnight"

        // Items
        private val SWORD_OF_RITUAL = 1161
        private val COIN_OF_LORDS_1 = 1162
        private val COIN_OF_LORDS_2 = 1163
        private val COIN_OF_LORDS_3 = 1164
        private val COIN_OF_LORDS_4 = 1165
        private val COIN_OF_LORDS_5 = 1166
        private val COIN_OF_LORDS_6 = 1167
        private val GLUDIO_GUARD_MARK_1 = 1168
        private val BUGBEAR_NECKLACE = 1169
        private val EINHASAD_CHURCH_MARK_1 = 1170
        private val EINHASAD_CRUCIFIX = 1171
        private val GLUDIO_GUARD_MARK_2 = 1172
        private val SPIDER_LEG = 1173
        private val EINHASAD_CHURCH_MARK_2 = 1174
        private val LIZARDMAN_TOTEM = 1175
        private val GLUDIO_GUARD_MARK_3 = 1176
        private val GIANT_SPIDER_HUSK = 1177
        private val EINHASAD_CHURCH_MARK_3 = 1178
        private val HORRIBLE_SKULL = 1179
        private val MARK_OF_ESQUIRE = 1271

        // NPCs
        private val SIR_KLAUS_VASPER = 30417
        private val BATHIS = 30332
        private val RAYMOND = 30289
        private val BEZIQUE = 30379
        private val LEVIAN = 30037
        private val GILBERT = 30039
        private val BIOTIN = 30031
        private val SIR_AARON_TANFORD = 30653
        private val SIR_COLLIN_WINDAWOOD = 30311
    }
}