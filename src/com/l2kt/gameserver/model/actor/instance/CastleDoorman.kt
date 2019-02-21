package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.pledge.Clan
import java.util.*

/**
 * An instance type extending [Doorman], used by castle doorman.<br></br>
 * <br></br>
 * isUnderSiege() checks current siege state associated to the doorman castle, while isOwnerClan() checks if the user is part of clan owning the castle and got the rights to open/close doors.
 */
class CastleDoorman(objectID: Int, template: NpcTemplate) : Doorman(objectID, template) {

    override fun openDoors(player: Player, command: String) {
        val st = StringTokenizer(command.substring(10), ", ")
        st.nextToken()

        while (st.hasMoreTokens()) {/*
			 * if (getConquerableHall() != null) getConquerableHall().openCloseDoor(Integer.parseInt(st.nextToken()), true); else
			 */
            castle.openDoor(player, Integer.parseInt(st.nextToken()))
        }
    }

    override fun closeDoors(player: Player, command: String) {
        val st = StringTokenizer(command.substring(11), ", ")
        st.nextToken()

        while (st.hasMoreTokens()) {/*
			 * if (getConquerableHall() != null) getConquerableHall().openCloseDoor(Integer.parseInt(st.nextToken()), false); else
			 */
            castle.closeDoor(player, Integer.parseInt(st.nextToken()))
        }
    }

    override fun isOwnerClan(player: Player): Boolean {
        if (player.clan != null) {/*
			 * if (getConquerableHall() != null) { // player should have privileges to open doors if (player.getClanId() == getConquerableHall().getOwnerId() && (player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR) return true; } else
			 */
            if (castle != null) {
                // player should have privileges to open doors
                if (player.clanId == castle.ownerId && player.clanPrivileges and Clan.CP_CS_OPEN_DOOR == Clan.CP_CS_OPEN_DOOR)
                    return true
            }
        }
        return false
    }

    override val isUnderSiege: Boolean
        get() = castle.siegeZone!!.isActive
}