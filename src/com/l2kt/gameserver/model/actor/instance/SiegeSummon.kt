package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId

class SiegeSummon(objectId: Int, template: NpcTemplate, owner: Player, skill: L2Skill) :
    Servitor(objectId, template, owner, skill) {

    override fun onSpawn() {
        super.onSpawn()

        if (!isInsideZone(ZoneId.SIEGE)) {
            unSummon(owner)
            owner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED)
        }
    }

    override fun onTeleported() {
        super.onTeleported()

        if (!isInsideZone(ZoneId.SIEGE)) {
            unSummon(owner)
            owner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED)
        }
    }

    companion object {
        const val SIEGE_GOLEM_ID = 14737
        const val HOG_CANNON_ID = 14768
        const val SWOOP_CANNON_ID = 14839
    }
}