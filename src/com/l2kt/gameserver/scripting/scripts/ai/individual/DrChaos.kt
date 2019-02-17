package com.l2kt.gameserver.scripting.scripts.ai.individual

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.instancemanager.GrandBossManager
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.GrandBoss
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.network.serverpackets.SpecialCamera
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

/**
 * Dr. Chaos is a boss @ Pavel's Ruins. Some things to know :
 *
 *  * As a mad scientist, he thinks all are spies, and for so if you stand too much longer near him you're considered as an "assassin from Black Anvil Guild".
 *  * You can chat with him, but if you try too much he will become angry.
 *  * That adaptation sends a decent cinematic made with the different social actions too.
 *  * The status of the RB is saved under GBs table, in order to retrieve the state if server restarts.
 *  * The spawn of the different NPCs (Dr. Chaos / War golem) is handled by that script aswell.
 *
 */
class DrChaos : L2AttackableAIScript("ai/individual") {

    private var _lastAttackTime: Long = 0
    private var _pissedOffTimer: Int = 0

    init {

        run{
            addFirstTalkId(DOCTOR_CHAOS) // Different HTMs following actual humor.
            addSpawnId(DOCTOR_CHAOS) // Timer activation at 30sec + paranoia activity.

            val info = GrandBossManager.getStatsSet(CHAOS_GOLEM) ?: return@run
            val status = GrandBossManager.getBossStatus(CHAOS_GOLEM)

            // Load the reset date and time for Dr. Chaos from DB.
            if (status == DEAD.toInt()) {
                val temp = info.getLong("respawn_time") - System.currentTimeMillis()
                if (temp > 0)
                    startQuestTimer("reset_drchaos", temp, null, null, false)
                else {
                    // The time has already expired while the server was offline. Delete the saved time and
                    // immediately spawn Dr. Chaos. Also the state need to be changed for NORMAL
                    addSpawn(DOCTOR_CHAOS, 96320, -110912, -3328, 8191, false, 0, false)
                    GrandBossManager.setBossStatus(CHAOS_GOLEM, NORMAL.toInt())
                }
            } else if (status == CRAZY.toInt()) {
                val loc_x = info.getInteger("loc_x")
                val loc_y = info.getInteger("loc_y")
                val loc_z = info.getInteger("loc_z")
                val heading = info.getInteger("heading")
                val hp = info.getInteger("currentHP")
                val mp = info.getInteger("currentMP")

                val golem = addSpawn(CHAOS_GOLEM, loc_x, loc_y, loc_z, heading, false, 0, false) as GrandBoss
                GrandBossManager.addBoss(golem)

                golem.setCurrentHpMp(hp.toDouble(), mp.toDouble())
                golem.setRunning()

                // start monitoring Dr. Chaos's inactivity
                _lastAttackTime = System.currentTimeMillis()
                startQuestTimer("golem_despawn", 60000, golem, null, true)
            } else
                addSpawn(DOCTOR_CHAOS, 96320, -110912, -3328, 8191, false, 0, false)// Spawn the regular NPC.
            // Spawn the war golem.
        }
    }

    override fun registerNpcs() {
        addKillId(CHAOS_GOLEM) // Message + despawn.
        addAttackActId(CHAOS_GOLEM) // Random messages when he attacks.
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var npc = npc
        if (event.equals("reset_drchaos", ignoreCase = true)) {
            GrandBossManager.setBossStatus(CHAOS_GOLEM, NORMAL.toInt())
            addSpawn(DOCTOR_CHAOS, 96320, -110912, -3328, 8191, false, 0, false)
        } else if (event.equals("golem_despawn", ignoreCase = true) && npc != null) {
            if (npc.npcId == CHAOS_GOLEM) {
                if (_lastAttackTime + 1800000 < System.currentTimeMillis()) {
                    // Despawn the war golem.
                    npc.deleteMe()

                    addSpawn(DOCTOR_CHAOS, 96320, -110912, -3328, 8191, false, 0, false) // spawn Dr. Chaos
                    GrandBossManager
                        .setBossStatus(CHAOS_GOLEM, NORMAL.toInt()) // mark Dr. Chaos is not crazy any more
                    cancelQuestTimer("golem_despawn", npc, null)
                }
            }
        } else if (event.equals("1", ignoreCase = true)) {
            npc!!.broadcastPacket(SocialAction(npc, 2))
            npc.broadcastPacket(SpecialCamera(npc.objectId, 1, -200, 15, 5500, 13500, 0, 0, 1, 0))
        } else if (event.equals("2", ignoreCase = true))
            npc!!.broadcastPacket(SocialAction(npc, 3))
        else if (event.equals("3", ignoreCase = true))
            npc!!.broadcastPacket(SocialAction(npc, 1))
        else if (event.equals("4", ignoreCase = true)) {
            npc!!.broadcastPacket(SpecialCamera(npc.objectId, 1, -150, 10, 3500, 5000, 0, 0, 1, 0))
            npc.ai.setIntention(CtrlIntention.MOVE_TO, Location(95928, -110671, -3340))
        } else if (event.equals("5", ignoreCase = true)) {
            // Delete Dr. Chaos && spawn the war golem.
            npc!!.deleteMe()
            val golem = addSpawn(CHAOS_GOLEM, 96080, -110822, -3343, 0, false, 0, false) as GrandBoss
            GrandBossManager.addBoss(golem)

            // The "npc" variable attribution is now for the golem.
            npc = golem
            npc.broadcastPacket(SpecialCamera(npc.objectId, 30, 200, 20, 6000, 8000, 0, 0, 1, 0))
            npc.broadcastPacket(SocialAction(npc, 1))
            npc.broadcastPacket(PlaySound(1, "Rm03_A", npc))

            // start monitoring Dr. Chaos's inactivity
            _lastAttackTime = System.currentTimeMillis()
            startQuestTimer("golem_despawn", 60000, npc, null, true)
        } else if (event.equals("paranoia_activity", ignoreCase = true)) {
            if (GrandBossManager.getBossStatus(CHAOS_GOLEM) == NORMAL.toInt()) {
                for (obj in npc!!.getKnownTypeInRadius(Player::class.java, 500)) {
                    if (obj.isDead)
                        continue

                    _pissedOffTimer -= 1

                    // Make him speak.
                    if (_pissedOffTimer == 15)
                        npc.broadcastNpcSay("How dare you trespass into my territory! Have you no fear?")
                    else if (_pissedOffTimer <= 0)
                        crazyMidgetBecomesAngry(npc)// That was "too much" for that time.

                    // Break it here, as we already found a valid player.
                    break
                }
            }
        }// Check every sec if someone is in range, if found, launch one task to decrease the timer.
        // despawn the live Dr. Chaos after 30 minutes of inactivity

        return super.onAdvEvent(event, npc, player)
    }

