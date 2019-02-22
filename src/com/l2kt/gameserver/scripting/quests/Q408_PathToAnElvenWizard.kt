package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q408_PathToAnElvenWizard : Quest(408, "Path to an Elven Wizard") {
    init {

        setItemsIds(
            ROSELLA_LETTER,
            RED_DOWN,
            MAGICAL_POWERS_RUBY,
            PURE_AQUAMARINE,
            APPETIZING_APPLE,
            GOLD_LEAVES,
            IMMORTAL_LOVE,
            AMETHYST,
            NOBILITY_AMETHYST,
            FERTILITY_PERIDOT,
            CHARM_OF_GRAIN,
            SAP_OF_THE_MOTHER_TREE,
            LUCKY_POTPOURRI
        )

        addStartNpc(ROSELLA)
        addTalkId(ROSELLA, GREENIS, THALIA, NORTHWIND)

        addKillId(20047, 20019, 20466)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30414-06.htm", ignoreCase = true)) {
            if (player.classId != ClassId.ELVEN_MYSTIC)
                htmltext = if (player.classId == ClassId.ELVEN_WIZARD) "30414-02a.htm" else "30414-03.htm"
            else if (player.level < 19)
                htmltext = "30414-04.htm"
            else if (st.hasQuestItems(ETERNITY_DIAMOND))
                htmltext = "30414-05.htm"
            else {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.giveItems(FERTILITY_PERIDOT, 1)
            }
        } else if (event.equals("30414-07.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(MAGICAL_POWERS_RUBY)) {
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(ROSELLA_LETTER, 1)
            } else
                htmltext = "30414-10.htm"
        } else if (event.equals("30414-14.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(PURE_AQUAMARINE)) {
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(APPETIZING_APPLE, 1)
            } else
                htmltext = "30414-13.htm"
        } else if (event.equals("30414-18.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(NOBILITY_AMETHYST)) {
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(IMMORTAL_LOVE, 1)
            } else
                htmltext = "30414-17.htm"
        } else if (event.equals("30157-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ROSELLA_LETTER, 1)
            st.giveItems(CHARM_OF_GRAIN, 1)
        } else if (event.equals("30371-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(APPETIZING_APPLE, 1)
            st.giveItems(SAP_OF_THE_MOTHER_TREE, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30414-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                ROSELLA -> if (st.hasQuestItems(MAGICAL_POWERS_RUBY, NOBILITY_AMETHYST, PURE_AQUAMARINE)) {
                    htmltext = "30414-24.htm"
                    st.takeItems(FERTILITY_PERIDOT, 1)
                    st.takeItems(MAGICAL_POWERS_RUBY, 1)
                    st.takeItems(NOBILITY_AMETHYST, 1)
                    st.takeItems(PURE_AQUAMARINE, 1)
                    st.giveItems(ETERNITY_DIAMOND, 1)
                    st.rewardExpAndSp(3200, 1890)
                    player.broadcastPacket(SocialAction(player, 3))
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                } else if (st.hasQuestItems(ROSELLA_LETTER))
                    htmltext = "30414-08.htm"
                else if (st.hasQuestItems(CHARM_OF_GRAIN)) {
                    if (st.getQuestItemsCount(RED_DOWN) == 5)
                        htmltext = "30414-25.htm"
                    else
                        htmltext = "30414-09.htm"
                } else if (st.hasQuestItems(APPETIZING_APPLE))
                    htmltext = "30414-15.htm"
                else if (st.hasQuestItems(SAP_OF_THE_MOTHER_TREE)) {
                    if (st.getQuestItemsCount(GOLD_LEAVES) == 5)
                        htmltext = "30414-26.htm"
                    else
                        htmltext = "30414-16.htm"
                } else if (st.hasQuestItems(IMMORTAL_LOVE))
                    htmltext = "30414-19.htm"
                else if (st.hasQuestItems(LUCKY_POTPOURRI)) {
                    if (st.getQuestItemsCount(AMETHYST) == 2)
                        htmltext = "30414-27.htm"
                    else
                        htmltext = "30414-20.htm"
                } else
                    htmltext = "30414-11.htm"

                GREENIS -> if (st.hasQuestItems(ROSELLA_LETTER))
                    htmltext = "30157-01.htm"
                else if (st.getQuestItemsCount(RED_DOWN) == 5) {
                    htmltext = "30157-04.htm"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(CHARM_OF_GRAIN, 1)
                    st.takeItems(RED_DOWN, -1)
                    st.giveItems(MAGICAL_POWERS_RUBY, 1)
                } else if (st.hasQuestItems(CHARM_OF_GRAIN))
                    htmltext = "30157-03.htm"

                THALIA -> if (st.hasQuestItems(APPETIZING_APPLE))
                    htmltext = "30371-01.htm"
                else if (st.getQuestItemsCount(GOLD_LEAVES) == 5) {
                    htmltext = "30371-04.htm"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(GOLD_LEAVES, -1)
                    st.takeItems(SAP_OF_THE_MOTHER_TREE, 1)
                    st.giveItems(PURE_AQUAMARINE, 1)
                } else if (st.hasQuestItems(SAP_OF_THE_MOTHER_TREE))
                    htmltext = "30371-03.htm"

                NORTHWIND -> if (st.hasQuestItems(IMMORTAL_LOVE)) {
                    htmltext = "30423-01.htm"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(IMMORTAL_LOVE, 1)
                    st.giveItems(LUCKY_POTPOURRI, 1)
                } else if (st.getQuestItemsCount(AMETHYST) == 2) {
                    htmltext = "30423-03.htm"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(AMETHYST, -1)
                    st.takeItems(LUCKY_POTPOURRI, 1)
                    st.giveItems(NOBILITY_AMETHYST, 1)
                } else if (st.hasQuestItems(LUCKY_POTPOURRI))
                    htmltext = "30423-02.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20019 -> if (st.hasQuestItems(SAP_OF_THE_MOTHER_TREE))
                st.dropItems(GOLD_LEAVES, 1, 5, 400000)

            20047 -> if (st.hasQuestItems(LUCKY_POTPOURRI))
                st.dropItems(AMETHYST, 1, 2, 400000)

            20466 -> if (st.hasQuestItems(CHARM_OF_GRAIN))
                st.dropItems(RED_DOWN, 1, 5, 700000)
        }

        return null
    }

    companion object {
        private val qn = "Q408_PathToAnElvenWizard"

        // Items
        private val ROSELLA_LETTER = 1218
        private val RED_DOWN = 1219
        private val MAGICAL_POWERS_RUBY = 1220
        private val PURE_AQUAMARINE = 1221
        private val APPETIZING_APPLE = 1222
        private val GOLD_LEAVES = 1223
        private val IMMORTAL_LOVE = 1224
        private val AMETHYST = 1225
        private val NOBILITY_AMETHYST = 1226
        private val FERTILITY_PERIDOT = 1229
        private val ETERNITY_DIAMOND = 1230
        private val CHARM_OF_GRAIN = 1272
        private val SAP_OF_THE_MOTHER_TREE = 1273
        private val LUCKY_POTPOURRI = 1274

        // NPCs
        private val ROSELLA = 30414
        private val GREENIS = 30157
        private val THALIA = 30371
        private val NORTHWIND = 30423
    }
}