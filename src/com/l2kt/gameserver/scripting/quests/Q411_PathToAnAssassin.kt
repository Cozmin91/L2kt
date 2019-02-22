package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q411_PathToAnAssassin : Quest(411, "Path to an Assassin") {
    init {

        setItemsIds(
            SHILEN_CALL,
            ARKENIA_LETTER,
            LEIKAN_NOTE,
            MOONSTONE_BEAST_MOLAR,
            SHILEN_TEARS,
            ARKENIA_RECOMMENDATION
        )

        addStartNpc(TRISKEL)
        addTalkId(TRISKEL, ARKENIA, LEIKAN)

        addKillId(27036, 20369)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30416-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.DARK_FIGHTER)
                htmltext = if (player.classId == ClassId.ASSASSIN) "30416-02a.htm" else "30416-02.htm"
            else if (player.level < 19)
                htmltext = "30416-03.htm"
            else if (st.hasQuestItems(IRON_HEART))
                htmltext = "30416-04.htm"
            else {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.giveItems(SHILEN_CALL, 1)
            }
        } else if (event.equals("30419-05.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SHILEN_CALL, 1)
            st.giveItems(ARKENIA_LETTER, 1)
        } else if (event.equals("30382-03.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ARKENIA_LETTER, 1)
            st.giveItems(LEIKAN_NOTE, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30416-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    TRISKEL -> if (cond == 1)
                        htmltext = "30416-11.htm"
                    else if (cond == 2)
                        htmltext = "30416-07.htm"
                    else if (cond == 3 || cond == 4)
                        htmltext = "30416-08.htm"
                    else if (cond == 5)
                        htmltext = "30416-09.htm"
                    else if (cond == 6)
                        htmltext = "30416-10.htm"
                    else if (cond == 7) {
                        htmltext = "30416-06.htm"
                        st.takeItems(ARKENIA_RECOMMENDATION, 1)
                        st.giveItems(IRON_HEART, 1)
                        st.rewardExpAndSp(3200, 3930)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    ARKENIA -> if (cond == 1)
                        htmltext = "30419-01.htm"
                    else if (cond == 2)
                        htmltext = "30419-07.htm"
                    else if (cond == 3 || cond == 4)
                        htmltext = "30419-10.htm"
                    else if (cond == 5)
                        htmltext = "30419-11.htm"
                    else if (cond == 6) {
                        htmltext = "30419-08.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SHILEN_TEARS, -1)
                        st.giveItems(ARKENIA_RECOMMENDATION, 1)
                    } else if (cond == 7)
                        htmltext = "30419-09.htm"

                    LEIKAN -> if (cond == 2)
                        htmltext = "30382-01.htm"
                    else if (cond == 3)
                        htmltext = if (!st.hasQuestItems(MOONSTONE_BEAST_MOLAR)) "30382-05.htm" else "30382-06.htm"
                    else if (cond == 4) {
                        htmltext = "30382-07.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(MOONSTONE_BEAST_MOLAR, -1)
                        st.takeItems(LEIKAN_NOTE, -1)
                    } else if (cond == 5)
                        htmltext = "30382-09.htm"
                    else if (cond > 5)
                        htmltext = "30382-08.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (npc.npcId == 20369) {
            if (st.getInt("cond") == 3 && st.dropItemsAlways(MOONSTONE_BEAST_MOLAR, 1, 10))
                st["cond"] = "4"
        } else if (st.getInt("cond") == 5) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(SHILEN_TEARS, 1)
        }

        return null
    }

    companion object {
        private val qn = "Q411_PathToAnAssassin"

        // Items
        private val SHILEN_CALL = 1245
        private val ARKENIA_LETTER = 1246
        private val LEIKAN_NOTE = 1247
        private val MOONSTONE_BEAST_MOLAR = 1248
        private val SHILEN_TEARS = 1250
        private val ARKENIA_RECOMMENDATION = 1251
        private val IRON_HEART = 1252

        // NPCs
        private val TRISKEL = 30416
        private val ARKENIA = 30419
        private val LEIKAN = 30382
    }
}