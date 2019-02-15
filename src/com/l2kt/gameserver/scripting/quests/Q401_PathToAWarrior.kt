package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q401_PathToAWarrior : Quest(401, "Path to a Warrior") {
    init {

        setItemsIds(
            AURON_LETTER,
            WARRIOR_GUILD_MARK,
            RUSTED_BRONZE_SWORD_1,
            RUSTED_BRONZE_SWORD_2,
            RUSTED_BRONZE_SWORD_3,
            SIMPLON_LETTER,
            POISON_SPIDER_LEG
        )

        addStartNpc(AURON)
        addTalkId(AURON, SIMPLON)

        addKillId(20035, 20038, 20042, 20043)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30010-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.HUMAN_FIGHTER)
                htmltext = if (player.classId == ClassId.WARRIOR) "30010-03.htm" else "30010-02b.htm"
            else if (player.level < 19)
                htmltext = "30010-02.htm"
            else if (st.hasQuestItems(MEDALLION_OF_WARRIOR))
                htmltext = "30010-04.htm"
        } else if (event.equals("30010-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(AURON_LETTER, 1)
        } else if (event.equals("30253-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(AURON_LETTER, 1)
            st.giveItems(WARRIOR_GUILD_MARK, 1)
        } else if (event.equals("30010-11.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(RUSTED_BRONZE_SWORD_2, 1)
            st.takeItems(SIMPLON_LETTER, 1)
            st.giveItems(RUSTED_BRONZE_SWORD_3, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30010-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    AURON -> if (cond == 1)
                        htmltext = "30010-07.htm"
                    else if (cond == 2 || cond == 3)
                        htmltext = "30010-08.htm"
                    else if (cond == 4)
                        htmltext = "30010-09.htm"
                    else if (cond == 5)
                        htmltext = "30010-12.htm"
                    else if (cond == 6) {
                        htmltext = "30010-13.htm"
                        st.takeItems(POISON_SPIDER_LEG, -1)
                        st.takeItems(RUSTED_BRONZE_SWORD_3, 1)
                        st.giveItems(MEDALLION_OF_WARRIOR, 1)
                        st.rewardExpAndSp(3200, 1500)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    SIMPLON -> if (cond == 1)
                        htmltext = "30253-01.htm"
                    else if (cond == 2) {
                        if (!st.hasQuestItems(RUSTED_BRONZE_SWORD_1))
                            htmltext = "30253-03.htm"
                        else if (st.getQuestItemsCount(RUSTED_BRONZE_SWORD_1) <= 9)
                            htmltext = "30253-03b.htm"
                    } else if (cond == 3) {
                        htmltext = "30253-04.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(RUSTED_BRONZE_SWORD_1, 10)
                        st.takeItems(WARRIOR_GUILD_MARK, 1)
                        st.giveItems(RUSTED_BRONZE_SWORD_2, 1)
                        st.giveItems(SIMPLON_LETTER, 1)
                    } else if (cond == 4)
                        htmltext = "30253-05.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20035, 20042 -> if (st.getInt("cond") == 2 && st.dropItems(RUSTED_BRONZE_SWORD_1, 1, 10, 400000))
                st["cond"] = "3"

            20038, 20043 -> if (st.getInt("cond") == 5 && st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == RUSTED_BRONZE_SWORD_3)
                if (st.dropItemsAlways(POISON_SPIDER_LEG, 1, 20))
                    st["cond"] = "6"
        }

        return null
    }

    companion object {
        private val qn = "Q401_PathToAWarrior"

        // Items
        private val AURON_LETTER = 1138
        private val WARRIOR_GUILD_MARK = 1139
        private val RUSTED_BRONZE_SWORD_1 = 1140
        private val RUSTED_BRONZE_SWORD_2 = 1141
        private val RUSTED_BRONZE_SWORD_3 = 1142
        private val SIMPLON_LETTER = 1143
        private val POISON_SPIDER_LEG = 1144
        private val MEDALLION_OF_WARRIOR = 1145

        // NPCs
        private val AURON = 30010
        private val SIMPLON = 30253
    }
}