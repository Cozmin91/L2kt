package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q034_InSearchOfCloth : Quest(34, "In Search of Cloth") {
    init {

        setItemsIds(SPINNERET, SPIDERSILK)

        addStartNpc(RADIA)
        addTalkId(RADIA, RALFORD, VARAN)

        addKillId(TRISALIM_SPIDER, TRISALIM_TARANTULA)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30088-1.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30294-1.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30088-3.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30165-1.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30165-3.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SPINNERET, 10)
            st.giveItems(SPIDERSILK, 1)
        } else if (event.equals("30088-5.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(SUEDE) >= 3000 && st.getQuestItemsCount(THREAD) >= 5000 && st.hasQuestItems(
                    SPIDERSILK
                )
            ) {
                st.takeItems(SPIDERSILK, 1)
                st.takeItems(SUEDE, 3000)
                st.takeItems(THREAD, 5000)
                st.giveItems(MYSTERIOUS_CLOTH, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "30088-4a.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level >= 60) {
                val fwear = player.getQuestState("Q037_MakeFormalWear")
                if (fwear != null && fwear.getInt("cond") == 6)
                    htmltext = "30088-0.htm"
                else
                    htmltext = "30088-0a.htm"
            } else
                htmltext = "30088-0b.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    RADIA -> if (cond == 1)
                        htmltext = "30088-1a.htm"
                    else if (cond == 2)
                        htmltext = "30088-2.htm"
                    else if (cond == 3)
                        htmltext = "30088-3a.htm"
                    else if (cond == 6) {
                        if (st.getQuestItemsCount(SUEDE) < 3000 || st.getQuestItemsCount(THREAD) < 5000 || !st.hasQuestItems(
                                SPIDERSILK
                            )
                        )
                            htmltext = "30088-4a.htm"
                        else
                            htmltext = "30088-4.htm"
                    }

                    VARAN -> if (cond == 1)
                        htmltext = "30294-0.htm"
                    else if (cond > 1)
                        htmltext = "30294-1a.htm"

                    RALFORD -> if (cond == 3)
                        htmltext = "30165-0.htm"
                    else if (cond == 4 && st.getQuestItemsCount(SPINNERET) < 10)
                        htmltext = "30165-1a.htm"
                    else if (cond == 5)
                        htmltext = "30165-2.htm"
                    else if (cond > 5)
                        htmltext = "30165-3a.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "4") ?: return null

        if (st.dropItems(SPINNERET, 1, 10, 500000))
            st["cond"] = "5"

        return null
    }

    companion object {
        private const val qn = "Q034_InSearchOfCloth"

        // NPCs
        private const val RADIA = 30088
        private const val RALFORD = 30165
        private const val VARAN = 30294

        // Monsters
        private const val TRISALIM_SPIDER = 20560
        private const val TRISALIM_TARANTULA = 20561

        // Items
        private const val SPINNERET = 7528
        private const val SUEDE = 1866
        private const val THREAD = 1868
        private const val SPIDERSILK = 7161

        // Rewards
        private const val MYSTERIOUS_CLOTH = 7076
    }
}