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

class Q604_DaimonTheWhiteEyed_Part2 : Quest(604, "Daimon The White-Eyed - Part 2") {

    private var _npc: Npc? = null
    private var _status = -1

    init {

        setItemsIds(SUMMON_CRYSTAL, ESSENCE_OF_DAIMON)

        addStartNpc(EYE_OF_ARGOS)
        addTalkId(EYE_OF_ARGOS, DAIMON_ALTAR)

        addAttackId(DAIMON_THE_WHITE_EYED)
        addKillId(DAIMON_THE_WHITE_EYED)

        when (RaidBossSpawnManager.getInstance().getRaidBossStatusId(DAIMON_THE_WHITE_EYED)) {
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
            val raid = RaidBossSpawnManager.getInstance().bosses[DAIMON_THE_WHITE_EYED]
            if (raid != null && raid.raidStatus == StatusEnum.ALIVE) {
                if (_status >= 0 && _status-- == 0)
                    despawnRaid(raid)

                spawnNpc()
            }

            return null
        }

        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // Eye of Argos
        if (event.equals("31683-03.htm", ignoreCase = true)) {
            if (st.hasQuestItems(UNFINISHED_SUMMON_CRYSTAL)) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.takeItems(UNFINISHED_SUMMON_CRYSTAL, 1)
                st.giveItems(SUMMON_CRYSTAL, 1)
            } else
                htmltext = "31683-04.htm"
        } else if (event.equals("31683-08.htm", ignoreCase = true)) {
            if (st.hasQuestItems(ESSENCE_OF_DAIMON)) {
                st.takeItems(ESSENCE_OF_DAIMON, 1)
                st.rewardItems(REWARD_DYE[Rnd[REWARD_DYE.size]], 5)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else
                htmltext = "31683-09.htm"
        } else if (event.equals("31541-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(SUMMON_CRYSTAL)) {
                if (_status < 0) {
                    if (spawnRaid()) {
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SUMMON_CRYSTAL, 1)
                    }
                } else
                    htmltext = "31541-04.htm"
            } else
                htmltext = "31541-03.htm"
        }// Diamon's Altar

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 73) {
                htmltext = "31683-02.htm"
                st.exitQuest(true)
            } else
                htmltext = "31683-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    EYE_OF_ARGOS -> if (cond == 1)
                        htmltext = "31683-05.htm"
                    else if (cond == 2)
                        htmltext = "31683-06.htm"
                    else
                        htmltext = "31683-07.htm"

                    DAIMON_ALTAR -> if (cond == 1)
                        htmltext = "31541-01.htm"
                    else if (cond == 2)
                        htmltext = "31541-05.htm"
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
                st.giveItems(ESSENCE_OF_DAIMON, 1)
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
            _npc = addSpawn(DAIMON_ALTAR, 186304, -43744, -3193, 57000, false, 0, false)
    }

    private fun spawnRaid(): Boolean {
        val raid = RaidBossSpawnManager.getInstance().bosses[DAIMON_THE_WHITE_EYED]
        if (raid != null && raid.raidStatus == StatusEnum.ALIVE) {
            // set temporarily spawn location (to provide correct behavior of checkAndReturnToSpawn())
            raid.spawn.setLoc(185900, -44000, -3160, Rnd[65536])

            // teleport raid from secret place
            raid.teleToLocation(185900, -44000, -3160, 100)
            raid.broadcastNpcSay("Who called me?")

            // set raid status
            _status = IDLE_INTERVAL

            return true
        }

        return false
    }

    private fun despawnRaid(raid: Npc) {
        // reset spawn location
        raid.spawn.setLoc(-106500, -252700, -15542, 0)

        // teleport raid back to secret place
        if (!raid.isDead)
            raid.teleToLocation(-106500, -252700, -15542, 0)

        // reset raid status
        _status = -1
    }

    companion object {
        private val qn = "Q604_DaimonTheWhiteEyed_Part2"

        // Monster
        private val DAIMON_THE_WHITE_EYED = 25290

        // NPCs
        private val EYE_OF_ARGOS = 31683
        private val DAIMON_ALTAR = 31541

        // Items
        private val UNFINISHED_SUMMON_CRYSTAL = 7192
        private val SUMMON_CRYSTAL = 7193
        private val ESSENCE_OF_DAIMON = 7194
        private val REWARD_DYE = intArrayOf(4595, 4596, 4597, 4598, 4599, 4600)

        // Other
        private val CHECK_INTERVAL = 600000 // 10 minutes
        private val IDLE_INTERVAL = 3 // (X * CHECK_INTERVAL) = 30 minutes
    }
}