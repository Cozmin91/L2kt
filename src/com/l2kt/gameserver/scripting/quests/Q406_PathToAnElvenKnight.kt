package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q406_PathToAnElvenKnight : Quest(406, "Path to an Elven Knight") {
    init {

        setItemsIds(SORIUS_LETTER, KLUTO_BOX, TOPAZ_PIECE, EMERALD_PIECE, KLUTO_MEMO)

        addStartNpc(SORIUS)
        addTalkId(SORIUS, KLUTO)

        addKillId(20035, 20042, 20045, 20051, 20054, 20060, 20782)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30327-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.ELVEN_FIGHTER)
                htmltext = if (player.classId == ClassId.ELVEN_KNIGHT) "30327-02a.htm" else "30327-02.htm"
            else if (player.level < 19)
                htmltext = "30327-03.htm"
            else if (st.hasQuestItems(ELVEN_KNIGHT_BROOCH))
                htmltext = "30327-04.htm"
        } else if (event.equals("30327-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30317-02.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SORIUS_LETTER, 1)
            st.giveItems(KLUTO_MEMO, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30327-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    SORIUS -> if (cond == 1)
                        htmltext = if (!st.hasQuestItems(TOPAZ_PIECE)) "30327-07.htm" else "30327-08.htm"
                    else if (cond == 2) {
                        htmltext = "30327-09.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(SORIUS_LETTER, 1)
                    } else if (cond > 2 && cond < 6)
                        htmltext = "30327-11.htm"
                    else if (cond == 6) {
                        htmltext = "30327-10.htm"
                        st.takeItems(KLUTO_BOX, 1)
                        st.takeItems(KLUTO_MEMO, 1)
                        st.giveItems(ELVEN_KNIGHT_BROOCH, 1)
                        st.rewardExpAndSp(3200, 2280)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    KLUTO -> if (cond == 3)
                        htmltext = "30317-01.htm"
                    else if (cond == 4)
                        htmltext = if (!st.hasQuestItems(EMERALD_PIECE)) "30317-03.htm" else "30317-04.htm"
                    else if (cond == 5) {
                        htmltext = "30317-05.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(EMERALD_PIECE, -1)
                        st.takeItems(TOPAZ_PIECE, -1)
                        st.giveItems(KLUTO_BOX, 1)
                    } else if (cond == 6)
                        htmltext = "30317-06.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20035, 20042, 20045, 20051, 20054, 20060 -> if (st.getInt("cond") == 1 && st.dropItems(
                    TOPAZ_PIECE,
                    1,
                    20,
                    700000
                )
            )
                st["cond"] = "2"

            20782 -> if (st.getInt("cond") == 4 && st.dropItems(EMERALD_PIECE, 1, 20, 500000))
                st["cond"] = "5"
        }

        return null
    }

    companion object {
        private val qn = "Q406_PathToAnElvenKnight"

        // Items
        private val SORIUS_LETTER = 1202
        private val KLUTO_BOX = 1203
        private val ELVEN_KNIGHT_BROOCH = 1204
        private val TOPAZ_PIECE = 1205
        private val EMERALD_PIECE = 1206
        private val KLUTO_MEMO = 1276

        // NPCs
        private val SORIUS = 30327
        private val KLUTO = 30317
    }
}