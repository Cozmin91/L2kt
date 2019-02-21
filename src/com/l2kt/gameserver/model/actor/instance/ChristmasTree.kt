package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import java.util.concurrent.ScheduledFuture

/**
 * Christmas trees used on events.<br></br>
 * The special tree (npcId 13007) emits a regen aura, but only when set outside a peace zone.
 */
class ChristmasTree(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    private var _aiTask: ScheduledFuture<*>? = null

    init {
        run{
            if (template.npcId == SPECIAL_TREE_ID && !isInsideZone(ZoneId.TOWN)) {
                val recoveryAura = SkillTable.FrequentSkill.SPECIAL_TREE_RECOVERY_BONUS.skill ?: return@run

                _aiTask = ThreadPool.scheduleAtFixedRate(Runnable{
                    for (player in getKnownTypeInRadius(Player::class.java, 200)) {
                        if (player.getFirstEffect(recoveryAura) == null)
                            recoveryAura!!.getEffects(player, player)
                    }
                }, 3000, 3000)
            }
        }

    }

    override fun deleteMe() {
        if (_aiTask != null) {
            _aiTask!!.cancel(true)
            _aiTask = null
        }
        super.deleteMe()
    }

    override fun onAction(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    companion object {
        val SPECIAL_TREE_ID = 13007
    }
}