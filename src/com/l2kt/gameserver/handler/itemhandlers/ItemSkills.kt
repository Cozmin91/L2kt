package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Servitor
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExUseSharedGroupItem
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * Template for item skills handler.
 */
open class ItemSkills : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable is Servitor)
            return

        val isPet = playable is Pet
        val activeChar = playable.actingPlayer

        // Pets can only use tradable items.
        if (isPet && !item.isTradable) {
            activeChar!!.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS)
            return
        }

        val skills = item.etcItem!!.skills
        if (skills == null) {
            IItemHandler.Companion.LOGGER.warn("{} doesn't have any registered skill for handler.", item.name)
            return
        }

        for (skillInfo in skills) {
            if (skillInfo == null)
                continue

            val itemSkill = skillInfo.skill ?: continue

            if (!itemSkill.checkCondition(playable, playable.target, false))
                return

            // No message on retail, the use is just forgotten.
            if (playable.isSkillDisabled(itemSkill))
                return

            if (!itemSkill.isPotion && playable.isCastingNow)
                return

            // Item consumption is setup here.
            if (itemSkill.isPotion || itemSkill.isSimultaneousCast) {
                if (!item.isHerb) {
                    // Normal item consumption is 1, if more, it must be given in DP with getItemConsume().
                    if (!playable.destroyItem(
                            "Consume",
                            item.objectId,
                            if (itemSkill.itemConsumeId == 0 && itemSkill.itemConsume > 0) itemSkill.itemConsume else 1,
                            null,
                            false
                        )
                    ) {
                        activeChar!!.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)
                        return
                    }
                }

                playable.doSimultaneousCast(itemSkill)
                // Summons should be affected by herbs too, self time effect is handled at L2Effect constructor.
                if (!isPet && item.itemType === EtcItemType.HERB && activeChar!!.hasServitor())
                    activeChar.pet!!.doSimultaneousCast(itemSkill)
            } else {
                // Normal item consumption is 1, if more, it must be given in DP with getItemConsume().
                if (!playable.destroyItem(
                        "Consume",
                        item.objectId,
                        if (itemSkill.itemConsumeId == 0 && itemSkill.itemConsume > 0) itemSkill.itemConsume else 1,
                        null,
                        false
                    )
                ) {
                    activeChar!!.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)
                    return
                }

                playable.ai.setIntention(CtrlIntention.IDLE)
                if (!playable.useMagic(itemSkill, forceUse, false))
                    return
            }

            // Send message to owner.
            if (isPet)
                activeChar!!.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1).addSkillName(
                        itemSkill
                    )
                )
            else {
                val skillId = skillInfo.id

                // Buff icon for healing potions.
                when (skillId) {
                    2031, 2032, 2037 -> {
                        val buffId = activeChar!!.shortBuffTaskSkillId

                        // Greater healing potions.
                        if (skillId == 2037)
                            activeChar.shortBuffStatusUpdate(skillId, skillInfo.value, itemSkill.buffDuration / 1000)
                        else if (skillId == 2032 && buffId != 2037)
                            activeChar.shortBuffStatusUpdate(skillId, skillInfo.value, itemSkill.buffDuration / 1000)
                        else {
                            if (buffId != 2037 && buffId != 2032)
                                activeChar.shortBuffStatusUpdate(
                                    skillId,
                                    skillInfo.value,
                                    itemSkill.buffDuration / 1000
                                )
                        }// Lesser healing potions.
                        // Healing potions.
                    }
                }
            }

            // Reuse.
            var reuseDelay = itemSkill.reuseDelay
            if (item.isEtcItem) {
                if (item.etcItem!!.reuseDelay > reuseDelay)
                    reuseDelay = item.etcItem!!.reuseDelay

                playable.addTimeStamp(itemSkill, reuseDelay.toLong())
                if (reuseDelay != 0)
                    playable.disableSkill(itemSkill, reuseDelay.toLong())

                if (!isPet) {
                    val group = item.etcItem!!.sharedReuseGroup
                    if (group >= 0)
                        activeChar!!.sendPacket(ExUseSharedGroupItem(item.itemId, group, reuseDelay, reuseDelay))
                }
            } else if (reuseDelay > 0) {
                playable.addTimeStamp(itemSkill, reuseDelay.toLong())
                playable.disableSkill(itemSkill, reuseDelay.toLong())
            }
        }
    }
}