package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q413_PathToAShillienOracle : Quest(413, "Path to a Shillien Oracle") {
    init {

        setItemsIds(
            SIDRA_LETTER,
            BLANK_SHEET,
            BLOODY_RUNE,
            GARMIEL_BOOK,
            PRAYER_OF_ADONIUS,
            PENITENT_MARK,
            ASHEN_BONES,
            ANDARIEL_BOOK
        )

        addStartNpc(SIDRA)
        addTalkId(SIDRA, ADONIUS, TALBOT)

        addKillId(20776, 20457, 20458, 20514, 20515)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30330-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.DARK_MYSTIC)
                htmltext = if (player.classId == ClassId.SHILLIEN_ORACLE) "30330-02a.htm" else "30330-03.htm"
            else if (player.level < 19)
                htmltext = "30330-02.htm"
            else if (st.hasQuestItems(ORB_OF_ABYSS))
                htmltext = "30330-04.htm"
        } else if (event.equals("30330-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(SIDRA_LETTER, 1)
        } else if (event.equals("30377-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SIDRA_LETTER, 1)
            st.giveItems(BLANK_SHEET, 5)
        } else if (event.equals("30375-04.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(PRAYER_OF_ADONIUS, 1)
            st.giveItems(PENITENT_MARK, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30330-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    SIDRA -> if (cond == 1)
                        htmltext = "30330-07.htm"
                    else if (cond > 1 && cond < 4)
                        htmltext = "30330-08.htm"
                    else if (cond > 3 && cond < 7)
                        htmltext = "30330-09.htm"
                    else if (cond == 7) {
                        htmltext = "30330-10.htm"
                        st.takeItems(ANDARIEL_BOOK, 1)
                        st.takeItems(GARMIEL_BOOK, 1)
                        st.giveItems(ORB_OF_ABYSS, 1)
                        st.rewardExpAndSp(3200, 3120)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    TALBOT -> if (cond == 1)
                        htmltext = "30377-01.htm"
                    else if (cond == 2)
                        htmltext = if (st.hasQuestItems(BLOODY_RUNE)) "30377-04.htm" else "30377-03.htm"
                    else if (cond == 3) {
                        htmltext = "30377-05.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BLOODY_RUNE, -1)
                        st.giveItems(GARMIEL_BOOK, 1)
                        st.giveItems(PRAYER_OF_ADONIUS, 1)
                    } else if (cond > 3 && cond < 7)
                        htmltext = "30377-06.htm"
                    else if (cond == 7)
                        htmltext = "30377-07.htm"

                    ADONIUS -> if (cond == 4)
                        htmltext = "30375-01.htm"
                    else if (cond == 5)
                        htmltext = if (st.hasQuestItems(ASHEN_BONES)) "30375-05.htm" else "30375-06.htm"
                    else if (cond == 6) {
                        htmltext = "30375-07.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ASHEN_BONES, -1)
                        st.takeItems(PENITENT_MARK, -1)
                        st.giveItems(ANDARIEL_BOOK, 1)
                    } else if (cond == 7)
                        htmltext = "30375-08.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (npc.npcId == 20776) {
            if (st.getInt("cond") == 2) {
                st.takeItems(BLANK_SHEET, 1)
                if (st.dropItemsAlways(BLOODY_RUNE, 1, 5))
                    st["cond"] = "3"
            }
        } else if (st.getInt("cond") == 5 && st.dropItemsAlways(ASHEN_BONES, 1, 10))
            st["cond"] = "6"

        return null
    }

    companion object {
        private val qn = "Q413_PathToAShillienOracle"

        // Items
        private val SIDRA_LETTER = 1262
        private val BLANK_SHEET = 1263
        private val BLOODY_RUNE = 1264
        private val GARMIEL_BOOK = 1265
        private val PRAYER_OF_ADONIUS = 1266
        private val PENITENT_MARK = 1267
        private val ASHEN_BONES = 1268
        private val ANDARIEL_BOOK = 1269
        private val ORB_OF_ABYSS = 1270

        // NPCs
        private val SIDRA = 30330
        private val ADONIUS = 30375
        private val TALBOT = 30377
    }
}