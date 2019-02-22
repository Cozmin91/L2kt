package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q296_TarantulasSpiderSilk : Quest(296, "Tarantula's Spider Silk") {
    init {

        setItemsIds(TARANTULA_SPIDER_SILK, TARANTULA_SPINNERETTE)

        addStartNpc(MION)
        addTalkId(MION, DEFENDER_NATHAN)

        addKillId(20394, 20403, 20508) // Crimson Tarantula, Hunter Tarantula, Plunder arantula
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30519-03.htm", ignoreCase = true)) {
            if (st.hasAtLeastOneQuestItem(RING_OF_RACCOON, RING_OF_FIREFLY)) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
            } else
                htmltext = "30519-03a.htm"
        } else if (event.equals("30519-06.htm", ignoreCase = true)) {
            st.takeItems(TARANTULA_SPIDER_SILK, -1)
            st.takeItems(TARANTULA_SPINNERETTE, -1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30548-02.htm", ignoreCase = true)) {
            val count = st.getQuestItemsCount(TARANTULA_SPINNERETTE)
            if (count > 0) {
                htmltext = "30548-03.htm"
                st.takeItems(TARANTULA_SPINNERETTE, -1)
                st.giveItems(TARANTULA_SPIDER_SILK, count * (15 + Rnd[10]))
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30519-01.htm" else "30519-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                MION -> {
                    val count = st.getQuestItemsCount(TARANTULA_SPIDER_SILK)
                    if (count == 0)
                        htmltext = "30519-04.htm"
                    else {
                        htmltext = "30519-05.htm"
                        st.takeItems(TARANTULA_SPIDER_SILK, -1)
                        st.rewardItems(57, (if (count >= 10) 2000 else 0) + count * 30)
                    }
                }

                DEFENDER_NATHAN -> htmltext = "30548-01.htm"
            }
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val rnd = Rnd[100]
        if (rnd > 95)
            st.dropItemsAlways(TARANTULA_SPINNERETTE, 1, 0)
        else if (rnd > 45)
            st.dropItemsAlways(TARANTULA_SPIDER_SILK, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q296_TarantulasSpiderSilk"

        // NPCs
        private val MION = 30519
        private val DEFENDER_NATHAN = 30548

        // Quest Items
        private val TARANTULA_SPIDER_SILK = 1493
        private val TARANTULA_SPINNERETTE = 1494

        // Items
        private val RING_OF_RACCOON = 1508
        private val RING_OF_FIREFLY = 1509
    }
}