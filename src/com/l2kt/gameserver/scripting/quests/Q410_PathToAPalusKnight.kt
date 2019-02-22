package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q410_PathToAPalusKnight : Quest(410, "Path to a Palus Knight") {
    init {

        setItemsIds(
            PALUS_TALISMAN,
            LYCANTHROPE_SKULL,
            VIRGIL_LETTER,
            MORTE_TALISMAN,
            PREDATOR_CARAPACE,
            ARACHNID_TRACKER_SILK,
            COFFIN_OF_ETERNAL_REST
        )

        addStartNpc(VIRGIL)
        addTalkId(VIRGIL, KALINTA)

        addKillId(POISON_SPIDER, ARACHNID_TRACKER, LYCANTHROPE)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30329-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.DARK_FIGHTER)
                htmltext = if (player.classId == ClassId.PALUS_KNIGHT) "30329-02a.htm" else "30329-03.htm"
            else if (player.level < 19)
                htmltext = "30329-02.htm"
            else if (st.hasQuestItems(GAZE_OF_ABYSS))
                htmltext = "30329-04.htm"
        } else if (event.equals("30329-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(PALUS_TALISMAN, 1)
        } else if (event.equals("30329-10.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LYCANTHROPE_SKULL, -1)
            st.takeItems(PALUS_TALISMAN, 1)
            st.giveItems(VIRGIL_LETTER, 1)
        } else if (event.equals("30422-02.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(VIRGIL_LETTER, 1)
            st.giveItems(MORTE_TALISMAN, 1)
        } else if (event.equals("30422-06.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ARACHNID_TRACKER_SILK, -1)
            st.takeItems(MORTE_TALISMAN, 1)
            st.takeItems(PREDATOR_CARAPACE, -1)
            st.giveItems(COFFIN_OF_ETERNAL_REST, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30329-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    VIRGIL -> if (cond == 1)
                        htmltext = if (!st.hasQuestItems(LYCANTHROPE_SKULL)) "30329-07.htm" else "30329-08.htm"
                    else if (cond == 2)
                        htmltext = "30329-09.htm"
                    else if (cond > 2 && cond < 6)
                        htmltext = "30329-12.htm"
                    else if (cond == 6) {
                        htmltext = "30329-11.htm"
                        st.takeItems(COFFIN_OF_ETERNAL_REST, 1)
                        st.giveItems(GAZE_OF_ABYSS, 1)
                        st.rewardExpAndSp(3200, 1500)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    KALINTA -> if (cond == 3)
                        htmltext = "30422-01.htm"
                    else if (cond == 4) {
                        if (!st.hasQuestItems(ARACHNID_TRACKER_SILK) || !st.hasQuestItems(PREDATOR_CARAPACE))
                            htmltext = "30422-03.htm"
                        else
                            htmltext = "30422-04.htm"
                    } else if (cond == 5)
                        htmltext = "30422-05.htm"
                    else if (cond == 6)
                        htmltext = "30422-06.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            LYCANTHROPE -> if (st.getInt("cond") == 1 && st.dropItemsAlways(LYCANTHROPE_SKULL, 1, 13))
                st["cond"] = "2"

            ARACHNID_TRACKER -> if (st.getInt("cond") == 4 && st.dropItemsAlways(
                    ARACHNID_TRACKER_SILK,
                    1,
                    5
                ) && st.hasQuestItems(PREDATOR_CARAPACE)
            )
                st["cond"] = "5"

            POISON_SPIDER -> if (st.getInt("cond") == 4 && st.dropItemsAlways(
                    PREDATOR_CARAPACE,
                    1,
                    1
                ) && st.getQuestItemsCount(ARACHNID_TRACKER_SILK) == 5
            )
                st["cond"] = "5"
        }

        return null
    }

    companion object {
        private val qn = "Q410_PathToAPalusKnight"

        // Items
        private val PALUS_TALISMAN = 1237
        private val LYCANTHROPE_SKULL = 1238
        private val VIRGIL_LETTER = 1239
        private val MORTE_TALISMAN = 1240
        private val PREDATOR_CARAPACE = 1241
        private val ARACHNID_TRACKER_SILK = 1242
        private val COFFIN_OF_ETERNAL_REST = 1243
        private val GAZE_OF_ABYSS = 1244

        // NPCs
        private val KALINTA = 30422
        private val VIRGIL = 30329

        // Monsters
        private val POISON_SPIDER = 20038
        private val ARACHNID_TRACKER = 20043
        private val LYCANTHROPE = 20049
    }
}