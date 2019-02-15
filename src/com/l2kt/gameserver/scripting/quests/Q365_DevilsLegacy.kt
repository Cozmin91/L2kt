package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q365_DevilsLegacy : Quest(365, "Devil's Legacy") {
    init {

        setItemsIds(PIRATE_TREASURE_CHEST)

        addStartNpc(RANDOLF)
        addTalkId(RANDOLF, COLLOB)

        addKillId(20836, 20845, 21629, 21630) // Pirate Zombie && Pirate Zombie Captain.
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30095-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30095-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_GIVEUP)
            st.exitQuest(true)
        } else if (event.equals("30092-05.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(PIRATE_TREASURE_CHEST))
                htmltext = "30092-02.htm"
            else if (st.getQuestItemsCount(57) < 600)
                htmltext = "30092-03.htm"
            else {
                st.takeItems(PIRATE_TREASURE_CHEST, 1)
                st.takeItems(57, 600)

                val i0: Int
                if (Rnd[100] < 80) {
                    i0 = Rnd[100]
                    if (i0 < 1)
                        st.giveItems(955, 1)
                    else if (i0 < 4)
                        st.giveItems(956, 1)
                    else if (i0 < 36)
                        st.giveItems(1868, 1)
                    else if (i0 < 68)
                        st.giveItems(1884, 1)
                    else
                        st.giveItems(1872, 1)

                    htmltext = "30092-05.htm"
                } else {
                    i0 = Rnd[1000]
                    if (i0 < 10)
                        st.giveItems(951, 1)
                    else if (i0 < 40)
                        st.giveItems(952, 1)
                    else if (i0 < 60)
                        st.giveItems(955, 1)
                    else if (i0 < 260)
                        st.giveItems(956, 1)
                    else if (i0 < 445)
                        st.giveItems(1879, 1)
                    else if (i0 < 630)
                        st.giveItems(1880, 1)
                    else if (i0 < 815)
                        st.giveItems(1882, 1)
                    else
                        st.giveItems(1881, 1)

                    htmltext = "30092-06.htm"

                    // Curse effect !
                    val skill = SkillTable.getInfo(4082, 1)
                    if (skill != null && player.getFirstEffect(skill) == null)
                        skill.getEffects(npc, player)
                }
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 39) "30095-00.htm" else "30095-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                RANDOLF -> if (!st.hasQuestItems(PIRATE_TREASURE_CHEST))
                    htmltext = "30095-03.htm"
                else {
                    htmltext = "30095-05.htm"

                    val reward = st.getQuestItemsCount(PIRATE_TREASURE_CHEST) * 400

                    st.takeItems(PIRATE_TREASURE_CHEST, -1)
                    st.rewardItems(57, reward + 19800)
                }

                COLLOB -> htmltext = "30092-01.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(PIRATE_TREASURE_CHEST, 1, 0, if (npc.npcId == 20836) 360000 else 520000)

        return null
    }

    companion object {
        private val qn = "Q365_DevilsLegacy"

        // NPCs
        private val RANDOLF = 30095
        private val COLLOB = 30092

        // Item
        private val PIRATE_TREASURE_CHEST = 5873
    }
}