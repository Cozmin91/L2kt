package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q162_CurseOfTheUndergroundFortress : Quest(162, "Curse of the Underground Fortress") {
    init {
        CHANCES[SHADE_HORROR] = 250000
        CHANCES[DARK_TERROR] = 260000
        CHANCES[MIST_TERROR] = 230000
        CHANCES[DUNGEON_SKELETON_ARCHER] = 250000
        CHANCES[DUNGEON_SKELETON] = 230000
        CHANCES[DREAD_SOLDIER] = 260000
    }

    init {

        setItemsIds(BONE_FRAGMENT, ELF_SKULL)

        addStartNpc(30147) // Unoren
        addTalkId(30147)

        addKillId(SHADE_HORROR, DARK_TERROR, MIST_TERROR, DUNGEON_SKELETON_ARCHER, DUNGEON_SKELETON, DREAD_SOLDIER)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30147-04.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> if (player.race == ClassRace.DARK_ELF)
                htmltext = "30147-00.htm"
            else if (player.level < 12)
                htmltext = "30147-01.htm"
            else
                htmltext = "30147-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "30147-05.htm"
                else if (cond == 2) {
                    htmltext = "30147-06.htm"
                    st.takeItems(ELF_SKULL, -1)
                    st.takeItems(BONE_FRAGMENT, -1)
                    st.giveItems(BONE_SHIELD, 1)
                    st.rewardItems(57, 24000)
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

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        val npcId = npc.npcId

        when (npcId) {
            DUNGEON_SKELETON, DUNGEON_SKELETON_ARCHER, DREAD_SOLDIER -> if (st.dropItems(
                    BONE_FRAGMENT,
                    1,
                    10,
                    CHANCES[npcId] ?: 0
                ) && st.getQuestItemsCount(ELF_SKULL) >= 3
            )
                st["cond"] = "2"

            SHADE_HORROR, DARK_TERROR, MIST_TERROR -> if (st.dropItems(
                    ELF_SKULL,
                    1,
                    3,
                    CHANCES[npcId] ?: 0
                ) && st.getQuestItemsCount(BONE_FRAGMENT) >= 10
            )
                st["cond"] = "2"
        }

        return null
    }

    companion object {
        private val qn = "Q162_CurseOfTheUndergroundFortress"

        // Monsters
        private val SHADE_HORROR = 20033
        private val DARK_TERROR = 20345
        private val MIST_TERROR = 20371
        private val DUNGEON_SKELETON_ARCHER = 20463
        private val DUNGEON_SKELETON = 20464
        private val DREAD_SOLDIER = 20504

        // Items
        private val BONE_FRAGMENT = 1158
        private val ELF_SKULL = 1159

        // Rewards
        private val BONE_SHIELD = 625

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}