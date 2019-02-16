package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player

import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ConfirmDlg
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.skills.L2SkillType

class SummonFriend : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

// Check player status.
        if (!activeChar.checkSummonerStatus())
            return

        for (obj in targets) {
            // The target must be a player.
            if (obj !is Player)
                continue

            // Can't summon yourself.
            if (activeChar === obj)
                continue

            // Check target status.
            if (!activeChar.checkSummonTargetStatus(obj))
                continue

            // Check target distance.
            if (MathUtil.checkIfInRange(50, activeChar, obj, false))
                continue

            // Check target teleport request status.
            if (!obj.teleportRequest(activeChar, skill)) {
                activeChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_SUMMONED).addCharName(
                        obj
                    )
                )
                continue
            }

            // Send a request for Summon Friend skill.
            if (skill.id == 1403) {
                val confirm = ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.id)
                confirm.addCharName(activeChar)
                confirm.addZoneName(activeChar.position)
                confirm.addTime(30000)
                confirm.addRequesterId(activeChar.objectId)
                obj.sendPacket(confirm)
            } else {
                obj.teleportToFriend(activeChar, skill)
                obj.teleportRequest(null, null)
            }
        }
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.SUMMON_FRIEND)
    }
}