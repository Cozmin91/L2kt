package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import java.util.*

/**
 * Angel spawns... When one of the angels in the keys dies, the other angel will spawn.
 */
class PolymorphingAngel : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addEventIds(ANGELSPAWNS.keys, EventType.ON_KILL)
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val newNpc = addSpawn(ANGELSPAWNS[npc.npcId]!!, npc, false, 0, false) as Attackable
        attack(newNpc, killer)

        return super.onKill(npc, killer)
    }

    companion object {
        private val ANGELSPAWNS = HashMap<Int, Int>()

        init {
            ANGELSPAWNS[20830] = 20859
            ANGELSPAWNS[21067] = 21068
            ANGELSPAWNS[21062] = 21063
            ANGELSPAWNS[20831] = 20860
            ANGELSPAWNS[21070] = 21071
        }
    }
}