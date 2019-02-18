package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.commons.random.Rnd
import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

/**
 * AI for mobs in Plains of Dion (near Floran Village)
 */
class PlainsOfDion : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addAttackId(*MONSTERS)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (npc.isScriptValue(0)) {
            npc.broadcastNpcSay(MONSTERS_MSG[Rnd[5]].replace("\$s1", attacker.name))

            for (obj in npc.getKnownTypeInRadius(Monster::class.java, 300)) {
                if (!obj.isAttackingNow && !obj.isDead() && ArraysUtil.contains(MONSTERS, obj.npcId)) {
                    attack(obj, attacker)
                    obj.broadcastNpcSay(MONSTERS_ASSIST_MSG[Rnd[3]])
                }
            }
            npc.scriptValue = 1
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    companion object {
        private val MONSTERS = intArrayOf(
            21104, // Delu Lizardman Supplier
            21105, // Delu Lizardman Special Agent
            21107
        )// Delu Lizardman Commander

        private val MONSTERS_MSG = arrayOf(
            "\$s1! How dare you interrupt our fight! Hey guys, help!",
            "\$s1! Hey! We're having a duel here!",
            "The duel is over! Attack!",
            "Foul! Kill the coward!",
            "How dare you interrupt a sacred duel! You must be taught a lesson!"
        )

        private val MONSTERS_ASSIST_MSG = arrayOf("Die, you coward!", "Kill the coward!", "What are you looking at?")
    }
}