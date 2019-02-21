package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.entity.ClanHall
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

/**
 * An instance type extending [Doorman], used by clan hall doorman. The clan hall is linked during NPC spawn, based on distance.<br></br>
 * <br></br>
 * isOwnerClan() checks if the user is part of clan owning the clan hall.
 */
class ClanHallDoorman(objectID: Int, template: NpcTemplate) : Doorman(objectID, template) {
    private var _clanHall: ClanHall? = null

    override fun showChatWindow(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)

        if (_clanHall == null)
            return

        val html = NpcHtmlMessage(objectId)

        val owner = ClanTable.getClan(_clanHall!!.ownerId)
        if (isOwnerClan(player)) {
            html.setFile("data/html/clanHallDoormen/doormen.htm")
            html.replace("%clanname%", owner!!.name)
        } else {
            if (owner != null && owner.leader != null) {
                html.setFile("data/html/clanHallDoormen/doormen-no.htm")
                html.replace("%leadername%", owner.leaderName)
                html.replace("%clanname%", owner.name)
            } else {
                html.setFile("data/html/clanHallDoormen/emptyowner.htm")
                html.replace("%hallname%", _clanHall!!.name)
            }
        }
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }

    override fun openDoors(player: Player, command: String) {
        _clanHall!!.openCloseDoors(true)

        val html = NpcHtmlMessage(objectId)
        html.setFile("data/html/clanHallDoormen/doormen-opened.htm")
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }

    override fun closeDoors(player: Player, command: String) {
        _clanHall!!.openCloseDoors(false)

        val html = NpcHtmlMessage(objectId)
        html.setFile("data/html/clanHallDoormen/doormen-closed.htm")
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }

    override fun isOwnerClan(player: Player): Boolean {
        return _clanHall != null && player.clan != null && player.clanId == _clanHall!!.ownerId
    }

    override fun onSpawn() {
        _clanHall = ClanHallManager.getNearbyClanHall(x, y, 500)
        super.onSpawn()
    }
}