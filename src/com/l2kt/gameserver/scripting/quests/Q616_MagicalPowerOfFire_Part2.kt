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

class Q616_MagicalPowerOfFire_Part2 : Quest(616, "Magical Power of Fire - Part 2") {

    private var _npc: Npc? = null
    private var _status = -1

    init {

        setItemsIds(FIRE_HEART_OF_NASTRON)

        addStartNpc(UDAN_MARDUI)
        addTalkId(UDAN_MARDUI, KETRAS_HOLY_ALTAR)

        addAttackId(SOUL_OF_FIRE_NASTRON)
        addKillId(SOUL_OF_FIRE_NASTRON)

        when (RaidBossSpawnManager.getRaidBossStatusId(SOUL_OF_FIRE_NASTRON)) {
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
            val raid = RaidBossSpawnManager.bosses[SOUL_OF_FIRE_NASTRON]
            if (raid != null && raid.raidStatus == StatusEnum.ALIVE) {
                if (_status >= 0 && _status-- == 0)
                    despawnRaid(raid)

                spawnNpc()
            }

            return null
        }

        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // Udan Mardui
        if (event.equals("31379-04.htm", ignoreCase = true)) {
            if (st.hasQuestItems(RED_TOTEM)) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
            } else
                htmltext = "31379-02.htm"
        } else if (event.equals("31379-08.htm", ignoreCase = true)) {
            if (st.hasQuestItems(FIRE_HEART_OF_NASTRON)) {
                st.takeItems(FIRE_HEART_OF_NASTRON, 1)
                st.rewardExpAndSp(10000, 0)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else
                htmltext = "31379-09.htm"
        } else if (event.equals("31558-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(RED_TOTEM)) {
                if (_status < 0) {
                    if (spawnRaid()) {
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(RED_TOTEM, 1)
                    }
                } else
                    htmltext = "31558-04.htm"
            } else
                htmltext = "31558-03.htm"
        }// Ketra's Holy Altar

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (!st.hasQuestItems(RED_TOTEM))
                htmltext = "31379-02.htm"
            else if (player.level < 75 && player.allianceWithVarkaKetra > -2)
                htmltext = "31379-03.htm"
            else
                htmltext = "31379-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    UDAN_MARDUI -> if (cond == 1)
                        htmltext = "31379-05.htm"
                    else if (cond == 2)
                        htmltext = "31379-06.htm"
                    else
                        htmltext = "31379-07.htm"

                    KETRAS_HOLY_ALTAR -> if (cond == 1)
                        htmltext = "31558-01.htm"
                    else if (cond == 2)
                        htmltext = "31558-05.htm"
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
                st.giveItems(FIRE_HEART_OF_NASTRON, 1)
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
            _npc = addSpawn(KETRAS_HOLY_ALTAR, 142368, -82512, -6487, 58000, false, 0, false)
    }

    private fun spawnRaid(): Boolean {
        val raid = RaidBossSpawnManager.bosses[SOUL_OF_FIRE_NASTRON]
        if (raid != null && raid.raidStatus == StatusEnum.ALIVE) {
            // set temporarily spawn location (to provide correct behavior of checkAndReturnToSpawn())
            raid.spawn?.setLoc(142624, -82285, -6491, Rnd[65536])

            // teleport raid from secret place
            raid.teleToLocation(142624, -82285, -6491, 100)

            // set raid status
            _status = IDLE_INTERVAL

            return true
        }

        return false
    }

    private fun despawnRaid(raid: Npc) {
        // reset spawn location
        raid.spawn?.setLoc(-105300, -252700, -15542, 0)

        // teleport raid back to secret place
        if (!raid.isDead)
            raid.teleToLocation(-105300, -252700, -15542, 0)

        // reset raid status
        _status = -1
    }

    companion object {
        private const val qn = "Q616_MagicalPowerOfFire_Part2"

        // Monster
        private const val SOUL_OF_FIRE_NASTRON = 25306

        // NPCs
        private const val UDAN_MARDUI = 31379
        private const val KETRAS_HOLY_ALTAR = 31558

        // Items
        private const val RED_TOTEM = 7243
        private const val FIRE_HEART_OF_NASTRON = 7244

        // Other
        private const val CHECK_INTERVAL = 600000 // 10 minutes
        private const val IDLE_INTERVAL = 2 // (X * CHECK_INTERVAL) = 20 minutes
    }
}