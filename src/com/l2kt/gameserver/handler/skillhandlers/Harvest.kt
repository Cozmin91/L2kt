package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.skills.L2SkillType

class Harvest : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

        val `object` = targets[0] as? Monster ?: return

        if (activeChar.objectId != `object`.seederId) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST)
            return
        }

        var send = false
        var total = 0
        var cropId = 0

        if (`object`.isSeeded) {
            if (calcSuccess(activeChar, `object`)) {
                val items = `object`.harvestItems
                if (!items.isEmpty()) {
                    val iu = InventoryUpdate()
                    for (ritem in items) {
                        cropId = ritem.id // always got 1 type of crop as reward

                        if (activeChar.isInParty)
                            activeChar.party!!.distributeItem(activeChar, ritem, true, `object`)
                        else {
                            val item =
                                activeChar.inventory!!.addItem("Manor", ritem.id, ritem.value, activeChar, `object`)
                            iu.addItem(item)

                            send = true
                            total += ritem.value
                        }
                    }

                    if (send) {
                        activeChar.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(
                                cropId
                            ).addNumber(total)
                        )

                        if (activeChar.isInParty)
                            activeChar.party!!.broadcastToPartyMembers(
                                activeChar,
                                SystemMessage.getSystemMessage(SystemMessageId.S1_HARVESTED_S3_S2S).addCharName(
                                    activeChar
                                ).addItemName(cropId).addNumber(total)
                            )

                        activeChar.sendPacket(iu)
                    }
                }
            } else
                activeChar.sendPacket(SystemMessageId.THE_HARVEST_HAS_FAILED)
        } else
            activeChar.sendPacket(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN)
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.HARVEST)

        private fun calcSuccess(activeChar: Creature, target: Creature): Boolean {
            var basicSuccess = 100
            val levelPlayer = activeChar.level
            val levelTarget = target.level

            var diff = levelPlayer - levelTarget
            if (diff < 0)
                diff = -diff

            // apply penalty, target <=> player levels, 5% penalty for each level
            if (diff > 5)
                basicSuccess -= (diff - 5) * 5

            // success rate cant be less than 1%
            if (basicSuccess < 1)
                basicSuccess = 1

            return Rnd[99] < basicSuccess
        }
    }
}