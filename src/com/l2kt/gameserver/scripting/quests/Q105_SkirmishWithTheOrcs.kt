package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q105_SkirmishWithTheOrcs : Quest(105, "Skirmish with the Orcs") {
    init {

        setItemsIds(
            KENDELL_ORDER_1,
            KENDELL_ORDER_2,
            KENDELL_ORDER_3,
            KENDELL_ORDER_4,
            KENDELL_ORDER_5,
            KENDELL_ORDER_6,
            KENDELL_ORDER_7,
            KENDELL_ORDER_8,
            KABOO_CHIEF_TORC_1,
            KABOO_CHIEF_TORC_2
        )

        addStartNpc(30218) // Kendell
        addTalkId(30218)

        addKillId(
            KABOO_CHIEF_UOPH,
            KABOO_CHIEF_KRACHA,
            KABOO_CHIEF_BATOH,
            KABOO_CHIEF_TANUKIA,
            KABOO_CHIEF_TUREL,
            KABOO_CHIEF_ROKO,
            KABOO_CHIEF_KAMUT,
            KABOO_CHIEF_MURTIKA
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30218-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(Rnd[1836, 1839], 1) // Kendell's orders 1 to 4.
        }
        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF)
                htmltext = "30218-00.htm"
            else if (player.level < 10)
                htmltext = "30218-01.htm"
            else
                htmltext = "30218-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "30218-05.htm"
                else if (cond == 2) {
                    htmltext = "30218-06.htm"
                    st["cond"] = "3"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(KABOO_CHIEF_TORC_1, 1)
                    st.takeItems(KENDELL_ORDER_1, 1)
                    st.takeItems(KENDELL_ORDER_2, 1)
                    st.takeItems(KENDELL_ORDER_3, 1)
                    st.takeItems(KENDELL_ORDER_4, 1)
                    st.giveItems(Rnd[1840, 1843], 1) // Kendell's orders 5 to 8.
                } else if (cond == 3)
                    htmltext = "30218-07.htm"
                else if (cond == 4) {
                    htmltext = "30218-08.htm"
                    st.takeItems(KABOO_CHIEF_TORC_2, 1)
                    st.takeItems(KENDELL_ORDER_5, 1)
                    st.takeItems(KENDELL_ORDER_6, 1)
                    st.takeItems(KENDELL_ORDER_7, 1)
                    st.takeItems(KENDELL_ORDER_8, 1)

                    if (player.isMageClass)
                        st.giveItems(RED_SUNSET_STAFF, 1)
                    else
                        st.giveItems(RED_SUNSET_SWORD, 1)

                    if (player.isNewbie) {
                        st.showQuestionMark(26)
                        if (player.isMageClass) {
                            st.playTutorialVoice("tutorial_voice_027")
                            st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000)
                        } else {
                            st.playTutorialVoice("tutorial_voice_026")
                            st.giveItems(SOULSHOT_FOR_BEGINNERS, 7000)
                        }
                    }

                    st.giveItems(ECHO_BATTLE, 10)
                    st.giveItems(ECHO_LOVE, 10)
                    st.giveItems(ECHO_SOLITUDE, 10)
                    st.giveItems(ECHO_FEAST, 10)
                    st.giveItems(ECHO_CELEBRATION, 10)
                    player.broadcastPacket(SocialAction(player, 3))
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(false)
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            KABOO_CHIEF_UOPH, KABOO_CHIEF_KRACHA, KABOO_CHIEF_BATOH, KABOO_CHIEF_TANUKIA -> if (st.getInt("cond") == 1 && st.hasQuestItems(
                    npc.npcId - 25223
                )
            )
            // npcId - 25223 = itemId to verify.
            {
                st["cond"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(KABOO_CHIEF_TORC_1, 1)
            }

            KABOO_CHIEF_TUREL, KABOO_CHIEF_ROKO -> if (st.getInt("cond") == 3 && st.hasQuestItems(npc.npcId - 25224))
            // npcId - 25224 = itemId to verify.
            {
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(KABOO_CHIEF_TORC_2, 1)
            }

            KABOO_CHIEF_KAMUT, KABOO_CHIEF_MURTIKA -> if (st.getInt("cond") == 3 && st.hasQuestItems(npc.npcId - 25225))
            // npcId - 25225 = itemId to verify.
            {
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(KABOO_CHIEF_TORC_2, 1)
            }
        }

        return null
    }

    companion object {
        private const val qn = "Q105_SkirmishWithTheOrcs"

        // Item
        private const val KENDELL_ORDER_1 = 1836
        private const val KENDELL_ORDER_2 = 1837
        private const val KENDELL_ORDER_3 = 1838
        private const val KENDELL_ORDER_4 = 1839
        private const val KENDELL_ORDER_5 = 1840
        private const val KENDELL_ORDER_6 = 1841
        private const val KENDELL_ORDER_7 = 1842
        private const val KENDELL_ORDER_8 = 1843
        private const val KABOO_CHIEF_TORC_1 = 1844
        private const val KABOO_CHIEF_TORC_2 = 1845

        // Monster
        private const val KABOO_CHIEF_UOPH = 27059
        private const val KABOO_CHIEF_KRACHA = 27060
        private const val KABOO_CHIEF_BATOH = 27061
        private const val KABOO_CHIEF_TANUKIA = 27062
        private const val KABOO_CHIEF_TUREL = 27064
        private const val KABOO_CHIEF_ROKO = 27065
        private const val KABOO_CHIEF_KAMUT = 27067
        private const val KABOO_CHIEF_MURTIKA = 27068

        // Rewards
        private const val SPIRITSHOT_FOR_BEGINNERS = 5790
        private const val SOULSHOT_FOR_BEGINNERS = 5789
        private const val RED_SUNSET_STAFF = 754
        private const val RED_SUNSET_SWORD = 981
        private const val ECHO_BATTLE = 4412
        private const val ECHO_LOVE = 4413
        private const val ECHO_SOLITUDE = 4414
        private const val ECHO_FEAST = 4415
        private const val ECHO_CELEBRATION = 4416
    }
}