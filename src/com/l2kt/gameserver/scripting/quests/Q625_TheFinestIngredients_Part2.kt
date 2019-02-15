package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager
import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager.StatusEnum
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q625_TheFinestIngredients_Part2 : Quest(625, "The Finest Ingredients - Part 2") {

    private var _npc: Npc? = null
    private var _status = -1

    init {

        setItemsIds(FOOD_FOR_BUMBALUMP, SPECIAL_YETI_MEAT)

        addStartNpc(JEREMY)
        addTalkId(JEREMY, YETI_TABLE)

        addAttackId(ICICLE_EMPEROR_BUMBALUMP)
        addKillId(ICICLE_EMPEROR_BUMBALUMP)

        when (RaidBossSpawnManager.getInstance().getRaidBossStatusId(ICICLE_EMPEROR_BUMBALUMP)) {
            RaidBossSpawnManager.StatusEnum.ALIVE -> {
                spawnNpc()
                startQuestTimer("check", CHECK_INTERVAL.toLong(), null, null, true)
            }
            RaidBossSpawnManager.StatusEnum.DEAD -> startQuestTimer("check", CHECK_INTERVAL.toLong(), null, null, true)
        }
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        // global quest timer has player==null -> cannot get QuestState
        if (event == "check") {
            val raid = RaidBossSpawnManager.getInstance().bosses[ICICLE_EMPEROR_BUMBALUMP]
            if (raid != null && raid.raidStatus == StatusEnum.ALIVE) {
                if (_status >= 0 && _status-- == 0)
                    despawnRaid(raid)

                spawnNpc()
            }

            return null
        }

        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // Jeremy
        if (event.equals("31521-03.htm", ignoreCase = true)) {
            if (st.hasQuestItems(SOY_SAUCE_JAR)) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.takeItems(SOY_SAUCE_JAR, 1)
                st.giveItems(FOOD_FOR_BUMBALUMP, 1)
            } else
                htmltext = "31521-04.htm"
        } else if (event.equals("31521-08.htm", ignoreCase = true)) {
            if (st.hasQuestItems(SPECIAL_YETI_MEAT)) {
                st.takeItems(SPECIAL_YETI_MEAT, 1)
                st.rewardItems(REWARD_DYE[Rnd[REWARD_DYE.size]], 5)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else
                htmltext = "31521-09.htm"
        } else if (event.equals("31542-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(FOOD_FOR_BUMBALUMP)) {
                if (_status < 0) {
                    if (spawnRaid()) {
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(FOOD_FOR_BUMBALUMP, 1)
                    }
                } else
                    htmltext = "31542-04.htm"
            } else
                htmltext = "31542-03.htm"
        }// Yeti's Table

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 73) "31521-02.htm" else "31521-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    JEREMY -> if (cond == 1)
                        htmltext = "31521-05.htm"
                    else if (cond == 2)
                        htmltext = "31521-06.htm"
                    else
                        htmltext = "31521-07.htm"

                    YETI_TABLE -> if (cond == 1)
                        htmltext = "31542-01.htm"
                    else if (cond == 2)
                        htmltext = "31542-05.htm"
                }
            }
        }

        return htmltext
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer
        if (player != null)
            _status = IDLE_INTERVAL

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer
        if (player != null) {
            for (st in getPartyMembers(player, npc, "cond", "2")) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(SPECIAL_YETI_MEAT, 1)
            }
        }

        npc.broadcastNpcSay("Oooh!")

        // despawn raid (reset info)
        despawnRaid(npc)

        // despawn npc
        if (_npc != null) {
            _npc!!.deleteMe()
            _npc = null
        }

        return null
    }

    private fun spawnNpc() {
        // spawn npc, if not spawned
        if (_npc == null)
            _npc = addSpawn(YETI_TABLE, 157136, -121456, -2363, 40000, false, 0, false)
    }

    private fun spawnRaid(): Boolean {
        val raid = RaidBossSpawnManager.getInstance().bosses[ICICLE_EMPEROR_BUMBALUMP]
        if (raid != null && raid.raidStatus == StatusEnum.ALIVE) {
            // set temporarily spawn location (to provide correct behavior of checkAndReturnToSpawn())
            raid.spawn.setLoc(157117, -121939, -2397, Rnd[65536])

            // teleport raid from secret place
            raid.teleToLocation(157117, -121939, -2397, 100)
            raid.broadcastNpcSay("I smell something delicious...")

            // set raid status
            _status = IDLE_INTERVAL

            return true
        }

        return false
    }

    private fun despawnRaid(raid: Npc) {
        // reset spawn location
        raid.spawn.setLoc(-104700, -252700, -15542, 0)

        // teleport raid back to secret place
        if (!raid.isDead)
            raid.teleToLocation(-104700, -252700, -15542, 0)

        // reset raid status
        _status = -1
    }

    companion object {
        private val qn = "Q625_TheFinestIngredients_Part2"

        // Monster
        private val ICICLE_EMPEROR_BUMBALUMP = 25296

        // NPCs
        private val JEREMY = 31521
        private val YETI_TABLE = 31542

        // Items
        private val SOY_SAUCE_JAR = 7205
        private val FOOD_FOR_BUMBALUMP = 7209
        private val SPECIAL_YETI_MEAT = 7210
        private val REWARD_DYE = intArrayOf(4589, 4590, 4591, 4592, 4593, 4594)

        // Other
        private val CHECK_INTERVAL = 600000 // 10 minutes
        private val IDLE_INTERVAL = 3 // (X * CHECK_INTERVAL) = 30 minutes
    }
}