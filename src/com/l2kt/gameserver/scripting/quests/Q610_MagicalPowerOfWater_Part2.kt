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

class Q610_MagicalPowerOfWater_Part2 : Quest(610, "Magical Power of Water - Part 2") {

    private var _npc: Npc? = null
    private var _status = -1

    init {

        setItemsIds(ICE_HEART_OF_ASHUTAR)

        addStartNpc(ASEFA)
        addTalkId(ASEFA, VARKAS_HOLY_ALTAR)

        addAttackId(SOUL_OF_WATER_ASHUTAR)
        addKillId(SOUL_OF_WATER_ASHUTAR)

        when (RaidBossSpawnManager.getRaidBossStatusId(SOUL_OF_WATER_ASHUTAR)) {
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
            val raid = RaidBossSpawnManager.bosses[SOUL_OF_WATER_ASHUTAR]
            if (raid != null && raid.raidStatus == StatusEnum.ALIVE) {
                if (_status >= 0 && _status-- == 0)
                    despawnRaid(raid)

                spawnNpc()
            }

            return null
        }

        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // Asefa
        if (event.equals("31372-04.htm", ignoreCase = true)) {
            if (st.hasQuestItems(GREEN_TOTEM)) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
            } else
                htmltext = "31372-02.htm"
        } else if (event.equals("31372-07.htm", ignoreCase = true)) {
            if (st.hasQuestItems(ICE_HEART_OF_ASHUTAR)) {
                st.takeItems(ICE_HEART_OF_ASHUTAR, 1)
                st.rewardExpAndSp(10000, 0)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else
                htmltext = "31372-08.htm"
        } else if (event.equals("31560-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(GREEN_TOTEM)) {
                if (_status < 0) {
                    if (spawnRaid()) {
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(GREEN_TOTEM, 1)
                    }
                } else
                    htmltext = "31560-04.htm"
            } else
                htmltext = "31560-03.htm"
        }// Varka's Holy Altar

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (!st.hasQuestItems(GREEN_TOTEM))
                htmltext = "31372-02.htm"
            else if (player.level < 75 && player.allianceWithVarkaKetra < 2)
                htmltext = "31372-03.htm"
            else
                htmltext = "31372-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ASEFA -> htmltext = if (cond < 3) "31372-05.htm" else "31372-06.htm"

                    VARKAS_HOLY_ALTAR -> if (cond == 1)
                        htmltext = "31560-01.htm"
                    else if (cond == 2)
                        htmltext = "31560-05.htm"
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

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer
        if (player != null) {
            for (st in getPartyMembers(player, npc, "cond", "2")) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(ICE_HEART_OF_ASHUTAR, 1)
            }
        }

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
            _npc = addSpawn(VARKAS_HOLY_ALTAR, 105452, -36775, -1050, 34000, false, 0, false)
    }

    private fun spawnRaid(): Boolean {
        val raid = RaidBossSpawnManager.bosses[SOUL_OF_WATER_ASHUTAR]
        if (raid != null && raid.raidStatus == StatusEnum.ALIVE) {
            // set temporarily spawn location (to provide correct behavior of checkAndReturnToSpawn())
            raid.spawn.setLoc(104771, -36993, -1149, Rnd[65536])

            // teleport raid from secret place
            raid.teleToLocation(104771, -36993, -1149, 100)
            raid.broadcastNpcSay("The water charm then is the storm and the tsunami strength! Opposes with it only has the blind alley!")

            // set raid status
            _status = IDLE_INTERVAL

            return true
        }

        return false
    }

    private fun despawnRaid(raid: Npc) {
        // reset spawn location
        raid.spawn.setLoc(-105900, -252700, -15542, 0)

        // teleport raid back to secret place
        if (!raid.isDead)
            raid.teleToLocation(-105900, -252700, -15542, 0)

        // reset raid status
        _status = -1
    }

    companion object {
        private val qn = "Q610_MagicalPowerOfWater_Part2"

        // Monster
        private val SOUL_OF_WATER_ASHUTAR = 25316

        // NPCs
        private val ASEFA = 31372
        private val VARKAS_HOLY_ALTAR = 31560

        // Items
        private val GREEN_TOTEM = 7238
        private val ICE_HEART_OF_ASHUTAR = 7239

        // Other
        private val CHECK_INTERVAL = 600000 // 10 minutes
        private val IDLE_INTERVAL = 2 // (X * CHECK_INTERVAL) = 20 minutes
    }
}