    override fun onFirstTalk(npc: Npc, player: Player): String? {
        var htmltext = ""

        if (GrandBossManager.getBossStatus(CHAOS_GOLEM) == NORMAL.toInt()) {
            _pissedOffTimer -= Rnd[1, 5] // remove 1-5 secs.

            if (_pissedOffTimer > 20)
                htmltext =
                        "<html><body>Doctor Chaos:<br>What?! Who are you? How did you come here?<br>You really look suspicious... Aren't those filthy members of Black Anvil guild send you? No? Mhhhhh... I don't trust you!</body></html>"
            else if (_pissedOffTimer in 11..20)
                htmltext =
                        "<html><body>Doctor Chaos:<br>Why are you standing here? Don't you see it's a private propertie? Don't look at him with those eyes... Did you smile?! Don't make fun of me! He will ... destroy ... you ... if you continue!</body></html>"
            else if (_pissedOffTimer in 1..10)
                htmltext =
                        "<html><body>Doctor Chaos:<br>I know why you are here, traitor! He discovered your plans! You are assassin ... sent by the Black Anvil guild! But you won't kill the Emperor of Evil!</body></html>"
            else if (_pissedOffTimer <= 0)
            // That was "too much" for that time.
                crazyMidgetBecomesAngry(npc)
        }

        return htmltext
    }

    override fun onSpawn(npc: Npc): String? {
        // 30 seconds timer at initialization.
        _pissedOffTimer = 30

        // Initialization of the paranoia.
        startQuestTimer("paranoia_activity", 1000, npc, null, true)

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        cancelQuestTimer("golem_despawn", npc, null)
        npc.broadcastNpcSay("Urggh! You will pay dearly for this insult.")

        // "lock" Dr. Chaos for regular RB time (36H fixed +- 24H random)
        val respawnTime = ((36 + Rnd[-24, 24]) * 3600000).toLong()

        GrandBossManager.setBossStatus(CHAOS_GOLEM, DEAD.toInt())
        startQuestTimer("reset_drchaos", respawnTime, null, null, false)

        // also save the respawn time so that the info is maintained past reboots
        val info = GrandBossManager.getStatsSet(CHAOS_GOLEM) ?: return null
        info.set("respawn_time", System.currentTimeMillis() + respawnTime)
        GrandBossManager.setStatsSet(CHAOS_GOLEM, info)

        return null
    }

    override fun onAttackAct(npc: Npc, victim: Player): String? {
        // Choose a message from 3 choices (1/100), and make him speak.
        val chance = Rnd[300]
        if (chance < 3)
            npc.broadcastNpcSay(SHOUTS[chance])

        return null
    }

    /**
     * Launches the complete animation.
     * @param npc the midget.
     */
    private fun crazyMidgetBecomesAngry(npc: Npc) {
        if (GrandBossManager.getBossStatus(CHAOS_GOLEM) != NORMAL.toInt())
            return

        // Set the status to "crazy".
        GrandBossManager.setBossStatus(CHAOS_GOLEM, CRAZY.toInt())

        // Cancels the paranoia timer.
        cancelQuestTimer("paranoia_activity", npc, null)

        // Makes the NPC moves near the Strange Box speaking.
        npc.ai.setIntention(CtrlIntention.MOVE_TO, Location(96323, -110914, -3328))
        npc.broadcastNpcSay("Fools! Why haven't you fled yet? Prepare to learn a lesson!")

        // Delayed animation timers.
        startQuestTimer("1", 2000, npc, null, false) // 2 secs, time to launch dr.C anim 2. Cam 1 on.
        startQuestTimer("2", 4000, npc, null, false) // 2,5 secs, time to launch dr.C anim 3.
        startQuestTimer("3", 6500, npc, null, false) // 6 secs, time to launch dr.C anim 1.
        startQuestTimer("4", 12500, npc, null, false) // 4,5 secs to make the NPC moves to the grotto. Cam 2 on.
        startQuestTimer("5", 17000, npc, null, false) // 4 secs for golem spawn, and golem anim. Cam 3 on.
    }

    companion object {
        private const val DOCTOR_CHAOS = 32033
        private const val CHAOS_GOLEM = 25512

        private const val NORMAL: Byte = 0 // Dr. Chaos is in NPC form.
        private const val CRAZY: Byte = 1 // Dr. Chaos entered on golem form.
        private const val DEAD: Byte = 2 // Dr. Chaos has been killed and has not yet spawned.

        private val SHOUTS = arrayOf(
            "Bwah-ha-ha! Your doom is at hand! Behold the Ultra Secret Super Weapon!",
            "Foolish, insignificant creatures! How dare you challenge me!",
            "I see that none will challenge me now!"
        )
    }
}