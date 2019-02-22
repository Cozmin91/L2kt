package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q381_LetsBecomeARoyalMember : Quest(381, "Lets Become a Royal Member!") {
    init {

        setItemsIds(KAIL_COIN, GOLDEN_CLOVER_COIN)

        addStartNpc(SORINT)
        addTalkId(SORINT, SANDRA)

        addKillId(21018, 27316)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30090-02.htm", ignoreCase = true))
            st["aCond"] = "1" // Alternative cond used for Sandra.
        else if (event.equals("30232-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext =
                    if (player.level < 55 || !st.hasQuestItems(COIN_COLLECTOR_MEMBERSHIP)) "30232-02.htm" else "30232-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                SORINT -> if (!st.hasQuestItems(KAIL_COIN))
                    htmltext = "30232-04.htm"
                else if (!st.hasQuestItems(COIN_ALBUM))
                    htmltext = "30232-05.htm"
                else {
                    htmltext = "30232-06.htm"
                    st.takeItems(KAIL_COIN, -1)
                    st.takeItems(COIN_ALBUM, -1)
                    st.giveItems(ROYAL_MEMBERSHIP, 1)
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                }

                SANDRA -> if (!st.hasQuestItems(COIN_ALBUM)) {
                    if (st.getInt("aCond") == 0)
                        htmltext = "30090-01.htm"
                    else {
                        if (!st.hasQuestItems(GOLDEN_CLOVER_COIN))
                            htmltext = "30090-03.htm"
                        else {
                            htmltext = "30090-04.htm"
                            st.takeItems(GOLDEN_CLOVER_COIN, -1)
                            st.giveItems(COIN_ALBUM, 1)
                        }
                    }
                } else
                    htmltext = "30090-05.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (npc.npcId == 21018)
            st.dropItems(KAIL_COIN, 1, 1, 50000)
        else if (st.getInt("aCond") == 1)
            st.dropItemsAlways(GOLDEN_CLOVER_COIN, 1, 1)

        return null
    }

    companion object {
        private val qn = "Q381_LetsBecomeARoyalMember"

        // NPCs
        private val SORINT = 30232
        private val SANDRA = 30090

        // Items
        private val KAIL_COIN = 5899
        private val COIN_ALBUM = 5900
        private val GOLDEN_CLOVER_COIN = 7569
        private val COIN_COLLECTOR_MEMBERSHIP = 3813

        // Reward
        private val ROYAL_MEMBERSHIP = 5898
    }
}