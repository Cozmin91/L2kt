package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import java.util.*

/**
 * Summon minions the first time being hitten.<br></br>
 * For Orcs case, send also a message.
 */
class SummonMinions : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addEventIds(MINIONS.keys, EventType.ON_ATTACK)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (npc.isScriptValue(0)) {
            val npcId = npc.npcId
            if (npcId != 20767) {
                for (`val` in MINIONS[npcId] ?: IntArray(0)) {
                    val newNpc = addSpawn(`val`, npc, true, 0, false) as Attackable
                    attack(newNpc, attacker)
                }
            } else {
                for (`val` in MINIONS[npcId] ?: IntArray(0))
                    addSpawn(`val`, npc, true, 0, false)

                npc.broadcastNpcSay(Rnd[ORCS_WORDS])
            }
            npc.scriptValue = 1
        }

        return super.onAttack(npc, attacker, damage, skill)
    }

    companion object {
        private val ORCS_WORDS = arrayOf(
            "Come out, you children of darkness!",
            "Destroy the enemy, my brothers!",
            "Show yourselves!",
            "Forces of darkness! Follow me!"
        )

        private val MINIONS = HashMap<Int, IntArray>()

        init {
            MINIONS[20767] = intArrayOf(20768, 20769, 20770) // Timak Orc Troop
            MINIONS[21524] = intArrayOf(21525) // Blade of Splendor
            MINIONS[21531] = intArrayOf(21658) // Punishment of Splendor
            MINIONS[21539] = intArrayOf(21540) // Wailing of Splendor
        }
    }
}