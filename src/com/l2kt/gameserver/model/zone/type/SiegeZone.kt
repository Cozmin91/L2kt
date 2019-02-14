package com.l2kt.gameserver.model.zone.type

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.SiegeSummon
import com.l2kt.gameserver.model.zone.SpawnZoneType
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.taskmanager.PvpFlagTaskManager

/**
 * A zone extending [SpawnZoneType], used for castle on siege progress, and which handles following spawns type :
 *
 *  * Generic spawn locs : other_restart_village_list (spawns used on siege, to respawn on second closest town.
 *  * Chaotic spawn locs : chao_restart_point_list (spawns used on siege, to respawn PKs on second closest town.
 *
 */
class SiegeZone(id: Int) : SpawnZoneType(id) {
    var siegeObjectId = -1
        private set
    var isActive = false

    override fun setParameter(name: String, value: String) {
        if (name == "castleId" || name == "clanHallId")
            siegeObjectId = Integer.parseInt(value)
        else
            super.setParameter(name, value)
    }

    override fun onEnter(character: Creature) {
        if (isActive) {
            character.setInsideZone(ZoneId.PVP, true)
            character.setInsideZone(ZoneId.SIEGE, true)
            character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true)

            if (character is Player) {

                character.setIsInSiege(true) // in siege

                character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE)
                character.enterOnNoLandingZone()
            }
        }
    }

    override fun onExit(character: Creature) {
        character.setInsideZone(ZoneId.PVP, false)
        character.setInsideZone(ZoneId.SIEGE, false)
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false)

        if (character is Player) {

            if (isActive) {
                character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE)
                character.exitOnNoLandingZone()

                PvpFlagTaskManager.add(character, Config.PVP_NORMAL_TIME.toLong())

                // Set pvp flag
                if (character.pvpFlag.toInt() == 0)
                    character.updatePvPFlag(1)
            }

            character.setIsInSiege(false)
        } else if (character is SiegeSummon)
            character.unSummon(character.owner)
    }

    fun updateZoneStatusForCharactersInside() {
        if (isActive) {
            for (character in _characters.values)
                onEnter(character)
        } else {
            for (character in _characters.values) {
                character.setInsideZone(ZoneId.PVP, false)
                character.setInsideZone(ZoneId.SIEGE, false)
                character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false)

                if (character is Player) {

                    character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE)
                    character.exitOnNoLandingZone()
                } else if (character is SiegeSummon)
                    character.unSummon(character.owner)
            }
        }
    }

    /**
     * Sends a message to all players in this zone
     * @param message
     */
    fun announceToPlayers(message: String) {
        for (player in getKnownTypeInside(Player::class.java))
            player.sendMessage(message)
    }

    /**
     * Kick [Player]s who don't belong to the clan set as parameter from this zone. They are ported to chaotic or regular spawn locations depending of their karma.
     * @param clanId : The castle owner id. Related players aren't teleported out.
     */
    fun banishForeigners(clanId: Int) {
        if (_characters.isEmpty())
            return

        for (player in getKnownTypeInside(Player::class.java)) {
            if (player.clanId == clanId)
                continue

            player.teleToLocation(if (player.karma > 0) randomChaoticLoc else randomLoc, 20)
        }
    }
}