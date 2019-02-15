package com.l2kt.gameserver.scripting.scripts.teleports

import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest

/**
 * Spawn Gatekeepers at Lilith/Anakim deaths (after a 10sec delay).<BR></BR>
 * Despawn them after 15 minutes.
 */
class GatekeeperSpirit : Quest(-1, "teleports") {
    init {

        addStartNpc(ENTER_GK)
        addFirstTalkId(ENTER_GK)
        addTalkId(ENTER_GK)

        addKillId(LILITH, ANAKIM)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        if (event.equals("lilith_exit", ignoreCase = true))
            addSpawn(EXIT_GK, 184446, -10112, -5488, 0, false, 900000, false)
        else if (event.equals("anakim_exit", ignoreCase = true))
            addSpawn(EXIT_GK, 184466, -13106, -5488, 0, false, 900000, false)

        return super.onAdvEvent(event, npc, player)
    }

    override fun onFirstTalk(npc: Npc, player: Player): String? {
        val playerCabal = SevenSigns.getInstance().getPlayerCabal(player.objectId)
        val sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SealType.AVARICE)
        val winningCabal = SevenSigns.getInstance().cabalHighestScore

        if (playerCabal == sealAvariceOwner && playerCabal == winningCabal) {
            when (sealAvariceOwner) {
                SevenSigns.CabalType.DAWN -> return "dawn.htm"

                SevenSigns.CabalType.DUSK -> return "dusk.htm"
            }
        }

        npc.showChatWindow(player)
        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        when (npc.npcId) {
            LILITH -> startQuestTimer("lilith_exit", 10000, null, null, false)

            ANAKIM -> startQuestTimer("anakim_exit", 10000, null, null, false)
        }
        return super.onKill(npc, killer)
    }

    companion object {
        private const val ENTER_GK = 31111
        private const val EXIT_GK = 31112
        private const val LILITH = 25283
        private const val ANAKIM = 25286
    }
}