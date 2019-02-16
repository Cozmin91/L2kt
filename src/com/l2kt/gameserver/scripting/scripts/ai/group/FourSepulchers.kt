package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.FourSepulchersManager
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

class FourSepulchers : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addAttackId(18150, 18151, 18152, 18153, 18154, 18155, 18156, 18157)
        addKillId(18120, 18121, 18122, 18123, 18124, 18125, 18126, 18127, 18128, 18129, 18130, 18131, 18149, 18158, 18159, 18160, 18161, 18162, 18163, 18164, 18165, 18183, 18184, 18212, 18213, 18214, 18215, 18216, 18217, 18218, 18219, 18150, 18151, 18152, 18153, 18154, 18155, 18156, 18157, 18141, 18142, 18143, 18144, 18145, 18146, 18147, 18148, 18220, 18221, 18222, 18223, 18224, 18225, 18226, 18227, 18228, 18229, 18230, 18231, 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239, 18240, 25339, 25342, 25346, 25349
        )
        addSpawnId(18150, 18151, 18152, 18153, 18154, 18155, 18156, 18157, 18231, 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239, 18240, 18241, 18242, 18243, 25339, 25342, 25346, 25349
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        if (event.equals("safety", ignoreCase = true)) {
            if (npc != null && !npc.isDead && npc.isVisible) {
                FourSepulchersManager.spawnKeyBox(npc)
                npc.broadcastNpcSay("Thank you for saving me.")
                npc.deleteMe()
            }
        } else if (event.equals("aggro", ignoreCase = true)) {
            // Aggro a single Imperial Guard.
            for (guard in npc?.getKnownTypeInRadius(Attackable::class.java, 600) ?: emptyList()) {
                when (guard.npcId) {
                    18166, 18167, 18168, 18169 -> {
                        attack(guard, npc)
                        return null
                    }
                }
            }
        }
        return null
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (attacker is Attackable) {
            // Calculate random coords.
            val rndX = npc.x + Rnd[-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE]
            val rndY = npc.y + Rnd[-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE]

            // Wait the NPC to be immobile to move him again. Also check destination point.
            if (!npc.isMoving && GeoEngine.canMoveToTarget(npc.x, npc.y, npc.z, rndX, rndY, npc.z)) {
                // Set the NPC as running.
                npc.setRunning()

                // Move the NPC.
                npc.ai.setIntention(CtrlIntention.MOVE_TO, Location(rndX, rndY, npc.z))

                // 50% to call a specific player. If no player can be found, we use generic string.
                var playerToCall: Player? = null
                if (Rnd.nextBoolean())
                    playerToCall = Rnd[npc.getKnownTypeInRadius(Player::class.java, 1200)]

                npc.broadcastNpcSay(
                    if (playerToCall == null) "Help me!!" else "%s! Help me!!".replace(
                        "%s".toRegex(),
                        playerToCall.name
                    )
                )
            }
        }
        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        when (npc.npcId) {
            18120, 18121, 18122, 18123, 18124, 18125, 18126, 18127, 18128, 18129, 18130, 18131, 18149, 18158, 18159, 18160, 18161, 18162, 18163, 18164, 18165, 18183, 18184, 18212, 18213, 18214, 18215, 18216, 18217, 18218, 18219 -> FourSepulchersManager.spawnKeyBox(
                npc
            )

            18150 // Victims.
                , 18151, 18152, 18153, 18154, 18155, 18156, 18157 -> {
                FourSepulchersManager.spawnExecutionerOfHalisha(npc)
                cancelQuestTimer("safety", npc, null)
            }

            18141, 18142, 18143, 18144, 18145, 18146, 18147, 18148 -> FourSepulchersManager.testViscountMobsAnnihilation(
                npc.scriptValue
            )

            18220, 18221, 18222, 18223, 18224, 18225, 18226, 18227, 18228, 18229, 18230, 18231 // Petrified statues.
                , 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239, 18240 -> FourSepulchersManager.testDukeMobsAnnihilation(
                npc.scriptValue
            )

            25339 // Shadows.
                , 25342, 25346, 25349 -> {
                var cupId = 0
                when (npc.npcId) {
                    25339 -> cupId = 7256
                    25342 -> cupId = 7257
                    25346 -> cupId = 7258
                    25349 -> cupId = 7259
                }

                val player = killer.actingPlayer
                if (player != null) {
                    val party = killer.party
                    if (party != null) {
                        for (member in party.members) {
                            val qs = member.getQuestState(QUEST_ID)
                            if (qs != null && (qs.isStarted || qs.isCompleted) && member.inventory!!.getItemByItemId(
                                    ANTIQUE_BROOCH
                                ) == null && member.isInsideRadius(npc, Config.PARTY_RANGE, true, false)
                            )
                                member.addItem("Quest", cupId, 1, member, true)
                        }
                    } else {
                        val qs = player.getQuestState(QUEST_ID)
                        if (qs != null && (qs.isStarted || qs.isCompleted) && player.inventory!!.getItemByItemId(
                                ANTIQUE_BROOCH
                            ) == null && player.isInsideRadius(npc, Config.PARTY_RANGE, true, false)
                        )
                            player.addItem("Quest", cupId, 1, player, true)
                    }
                }
                FourSepulchersManager.spawnEmperorsGraveNpc(npc.scriptValue)
            }
        }
        return super.onKill(npc, killer)
    }

    override fun onSpawn(npc: Npc): String? {
        when (npc.npcId) {
            18150 // Victims.
                , 18151, 18152, 18153, 18154, 18155, 18156, 18157 -> {
                startQuestTimer("safety", 300000, npc, null, false)
                startQuestTimer("aggro", 1000, npc, null, false)
            }

            18231 // Petrified statues.
                , 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239, 18240, 18241, 18242, 18243 -> {
                SkillTable.FrequentSkill.FAKE_PETRIFICATION.skill!!.getEffects(npc, npc)
                npc.setIsNoRndWalk(true)
            }

            25339 // Shadows.
                , 25342, 25346, 25349 -> (npc as Attackable).setRaid(true)
        }
        return super.onSpawn(npc)
    }

    companion object {
        private const val QUEST_ID = "Q620_FourGoblets"

        private const val ANTIQUE_BROOCH = 7262
    }
}