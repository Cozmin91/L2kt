package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType
import com.l2kt.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo
import com.l2kt.gameserver.network.serverpackets.ServerObjectInfo

/**
 * A zone extending [ZoneType], used for the water behavior. [Player]s can drown if they stay too long below water line.
 */
class WaterZone(id: Int) : ZoneType(id) {

    val waterZ: Int
        get() = zone!!.highZ

    override fun onEnter(character: Creature) {
        character.setInsideZone(ZoneId.WATER, true)

        if (character is Player)
            character.broadcastUserInfo()
        else if (character is Npc) {
            for (player in character.getKnownType(Player::class.java)) {
                if (character.getMoveSpeed() == 0)
                    player.sendPacket(ServerObjectInfo(character, player))
                else
                    player.sendPacket(NpcInfo(character, player))
            }
        }
    }

    override fun onExit(character: Creature) {
        character.setInsideZone(ZoneId.WATER, false)

        if (character is Player)
            character.broadcastUserInfo()
        else if (character is Npc) {
            for (player in character.getKnownType(Player::class.java)) {
                if (character.getMoveSpeed() == 0)
                    player.sendPacket(ServerObjectInfo(character, player))
                else
                    player.sendPacket(NpcInfo(character, player))
            }
        }
    }
}