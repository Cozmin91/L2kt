package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

/**
 * Primeval Isle AIs. This script controls following behaviors :
 *
 *  * Sprigant : casts a spell if you enter in aggro range, finish task if die or none around.
 *  * Ancient Egg : call all NPCs in a 2k range if attacked.
 *  * Pterosaurs and Tyrannosaurus : can see through Silent Move.
 *
 */
class PrimevalIsle : L2AttackableAIScript("ai/group") {
    init {

        for (npc in SpawnTable.spawnTable)
            if (ArraysUtil.contains(MOBIDS, npc.npcId) && npc.npc != null && npc.npc is Attackable)
                (npc.npc as Attackable).seeThroughSilentMove(true)
    }

    override fun registerNpcs() {
        addEventIds(SPRIGANTS, EventType.ON_AGGRO, EventType.ON_KILL)
        addAttackId(ANCIENT_EGG)
        addSpawnId(*MOBIDS)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        if (npc !is Attackable)
            return null

        if (event.equals("skill", ignoreCase = true)) {
            var playableCounter = 0
            for (playable in npc.getKnownTypeInRadius(Playable::class.java, npc.getTemplate().aggroRange)) {
                if (!playable.isDead)
                    playableCounter++
            }

            // If no one is inside aggro range, drop the task.
            if (playableCounter == 0) {
                cancelQuestTimer("skill", npc, null)
                return null
            }

            npc.setTarget(npc)
            npc.doCast(if (npc.getNpcId() == 18345) ANESTHESIA else POISON)
        }
        return null
    }

    override fun onAggro(npc: Npc, player: Player?, isPet: Boolean): String? {
        if (player == null)
            return null

        // Instant use
        npc.target = npc
        npc.doCast(if (npc.npcId == 18345) ANESTHESIA else POISON)

        // Launch a task every 15sec.
        if (getQuestTimer("skill", npc, null) == null)
            startQuestTimer("skill", 15000, npc, null, true)

        return super.onAggro(npc, player, isPet)
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        if (getQuestTimer("skill", npc, null) != null)
            cancelQuestTimer("skill", npc, null)

        return super.onKill(npc, killer)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        // Make all mobs found in a radius 2k aggressive towards attacker.
        for (called in attacker.getKnownTypeInRadius(Attackable::class.java, 2000)) {
            // Caller hasn't AI or is dead.
            if (!called.hasAI() || called.isDead)
                continue

            // Check if the Attackable can help the actor.
            val calledIntention = called.ai.desire.intention
            if (calledIntention == CtrlIntention.IDLE || calledIntention == CtrlIntention.ACTIVE || calledIntention == CtrlIntention.MOVE_TO && !called.isRunning)
                attack(called, attacker, 1)
        }

        return null
    }

    override fun onSpawn(npc: Npc): String? {
        if (npc is Attackable)
            npc.seeThroughSilentMove(true)

        return super.onSpawn(npc)
    }

    companion object {
        private val SPRIGANTS = intArrayOf(18345, 18346)

        private val MOBIDS = intArrayOf(22199, 22215, 22216, 22217)

        private const val ANCIENT_EGG = 18344

        private val ANESTHESIA = SkillTable.getInfo(5085, 1)
        private val POISON = SkillTable.getInfo(5086, 1)
    }
}