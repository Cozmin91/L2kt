package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q102_SeaOfSporesFever : Quest(102, "Sea of Spores Fever") {
    init {

        setItemsIds(
            ALBERIUS_LETTER,
            EVERGREEN_AMULET,
            DRYAD_TEARS,
            COBENDELL_MEDICINE_1,
            COBENDELL_MEDICINE_2,
            COBENDELL_MEDICINE_3,
            COBENDELL_MEDICINE_4,
            COBENDELL_MEDICINE_5,
            ALBERIUS_LIST
        )

        addStartNpc(ALBERIUS)
        addTalkId(ALBERIUS, COBENDELL, BERROS, RAYEN, GARTRANDELL, VELTRESS)

        addKillId(20013, 20019)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30284-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ALBERIUS_LETTER, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF)
                htmltext = "30284-00.htm"
            else if (player.level < 12)
                htmltext = "30284-08.htm"
            else
                htmltext = "30284-07.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ALBERIUS -> if (cond == 1)
                        htmltext = "30284-03.htm"
                    else if (cond == 2 || cond == 3)
                        htmltext = "30284-09.htm"
                    else if (cond == 4) {
                        htmltext = "30284-04.htm"
                        st["cond"] = "5"
                        st["medicines"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(COBENDELL_MEDICINE_1, 1)
                        st.giveItems(ALBERIUS_LIST, 1)
                    } else if (cond == 5)
                        htmltext = "30284-05.htm"
                    else if (cond == 6) {
                        htmltext = "30284-06.htm"
                        st.takeItems(ALBERIUS_LIST, 1)

                        if (player.isMageClass) {
                            st.giveItems(STAFF_OF_SENTINEL, 1)
                            st.rewardItems(SPIRITSHOT_NO_GRADE, 500)
                        } else {
                            st.giveItems(SWORD_OF_SENTINEL, 1)
                            st.rewardItems(SOULSHOT_NO_GRADE, 1000)
                        }

                        st.giveItems(LESSER_HEALING_POT, 100)
                        st.giveItems(ECHO_BATTLE, 10)
                        st.giveItems(ECHO_LOVE, 10)
                        st.giveItems(ECHO_SOLITUDE, 10)
                        st.giveItems(ECHO_FEAST, 10)
                        st.giveItems(ECHO_CELEBRATION, 10)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    COBENDELL -> if (cond == 1) {
                        htmltext = "30156-03.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ALBERIUS_LETTER, 1)
                        st.giveItems(EVERGREEN_AMULET, 1)
                    } else if (cond == 2)
                        htmltext = "30156-04.htm"
                    else if (cond == 5)
                        htmltext = "30156-07.htm"
                    else if (cond == 3) {
                        htmltext = "30156-05.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(DRYAD_TEARS, -1)
                        st.takeItems(EVERGREEN_AMULET, 1)
                        st.giveItems(COBENDELL_MEDICINE_1, 1)
                        st.giveItems(COBENDELL_MEDICINE_2, 1)
                        st.giveItems(COBENDELL_MEDICINE_3, 1)
                        st.giveItems(COBENDELL_MEDICINE_4, 1)
                        st.giveItems(COBENDELL_MEDICINE_5, 1)
                    } else if (cond == 4)
                        htmltext = "30156-06.htm"

                    BERROS -> if (cond == 5) {
                        htmltext = "30217-01.htm"
                        checkItem(st, COBENDELL_MEDICINE_2)
                    }

                    VELTRESS -> if (cond == 5) {
                        htmltext = "30219-01.htm"
                        checkItem(st, COBENDELL_MEDICINE_3)
                    }

                    RAYEN -> if (cond == 5) {
                        htmltext = "30221-01.htm"
                        checkItem(st, COBENDELL_MEDICINE_4)
                    }

                    GARTRANDELL -> if (cond == 5) {
                        htmltext = "30285-01.htm"
                        checkItem(st, COBENDELL_MEDICINE_5)
                    }
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "2") ?: return null

        if (st.dropItems(DRYAD_TEARS, 1, 10, 300000))
            st["cond"] = "3"

        return null
    }

    companion object {
        private const val qn = "Q102_SeaOfSporesFever"

        // Items
        private const val ALBERIUS_LETTER = 964
        private const val EVERGREEN_AMULET = 965
        private const val DRYAD_TEARS = 966
        private const val ALBERIUS_LIST = 746
        private const val COBENDELL_MEDICINE_1 = 1130
        private const val COBENDELL_MEDICINE_2 = 1131
        private const val COBENDELL_MEDICINE_3 = 1132
        private const val COBENDELL_MEDICINE_4 = 1133
        private const val COBENDELL_MEDICINE_5 = 1134

        // Rewards
        private const val SPIRITSHOT_NO_GRADE = 2509
        private const val SOULSHOT_NO_GRADE = 1835
        private const val SWORD_OF_SENTINEL = 743
        private const val STAFF_OF_SENTINEL = 744
        private const val LESSER_HEALING_POT = 1060
        private const val ECHO_BATTLE = 4412
        private const val ECHO_LOVE = 4413
        private const val ECHO_SOLITUDE = 4414
        private const val ECHO_FEAST = 4415
        private const val ECHO_CELEBRATION = 4416

        // NPCs
        private const val ALBERIUS = 30284
        private const val COBENDELL = 30156
        private const val BERROS = 30217
        private const val VELTRESS = 30219
        private const val RAYEN = 30221
        private const val GARTRANDELL = 30285

        private fun checkItem(st: QuestState, itemId: Int) {
            if (st.hasQuestItems(itemId)) {
                st.takeItems(itemId, 1)

                val medicinesLeft = st.getInt("medicines") - 1
                if (medicinesLeft == 0) {
                    st["cond"] = "6"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st["medicines"] = medicinesLeft.toString()
            }
        }
    }
}