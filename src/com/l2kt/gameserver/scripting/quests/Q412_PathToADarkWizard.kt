package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q412_PathToADarkWizard : Quest(412, "Path to a Dark Wizard") {
    init {

        setItemsIds(
            SEED_OF_ANGER,
            SEED_OF_DESPAIR,
            SEED_OF_HORROR,
            SEED_OF_LUNACY,
            FAMILY_REMAINS,
            VARIKA_LIQUOR,
            KNEE_BONE,
            HEART_OF_LUNACY,
            LUCKY_KEY,
            CANDLE,
            HUB_SCENT
        )

        addStartNpc(VARIKA)
        addTalkId(VARIKA, CHARKEREN, ANNIKA, ARKENIA)

        addKillId(20015, 20022, 20045, 20517, 20518)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30421-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.DARK_MYSTIC)
                htmltext = if (player.classId == ClassId.DARK_WIZARD) "30421-02a.htm" else "30421-03.htm"
            else if (player.level < 19)
                htmltext = "30421-02.htm"
            else if (st.hasQuestItems(JEWEL_OF_DARKNESS))
                htmltext = "30421-04.htm"
            else {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.giveItems(SEED_OF_DESPAIR, 1)
            }
        } else if (event.equals("30421-07.htm", ignoreCase = true)) {
            if (st.hasQuestItems(SEED_OF_ANGER))
                htmltext = "30421-06.htm"
            else if (st.hasQuestItems(LUCKY_KEY))
                htmltext = "30421-08.htm"
            else if (st.getQuestItemsCount(FAMILY_REMAINS) == 3)
                htmltext = "30421-18.htm"
        } else if (event.equals("30421-10.htm", ignoreCase = true)) {
            if (st.hasQuestItems(SEED_OF_HORROR))
                htmltext = "30421-09.htm"
            else if (st.getQuestItemsCount(KNEE_BONE) == 2)
                htmltext = "30421-19.htm"
        } else if (event.equals("30421-13.htm", ignoreCase = true)) {
            if (st.hasQuestItems(SEED_OF_LUNACY))
                htmltext = "30421-12.htm"
        } else if (event.equals("30415-03.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(LUCKY_KEY, 1)
        } else if (event.equals("30418-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(CANDLE, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30421-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                VARIKA -> if (st.hasQuestItems(SEED_OF_ANGER, SEED_OF_HORROR, SEED_OF_LUNACY)) {
                    htmltext = "30421-16.htm"
                    st.takeItems(SEED_OF_ANGER, 1)
                    st.takeItems(SEED_OF_DESPAIR, 1)
                    st.takeItems(SEED_OF_HORROR, 1)
                    st.takeItems(SEED_OF_LUNACY, 1)
                    st.giveItems(JEWEL_OF_DARKNESS, 1)
                    st.rewardExpAndSp(3200, 1650)
                    player.broadcastPacket(SocialAction(player, 3))
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                } else
                    htmltext = "30421-17.htm"

                CHARKEREN -> if (st.hasQuestItems(SEED_OF_ANGER))
                    htmltext = "30415-06.htm"
                else if (!st.hasQuestItems(LUCKY_KEY))
                    htmltext = "30415-01.htm"
                else if (st.getQuestItemsCount(FAMILY_REMAINS) == 3) {
                    htmltext = "30415-05.htm"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(FAMILY_REMAINS, -1)
                    st.takeItems(LUCKY_KEY, 1)
                    st.giveItems(SEED_OF_ANGER, 1)
                } else
                    htmltext = "30415-04.htm"

                ANNIKA -> if (st.hasQuestItems(SEED_OF_HORROR))
                    htmltext = "30418-04.htm"
                else if (!st.hasQuestItems(CANDLE))
                    htmltext = "30418-01.htm"
                else if (st.getQuestItemsCount(KNEE_BONE) == 2) {
                    htmltext = "30418-04.htm"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(CANDLE, 1)
                    st.takeItems(KNEE_BONE, -1)
                    st.giveItems(SEED_OF_HORROR, 1)
                } else
                    htmltext = "30418-03.htm"

                ARKENIA -> if (st.hasQuestItems(SEED_OF_LUNACY))
                    htmltext = "30419-03.htm"
                else if (!st.hasQuestItems(HUB_SCENT)) {
                    htmltext = "30419-01.htm"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(HUB_SCENT, 1)
                } else if (st.getQuestItemsCount(HEART_OF_LUNACY) == 3) {
                    htmltext = "30419-03.htm"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(HEART_OF_LUNACY, -1)
                    st.takeItems(HUB_SCENT, 1)
                    st.giveItems(SEED_OF_LUNACY, 1)
                } else
                    htmltext = "30419-02.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20015 -> if (st.hasQuestItems(LUCKY_KEY))
                st.dropItems(FAMILY_REMAINS, 1, 3, 500000)

            20022, 20517, 20518 -> if (st.hasQuestItems(CANDLE))
                st.dropItems(KNEE_BONE, 1, 2, 500000)

            20045 -> if (st.hasQuestItems(HUB_SCENT))
                st.dropItems(HEART_OF_LUNACY, 1, 3, 500000)
        }

        return null
    }

    companion object {
        private val qn = "Q412_PathToADarkWizard"

        // Items
        private val SEED_OF_ANGER = 1253
        private val SEED_OF_DESPAIR = 1254
        private val SEED_OF_HORROR = 1255
        private val SEED_OF_LUNACY = 1256
        private val FAMILY_REMAINS = 1257
        private val VARIKA_LIQUOR = 1258
        private val KNEE_BONE = 1259
        private val HEART_OF_LUNACY = 1260
        private val JEWEL_OF_DARKNESS = 1261
        private val LUCKY_KEY = 1277
        private val CANDLE = 1278
        private val HUB_SCENT = 1279

        // NPCs
        private val VARIKA = 30421
        private val CHARKEREN = 30415
        private val ANNIKA = 30418
        private val ARKENIA = 30419
    }
}