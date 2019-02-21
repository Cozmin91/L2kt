package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.model.actor.template.NpcTemplate

/**
 * An instance type extending [Folk], used by Guardian of Border NPC (internal room rift teleporters).
 */
class BorderGuard(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        val party = player.party ?: return

        val rift = party.dimensionalRift ?: return

        if (command.startsWith("ChangeRiftRoom"))
            rift.manualTeleport(player, this)
        else if (command.startsWith("ExitRift"))
            rift.manualExitRift(player, this)
    }

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        return SevenSigns.SEVEN_SIGNS_HTML_PATH + "rift/GuardianOfBorder.htm"
    }
}