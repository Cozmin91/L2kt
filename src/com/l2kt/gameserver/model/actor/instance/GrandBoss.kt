package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.RaidPointManager
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.entity.Hero
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * This class manages all Grand Bosses.
 */
class GrandBoss
/**
 * Constructor for L2GrandBossInstance. This represent all grandbosses.
 * @param objectId ID of the instance
 * @param template L2NpcTemplate of the instance
 */
    (objectId: Int, template: NpcTemplate) : Monster(objectId, template) {
    init {
        setRaid(true)
    }

    override fun onSpawn() {
        setIsNoRndWalk(true)
        super.onSpawn()
    }

    override fun doDie(killer: Creature): Boolean {
        if (!super.doDie(killer))
            return false

        val player = killer.actingPlayer
        if (player != null) {
            broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL))
            broadcastPacket(PlaySound("systemmsg_e.1209"))

            val party = player.party
            if (party != null) {
                for (member in party.members) {
                    RaidPointManager.addPoints(member, npcId, level / 2 + Rnd[-5, 5])
                    if (member.isNoble)
                        Hero.setRBkilled(member.objectId, npcId)
                }
            } else {
                RaidPointManager.addPoints(player, npcId, level / 2 + Rnd[-5, 5])
                if (player.isNoble)
                    Hero.setRBkilled(player.objectId, npcId)
            }
        }

        return true
    }

    override fun returnHome(): Boolean {
        return false
    }
}