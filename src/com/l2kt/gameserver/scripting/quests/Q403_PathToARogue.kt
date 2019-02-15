package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q403_PathToARogue : Quest(403, "Path to a Rogue") {
    init {

        setItemsIds(
            BEZIQUE_LETTER,
            NETI_BOW,
            NETI_DAGGER,
            SPARTOI_BONES,
            HORSESHOE_OF_LIGHT,
            MOST_WANTED_LIST,
            STOLEN_JEWELRY,
            STOLEN_TOMES,
            STOLEN_RING,
            STOLEN_NECKLACE
        )

        addStartNpc(BEZIQUE)
        addTalkId(BEZIQUE, NETI)

        addKillId(20035, 20042, 20045, 20051, 20054, 20060, 27038)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30379-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.HUMAN_FIGHTER)
                htmltext = if (player.classId == ClassId.ROGUE) "30379-02a.htm" else "30379-02.htm"
            else if (player.level < 19)
                htmltext = "30379-02.htm"
            else if (st.hasQuestItems(BEZIQUE_RECOMMENDATION))
                htmltext = "30379-04.htm"
        } else if (event.equals("30379-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(BEZIQUE_LETTER, 1)
        } else if (event.equals("30425-05.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(NETI_BOW, 1)
            st.giveItems(NETI_DAGGER, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30379-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    BEZIQUE -> if (cond == 1)
                        htmltext = "30379-07.htm"
                    else if (cond == 2 || cond == 3)
                        htmltext = "30379-10.htm"
                    else if (cond == 4) {
                        htmltext = "30379-08.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(HORSESHOE_OF_LIGHT, 1)
                        st.giveItems(MOST_WANTED_LIST, 1)
                    } else if (cond == 5)
                        htmltext = "30379-11.htm"
                    else if (cond == 6) {
                        htmltext = "30379-09.htm"
                        st.takeItems(NETI_BOW, 1)
                        st.takeItems(NETI_DAGGER, 1)
                        st.takeItems(STOLEN_JEWELRY, 1)
                        st.takeItems(STOLEN_NECKLACE, 1)
                        st.takeItems(STOLEN_RING, 1)
                        st.takeItems(STOLEN_TOMES, 1)
                        st.giveItems(BEZIQUE_RECOMMENDATION, 1)
                        st.rewardExpAndSp(3200, 1500)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    NETI -> if (cond == 1)
                        htmltext = "30425-01.htm"
                    else if (cond == 2)
                        htmltext = "30425-06.htm"
                    else if (cond == 3) {
                        htmltext = "30425-07.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SPARTOI_BONES, 10)
                        st.giveItems(HORSESHOE_OF_LIGHT, 1)
                    } else if (cond > 3)
                        htmltext = "30425-08.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val equippedItemId = st.getItemEquipped(Inventory.PAPERDOLL_RHAND)
        if (equippedItemId != NETI_BOW && equippedItemId != NETI_DAGGER)
            return null

        when (npc.npcId) {
            20035, 20045, 20051 -> if (st.getInt("cond") == 2 && st.dropItems(SPARTOI_BONES, 1, 10, 200000))
                st["cond"] = "3"

            20042 -> if (st.getInt("cond") == 2 && st.dropItems(SPARTOI_BONES, 1, 10, 300000))
                st["cond"] = "3"

            20054, 20060 -> if (st.getInt("cond") == 2 && st.dropItems(SPARTOI_BONES, 1, 10, 800000))
                st["cond"] = "3"

            27038 -> if (st.getInt("cond") == 5) {
                val randomItem = Rnd[STOLEN_JEWELRY, STOLEN_NECKLACE]

                if (!st.hasQuestItems(randomItem)) {
                    st.giveItems(randomItem, 1)

                    if (st.hasQuestItems(STOLEN_JEWELRY, STOLEN_TOMES, STOLEN_RING, STOLEN_NECKLACE)) {
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else
                        st.playSound(QuestState.SOUND_ITEMGET)
                }
            }
        }

        return null
    }

    companion object {
        private val qn = "Q403_PathToARogue"

        // Items
        private val BEZIQUE_LETTER = 1180
        private val NETI_BOW = 1181
        private val NETI_DAGGER = 1182
        private val SPARTOI_BONES = 1183
        private val HORSESHOE_OF_LIGHT = 1184
        private val MOST_WANTED_LIST = 1185
        private val STOLEN_JEWELRY = 1186
        private val STOLEN_TOMES = 1187
        private val STOLEN_RING = 1188
        private val STOLEN_NECKLACE = 1189
        private val BEZIQUE_RECOMMENDATION = 1190

        // NPCs
        private val BEZIQUE = 30379
        private val NETI = 30425
    }
}