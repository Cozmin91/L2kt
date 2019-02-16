package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.olympiad.OlympiadGameTask
import com.l2kt.gameserver.model.zone.SpawnZoneType
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExOlympiadMatchEnd
import com.l2kt.gameserver.network.serverpackets.ExOlympiadUserInfo
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * A zone extending [SpawnZoneType], used for olympiad event.<br></br>
 * <br></br>
 * Restart and the use of "summoning friend" skill aren't allowed. The zone is considered a pvp zone.
 */
class OlympiadStadiumZone(id: Int) : SpawnZoneType(id) {
    internal var _task: OlympiadGameTask? = null

    override fun onEnter(character: Creature) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true)
        character.setInsideZone(ZoneId.NO_RESTART, true)

        if (_task != null && _task!!.isBattleStarted) {
            character.setInsideZone(ZoneId.PVP, true)
            if (character is Player) {
                character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE))
                _task!!.game?.sendOlympiadInfo(character)
            }
        }

        // Only participants, observers and GMs are allowed.
        val player = character.actingPlayer
        if (player != null && !player.isGM && !player.isInOlympiadMode && !player.isInObserverMode) {
            val summon = player.pet
            summon?.unSummon(player)

            player.teleToLocation(MapRegionData.TeleportType.TOWN)
        }
    }

    override fun onExit(character: Creature) {
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false)
        character.setInsideZone(ZoneId.NO_RESTART, false)

        if (_task != null && _task!!.isBattleStarted) {
            character.setInsideZone(ZoneId.PVP, false)

            if (character is Player) {
                character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE))
                character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET)
            }
        }
    }

    fun updateZoneStatusForCharactersInside() {
        if (_task == null)
            return

        val battleStarted = _task!!.isBattleStarted
        val sm =
            SystemMessage.getSystemMessage(if (battleStarted) SystemMessageId.ENTERED_COMBAT_ZONE else SystemMessageId.LEFT_COMBAT_ZONE)

        for (character in _characters.values) {
            if (battleStarted) {
                character.setInsideZone(ZoneId.PVP, true)
                if (character is Player)
                    character.sendPacket(sm)
            } else {
                character.setInsideZone(ZoneId.PVP, false)
                if (character is Player) {
                    character.sendPacket(sm)
                    character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET)
                }
            }
        }
    }

    fun registerTask(task: OlympiadGameTask) {
        _task = task
    }

    fun broadcastStatusUpdate(player: Player) {
        val packet = ExOlympiadUserInfo(player)
        for (plyr in getKnownTypeInside(Player::class.java)) {
            if (plyr.isInObserverMode || plyr.olympiadSide != player.olympiadSide)
                plyr.sendPacket(packet)
        }
    }

    fun broadcastPacketToObservers(packet: L2GameServerPacket) {
        for (player in getKnownTypeInside(Player::class.java)) {
            if (player.isInObserverMode)
                player.sendPacket(packet)
        }
    }
}