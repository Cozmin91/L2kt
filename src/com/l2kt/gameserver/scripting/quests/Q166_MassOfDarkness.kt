package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q166_MassOfDarkness : Quest(166, "Mass of Darkness") {
    init {

        setItemsIds(UNDRIAS_LETTER, CEREMONIAL_DAGGER, DREVIANT_WINE, GARMIEL_SCRIPTURE)

        addStartNpc(UNDRIAS)
        addTalkId(UNDRIAS, IRIA, DORANKUS, TRUDY)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30130-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(UNDRIAS_LETTER, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.DARK_ELF)
                htmltext = "30130-00.htm"
            else if (player.level < 2)
                htmltext = "30130-02.htm"
            else
                htmltext = "30130-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    UNDRIAS -> if (cond == 1)
                        htmltext = "30130-05.htm"
                    else if (cond == 2) {
                        htmltext = "30130-06.htm"
                        st.takeItems(CEREMONIAL_DAGGER, 1)
                        st.takeItems(DREVIANT_WINE, 1)
                        st.takeItems(GARMIEL_SCRIPTURE, 1)
                        st.takeItems(UNDRIAS_LETTER, 1)
                        st.rewardItems(57, 500)
                        st.rewardExpAndSp(500, 0)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    IRIA -> if (cond == 1 && !st.hasQuestItems(CEREMONIAL_DAGGER)) {
                        htmltext = "30135-01.htm"
                        st.giveItems(CEREMONIAL_DAGGER, 1)

                        if (st.hasQuestItems(DREVIANT_WINE, GARMIEL_SCRIPTURE)) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (cond == 2)
                        htmltext = "30135-02.htm"

                    DORANKUS -> if (cond == 1 && !st.hasQuestItems(DREVIANT_WINE)) {
                        htmltext = "30139-01.htm"
                        st.giveItems(DREVIANT_WINE, 1)

                        if (st.hasQuestItems(CEREMONIAL_DAGGER, GARMIEL_SCRIPTURE)) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (cond == 2)
                        htmltext = "30139-02.htm"

                    TRUDY -> if (cond == 1 && !st.hasQuestItems(GARMIEL_SCRIPTURE)) {
                        htmltext = "30143-01.htm"
                        st.giveItems(GARMIEL_SCRIPTURE, 1)

                        if (st.hasQuestItems(CEREMONIAL_DAGGER, DREVIANT_WINE)) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (cond == 2)
                        htmltext = "30143-02.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private val qn = "Q166_MassOfDarkness"

        // NPCs
        private val UNDRIAS = 30130
        private val IRIA = 30135
        private val DORANKUS = 30139
        private val TRUDY = 30143

        // Items
        private val UNDRIAS_LETTER = 1088
        private val CEREMONIAL_DAGGER = 1089
        private val DREVIANT_WINE = 1090
        private val GARMIEL_SCRIPTURE = 1091
    }
}