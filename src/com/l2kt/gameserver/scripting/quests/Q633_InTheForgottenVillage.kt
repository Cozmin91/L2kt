package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q633_InTheForgottenVillage : Quest(633, "In the Forgotten Village") {
    init {
        MOBS[21557] = 328000 // Bone Snatcher
        MOBS[21558] = 328000 // Bone Snatcher
        MOBS[21559] = 337000 // Bone Maker
        MOBS[21560] = 337000 // Bone Shaper
        MOBS[21563] = 342000 // Bone Collector
        MOBS[21564] = 348000 // Skull Collector
        MOBS[21565] = 351000 // Bone Animator
        MOBS[21566] = 359000 // Skull Animator
        MOBS[21567] = 359000 // Bone Slayer
        MOBS[21572] = 365000 // Bone Sweeper
        MOBS[21574] = 383000 // Bone Grinder
        MOBS[21575] = 383000 // Bone Grinder
        MOBS[21580] = 385000 // Bone Caster
        MOBS[21581] = 395000 // Bone Puppeteer
        MOBS[21583] = 397000 // Bone Scavenger
        MOBS[21584] = 401000 // Bone Scavenger
    }

    init {
        UNDEADS[21553] = 347000 // Trampled Man
        UNDEADS[21554] = 347000 // Trampled Man
        UNDEADS[21561] = 450000 // Sacrificed Man
        UNDEADS[21578] = 501000 // Behemoth Zombie
        UNDEADS[21596] = 359000 // Requiem Lord
        UNDEADS[21597] = 370000 // Requiem Behemoth
        UNDEADS[21598] = 441000 // Requiem Behemoth
        UNDEADS[21599] = 395000 // Requiem Priest
        UNDEADS[21600] = 408000 // Requiem Behemoth
        UNDEADS[21601] = 411000 // Requiem Behemoth
    }

    init {

        setItemsIds(RIB_BONE, ZOMBIE_LIVER)

        addStartNpc(MINA)
        addTalkId(MINA)

        for (i in MOBS.keys)
            addKillId(i)

        for (i in UNDEADS.keys)
            addKillId(i)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31388-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31388-10.htm", ignoreCase = true)) {
            st.takeItems(RIB_BONE, -1)
            st.playSound(QuestState.SOUND_GIVEUP)
            st.exitQuest(true)
        } else if (event.equals("31388-09.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(RIB_BONE) >= 200) {
                htmltext = "31388-08.htm"
                st.takeItems(RIB_BONE, 200)
                st.rewardItems(57, 25000)
                st.rewardExpAndSp(305235, 0)
                st.playSound(QuestState.SOUND_FINISH)
            }
            st["cond"] = "1"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 65) "31388-03.htm" else "31388-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "31388-06.htm"
                else if (cond == 2)
                    htmltext = "31388-05.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer
        val npcId = npc.npcId

        if (UNDEADS.containsKey(npcId)) {
            val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

            st.dropItems(ZOMBIE_LIVER, 1, 0, UNDEADS[npcId] ?: return null)
        } else if (MOBS.containsKey(npcId)) {
            val st = getRandomPartyMember(player!!, npc, "1") ?: return null

            if (st.dropItems(RIB_BONE, 1, 200, MOBS[npcId] ?: return null))
                st["cond"] = "2"
        }

        return null
    }

    companion object {
        private val qn = "Q633_InTheForgottenVillage"

        // NPCS
        private val MINA = 31388

        // ITEMS
        private val RIB_BONE = 7544
        private val ZOMBIE_LIVER = 7545

        // MOBS / DROP chances
        private val MOBS = HashMap<Int, Int>()

        private val UNDEADS = HashMap<Int, Int>()
    }
}