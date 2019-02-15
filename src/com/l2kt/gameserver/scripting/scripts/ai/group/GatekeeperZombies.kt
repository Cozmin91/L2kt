package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

/**
 * Those monsters don't attack at sight players owning itemId 8064, 8065 or 8067.
 */
class GatekeeperZombies : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addAggroRangeEnterId(22136)
    }

    override fun onAggro(npc: Npc, player: Player?, isPet: Boolean): String? {
        return if (player?.inventory!!.hasAtLeastOneItem(8064, 8065, 8067)) null else super.onAggro(
            npc,
            player,
            isPet
        )

    }
}