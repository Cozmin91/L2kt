package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q293_TheHiddenVeins : Quest(293, "The Hidden Veins") {
    init {

        setItemsIds(CHRYSOLITE_ORE, TORN_MAP_FRAGMENT, HIDDEN_VEIN_MAP)

        addStartNpc(FILAUR)
        addTalkId(FILAUR, CHINCHIRIN)

        addKillId(UTUKU_ORC, UTUKU_ARCHER, UTUKU_GRUNT)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30535-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30535-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30539-02.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(TORN_MAP_FRAGMENT) >= 4) {
                htmltext = "30539-03.htm"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(TORN_MAP_FRAGMENT, 4)
                st.giveItems(HIDDEN_VEIN_MAP, 1)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.DWARF)
                htmltext = "30535-00.htm"
            else if (player.level < 6)
                htmltext = "30535-01.htm"
            else
                htmltext = "30535-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                FILAUR -> {
                    val chrysoliteOres = st.getQuestItemsCount(CHRYSOLITE_ORE)
                    val hiddenVeinMaps = st.getQuestItemsCount(HIDDEN_VEIN_MAP)

                    if (chrysoliteOres + hiddenVeinMaps == 0)
                        htmltext = "30535-04.htm"
                    else {
                        if (hiddenVeinMaps > 0) {
                            if (chrysoliteOres > 0)
                                htmltext = "30535-09.htm"
                            else
                                htmltext = "30535-08.htm"
                        } else
                            htmltext = "30535-05.htm"

                        val reward = chrysoliteOres * 5 + hiddenVeinMaps * 500 + if (chrysoliteOres >= 10) 2000 else 0

                        st.takeItems(CHRYSOLITE_ORE, -1)
                        st.takeItems(HIDDEN_VEIN_MAP, -1)
                        st.rewardItems(57, reward)

                        if (player.isNewbie && st.getInt("Reward") == 0) {
                            st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000)
                            st.playTutorialVoice("tutorial_voice_026")
                            st["Reward"] = "1"
                        }
                    }
                }

                CHINCHIRIN -> htmltext = "30539-01.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val chance = Rnd[100]

        if (chance > 50)
            st.dropItemsAlways(CHRYSOLITE_ORE, 1, 0)
        else if (chance < 5)
            st.dropItemsAlways(TORN_MAP_FRAGMENT, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q293_TheHiddenVeins"

        // Items
        private val CHRYSOLITE_ORE = 1488
        private val TORN_MAP_FRAGMENT = 1489
        private val HIDDEN_VEIN_MAP = 1490

        // Reward
        private val SOULSHOT_FOR_BEGINNERS = 5789

        // NPCs
        private val FILAUR = 30535
        private val CHINCHIRIN = 30539

        // Mobs
        private val UTUKU_ORC = 20446
        private val UTUKU_ARCHER = 20447
        private val UTUKU_GRUNT = 20448
    }
}