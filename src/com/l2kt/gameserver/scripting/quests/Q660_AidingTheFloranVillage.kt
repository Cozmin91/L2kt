package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q660_AidingTheFloranVillage : Quest(660, "Aiding the Floran Village") {
    init {

        setItemsIds(WATCHING_EYES, LIZARDMEN_SCALE, GOLEM_SHARD)

        addStartNpc(MARIA, ALEX)
        addTalkId(MARIA, ALEX)

        addKillId(
            CURSED_SEER,
            PLAIN_WATCHMAN,
            ROCK_GOLEM,
            LIZARDMEN_SHAMAN,
            LIZARDMEN_SUPPLIER,
            LIZARDMEN_COMMANDER,
            LIZARDMEN_AGENT
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30608-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30291-02.htm", ignoreCase = true)) {
            if (player.level < 30)
                htmltext = "30291-02a.htm"
            else {
                st.state = Quest.STATE_STARTED
                st["cond"] = "2"
                st.playSound(QuestState.SOUND_ACCEPT)
            }
        } else if (event.equals("30291-05.htm", ignoreCase = true)) {
            val count =
                st.getQuestItemsCount(WATCHING_EYES) + st.getQuestItemsCount(LIZARDMEN_SCALE) + st.getQuestItemsCount(
                    GOLEM_SHARD
                )
            if (count == 0)
                htmltext = "30291-05a.htm"
            else {
                st.takeItems(GOLEM_SHARD, -1)
                st.takeItems(LIZARDMEN_SCALE, -1)
                st.takeItems(WATCHING_EYES, -1)
                st.rewardItems(ADENA, count * 100 + if (count >= 45) 9000 else 0)
            }
        } else if (event.equals("30291-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30291-11.htm", ignoreCase = true)) {
            if (!verifyAndRemoveItems(st, 100))
                htmltext = "30291-11a.htm"
            else {
                if (Rnd[10] < 8)
                    st.rewardItems(ADENA, 1000)
                else {
                    st.rewardItems(ADENA, 13000)
                    st.rewardItems(ENCHANT_ARMOR_D, 1)
                }
            }
        } else if (event.equals("30291-12.htm", ignoreCase = true)) {
            if (!verifyAndRemoveItems(st, 200))
                htmltext = "30291-12a.htm"
            else {
                val luck = Rnd[15]
                if (luck < 8)
                    st.rewardItems(ADENA, 2000)
                else if (luck < 12) {
                    st.rewardItems(ADENA, 20000)
                    st.rewardItems(ENCHANT_ARMOR_D, 1)
                } else
                    st.rewardItems(ENCHANT_WEAPON_D, 1)
            }
        } else if (event.equals("30291-13.htm", ignoreCase = true)) {
            if (!verifyAndRemoveItems(st, 500))
                htmltext = "30291-13a.htm"
            else {
                if (Rnd[10] < 8)
                    st.rewardItems(ADENA, 5000)
                else {
                    st.rewardItems(ADENA, 45000)
                    st.rewardItems(ENCHANT_WEAPON_D, 1)
                }
            }
        } else if (event.equals("30291-17.htm", ignoreCase = true)) {
            val count =
                st.getQuestItemsCount(WATCHING_EYES) + st.getQuestItemsCount(LIZARDMEN_SCALE) + st.getQuestItemsCount(
                    GOLEM_SHARD
                )
            if (count != 0) {
                htmltext = "30291-17a.htm"
                st.takeItems(WATCHING_EYES, -1)
                st.takeItems(LIZARDMEN_SCALE, -1)
                st.takeItems(GOLEM_SHARD, -1)
                st.rewardItems(ADENA, count * 100 + if (count >= 45) 9000 else 0)
            }
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> when (npc.npcId) {
                MARIA -> htmltext = if (player.level < 30) "30608-01.htm" else "30608-02.htm"

                ALEX -> htmltext = "30291-01.htm"
            }

            Quest.STATE_STARTED -> when (npc.npcId) {
                MARIA -> htmltext = "30608-06.htm"

                ALEX -> {
                    val cond = st.getInt("cond")
                    if (cond == 1) {
                        htmltext = "30291-03.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 2)
                        htmltext = if (st.hasAtLeastOneQuestItem(
                                WATCHING_EYES,
                                LIZARDMEN_SCALE,
                                GOLEM_SHARD
                            )
                        ) "30291-04.htm" else "30291-05a.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "2") ?: return null

        when (npc.npcId) {
            PLAIN_WATCHMAN, CURSED_SEER -> st.dropItems(WATCHING_EYES, 1, 0, 790000)

            ROCK_GOLEM -> st.dropItems(GOLEM_SHARD, 1, 0, 750000)

            LIZARDMEN_SHAMAN, LIZARDMEN_SUPPLIER, LIZARDMEN_AGENT, LIZARDMEN_COMMANDER -> st.dropItems(
                LIZARDMEN_SCALE,
                1,
                0,
                670000
            )
        }

        return null
    }

    companion object {
        private const val qn = "Q660_AidingTheFloranVillage"

        // NPCs
        private const val MARIA = 30608
        private const val ALEX = 30291

        // Items
        private const val WATCHING_EYES = 8074
        private const val GOLEM_SHARD = 8075
        private const val LIZARDMEN_SCALE = 8076

        // Mobs
        private const val PLAIN_WATCHMAN = 21102
        private const val ROCK_GOLEM = 21103
        private const val LIZARDMEN_SUPPLIER = 21104
        private const val LIZARDMEN_AGENT = 21105
        private const val CURSED_SEER = 21106
        private const val LIZARDMEN_COMMANDER = 21107
        private const val LIZARDMEN_SHAMAN = 20781

        // Rewards
        private const val ADENA = 57
        private const val ENCHANT_WEAPON_D = 955
        private const val ENCHANT_ARMOR_D = 956

        /**
         * This method drops items following current counts.
         * @param st The QuestState to affect.
         * @param numberToVerify The count of qItems to drop from the different categories.
         * @return false when counter isn't reached, true otherwise.
         */
        private fun verifyAndRemoveItems(st: QuestState, numberToVerify: Int): Boolean {
            val eyes = st.getQuestItemsCount(WATCHING_EYES)
            val scale = st.getQuestItemsCount(LIZARDMEN_SCALE)
            val shard = st.getQuestItemsCount(GOLEM_SHARD)

            if (eyes + scale + shard < numberToVerify)
                return false

            if (eyes >= numberToVerify)
                st.takeItems(WATCHING_EYES, numberToVerify)
            else {
                var currentNumber = numberToVerify - eyes

                st.takeItems(WATCHING_EYES, -1)
                if (scale >= currentNumber)
                    st.takeItems(LIZARDMEN_SCALE, currentNumber)
                else {
                    currentNumber -= scale
                    st.takeItems(LIZARDMEN_SCALE, -1)
                    st.takeItems(GOLEM_SHARD, currentNumber)
                }
            }
            return true
        }
    }
}