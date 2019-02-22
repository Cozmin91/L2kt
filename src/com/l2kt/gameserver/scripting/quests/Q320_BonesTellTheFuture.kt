package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q320_BonesTellTheFuture : Quest(320, "Bones Tell the Future") {

    // Quest item
    private val BONE_FRAGMENT = 809

    init {

        setItemsIds(BONE_FRAGMENT)

        addStartNpc(30359) // Kaitar
        addTalkId(30359)

        addKillId(20517, 20518)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30359-04.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> if (player.race != ClassRace.DARK_ELF)
                htmltext = "30359-00.htm"
            else if (player.level < 10)
                htmltext = "30359-02.htm"
            else
                htmltext = "30359-03.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30359-05.htm"
            else {
                htmltext = "30359-06.htm"
                st.takeItems(BONE_FRAGMENT, -1)
                st.rewardItems(57, 8470)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(BONE_FRAGMENT, 1, 10, if (npc.npcId == 20517) 180000 else 200000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q320_BonesTellTheFuture"
    }
}