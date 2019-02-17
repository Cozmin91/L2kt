package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.ClanHall
import com.l2kt.gameserver.model.zone.SpawnZoneType
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.serverpackets.ClanHallDecoration

/**
 * A zone extending [SpawnZoneType] used by [ClanHall]s.
 */
class ClanHallZone(id: Int) : SpawnZoneType(id) {
    var clanHallId: Int = 0
        private set

    override fun setParameter(name: String, value: String) {
        if (name == "clanHallId") {
            clanHallId = Integer.parseInt(value)

            // Register self to the correct clan hall
            ClanHallManager.getClanHallById(clanHallId)!!.zone = this
        } else
            super.setParameter(name, value)
    }

    override fun onEnter(character: Creature) {
        if (character is Player) {
            // Set as in clan hall
            character.setInsideZone(ZoneId.CLAN_HALL, true)

            val ch = ClanHallManager.getClanHallById(clanHallId) ?: return

            // Send decoration packet
            character.sendPacket(ClanHallDecoration(ch))
        }
    }

    override fun onExit(character: Creature) {
        if (character is Player)
            character.setInsideZone(ZoneId.CLAN_HALL, false)
    }

    /**
     * Kick [Player]s who don't belong to the clan set as parameter from this zone. They are ported to town.
     * @param clanId : The clanhall owner id. Related players aren't teleported out.
     */
    fun banishForeigners(clanId: Int) {
        if (_characters.isEmpty())
            return

        for (player in getKnownTypeInside(Player::class.java)) {
            if (player.clanId == clanId)
                continue

            player.teleToLocation(MapRegionData.TeleportType.TOWN)
        }
    }
}