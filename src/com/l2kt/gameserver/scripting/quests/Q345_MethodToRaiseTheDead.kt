package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q345_MethodToRaiseTheDead : Quest(345, "Method to Raise the Dead") {
    init {

        setItemsIds(
            VICTIM_ARM_BONE,
            VICTIM_THIGH_BONE,
            VICTIM_SKULL,
            VICTIM_RIB_BONE,
            VICTIM_SPINE,
            POWDER_TO_SUMMON_DEAD_SOULS,
            USELESS_BONE_PIECES
        )

        addStartNpc(DOROTHY)
        addTalkId(DOROTHY, XENOVIA, MEDIUM_JAR, ORPHEUS)

        addKillId(20789, 20791)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30970-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30970-06.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30912-04.htm", ignoreCase = true)) {
            if (player.adena >= 1000) {
                htmltext = "30912-03.htm"
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(57, 1000)
                st.giveItems(POWDER_TO_SUMMON_DEAD_SOULS, 1)
            }
        } else if (event.equals("30973-04.htm", ignoreCase = true)) {
            if (st.getInt("cond") == 3) {
                val chance = Rnd[3]
                if (chance == 0) {
                    st["cond"] = "6"
                    htmltext = "30973-02a.htm"
                } else if (chance == 1) {
                    st["cond"] = "6"
                    htmltext = "30973-02b.htm"
                } else {
                    st["cond"] = "7"
                    htmltext = "30973-02c.htm"
                }

                st.takeItems(POWDER_TO_SUMMON_DEAD_SOULS, -1)
                st.takeItems(VICTIM_ARM_BONE, -1)
                st.takeItems(VICTIM_THIGH_BONE, -1)
                st.takeItems(VICTIM_SKULL, -1)
                st.takeItems(VICTIM_RIB_BONE, -1)
                st.takeItems(VICTIM_SPINE, -1)

                st.playSound(QuestState.SOUND_MIDDLE)
            }
        } else if (event.equals("30971-02a.htm", ignoreCase = true)) {
            if (st.hasQuestItems(USELESS_BONE_PIECES))
                htmltext = "30971-02.htm"
        } else if (event.equals("30971-03.htm", ignoreCase = true)) {
            if (st.hasQuestItems(USELESS_BONE_PIECES)) {
                val amount = st.getQuestItemsCount(USELESS_BONE_PIECES) * 104
                st.takeItems(USELESS_BONE_PIECES, -1)
                st.rewardItems(57, amount)
            } else
                htmltext = "30971-02a.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 35) "30970-00.htm" else "30970-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    DOROTHY -> if (cond == 1)
                        htmltext = if (!st.hasQuestItems(
                                VICTIM_ARM_BONE,
                                VICTIM_THIGH_BONE,
                                VICTIM_SKULL,
                                VICTIM_RIB_BONE,
                                VICTIM_SPINE
                            )
                        ) "30970-04.htm" else "30970-05.htm"
                    else if (cond == 2)
                        htmltext = "30970-07.htm"
                    else if (cond > 2 && cond < 6)
                        htmltext = "30970-08.htm"
                    else {
                        // Shared part between cond 6 and 7.
                        val amount = st.getQuestItemsCount(USELESS_BONE_PIECES) * 70
                        st.takeItems(USELESS_BONE_PIECES, -1)

                        // Scaried little girl
                        if (cond == 7) {
                            htmltext = "30970-10.htm"
                            st.rewardItems(57, 3040 + amount)

                            // Reward can be either an Imperial Diamond or bills.
                            if (Rnd[100] < 10)
                                st.giveItems(IMPERIAL_DIAMOND, 1)
                            else
                                st.giveItems(BILL_OF_IASON_HEINE, 5)
                        } else {
                            htmltext = "30970-09.htm"
                            st.rewardItems(57, 5390 + amount)
                            st.giveItems(BILL_OF_IASON_HEINE, 3)
                        }// Friends of Dorothy
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    XENOVIA -> if (cond == 2)
                        htmltext = "30912-01.htm"
                    else if (cond > 2)
                        htmltext = "30912-06.htm"

                    MEDIUM_JAR -> htmltext = "30973-01.htm"

                    ORPHEUS -> htmltext = "30971-01.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (Rnd[4] == 0) {
            val randomPart = Rnd[VICTIM_ARM_BONE, VICTIM_SPINE]
            if (!st.hasQuestItems(randomPart)) {
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(randomPart, 1)
                return null
            }
        }
        st.dropItemsAlways(USELESS_BONE_PIECES, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q345_MethodToRaiseTheDead"

        // Items
        private val VICTIM_ARM_BONE = 4274
        private val VICTIM_THIGH_BONE = 4275
        private val VICTIM_SKULL = 4276
        private val VICTIM_RIB_BONE = 4277
        private val VICTIM_SPINE = 4278
        private val USELESS_BONE_PIECES = 4280
        private val POWDER_TO_SUMMON_DEAD_SOULS = 4281

        // NPCs
        private val XENOVIA = 30912
        private val DOROTHY = 30970
        private val ORPHEUS = 30971
        private val MEDIUM_JAR = 30973

        // Rewards
        private val BILL_OF_IASON_HEINE = 4310
        private val IMPERIAL_DIAMOND = 3456
    }
}