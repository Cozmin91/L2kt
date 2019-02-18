package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.CreatureSay
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import java.util.*

class PolymorphingOnAttack : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addEventIds(MOBSPAWNS.keys, EventType.ON_ATTACK)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (npc.isVisible && !npc.isDead()) {
            val tmp = MOBSPAWNS[npc.npcId]
            if (tmp != null) {
                if (npc.currentHp <= npc.maxHp * tmp[1] / 100.0 && Rnd[100] < tmp[2]) {
                    if (tmp[3] >= 0) {
                        val text = Rnd[MOBTEXTS[tmp[3]]]
                        npc.broadcastPacket(CreatureSay(npc.objectId, Say2.ALL, npc.name, text!!))
                    }
                    npc.deleteMe()

                    val newNpc = addSpawn(tmp[0], npc, false, 0, true) as Attackable
                    attack(newNpc, attacker)
                }
            }
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    companion object {
        private val MOBSPAWNS = HashMap<Int, Array<Int>>()

        init {
            MOBSPAWNS[21258] =
                    arrayOf(21259, 100, 100, -1) // Fallen Orc Shaman -> Sharp Talon Tiger (always polymorphs)
            MOBSPAWNS[21261] = arrayOf(21262, 100, 20, 0) // Ol Mahum Transcender 1st stage
            MOBSPAWNS[21262] = arrayOf(21263, 100, 10, 1) // Ol Mahum Transcender 2nd stage
            MOBSPAWNS[21263] = arrayOf(21264, 100, 5, 2) // Ol Mahum Transcender 3rd stage
            MOBSPAWNS[21265] = arrayOf(21271, 100, 33, 0) // Cave Ant Larva -> Cave Ant
            MOBSPAWNS[21266] = arrayOf(21269, 100, 100, -1) // Cave Ant Larva -> Cave Ant (always polymorphs)
            MOBSPAWNS[21267] = arrayOf(21270, 100, 100, -1) // Cave Ant Larva -> Cave Ant Soldier (always polymorphs)
            MOBSPAWNS[21271] = arrayOf(21272, 66, 10, 1) // Cave Ant -> Cave Ant Soldier
            MOBSPAWNS[21272] = arrayOf(21273, 33, 5, 2) // Cave Ant Soldier -> Cave Noble Ant
            MOBSPAWNS[21521] = arrayOf(21522, 100, 30, -1) // Claws of Splendor
            MOBSPAWNS[21527] = arrayOf(21528, 100, 30, -1) // Anger of Splendor
            MOBSPAWNS[21533] = arrayOf(21534, 100, 30, -1) // Alliance of Splendor
            MOBSPAWNS[21537] = arrayOf(21538, 100, 30, -1) // Fang of Splendor
        }

        private val MOBTEXTS = arrayOf(
            arrayOf(
                "Enough fooling around. Get ready to die!",
                "You idiot! I've just been toying with you!",
                "Now the fun starts!"
            ),
            arrayOf(
                "I must admit, no one makes my blood boil quite like you do!",
                "Now the battle begins!",
                "Witness my true power!"
            ),
            arrayOf("Prepare to die!", "I'll double my strength!", "You have more skill than I thought")
        )
    }
}