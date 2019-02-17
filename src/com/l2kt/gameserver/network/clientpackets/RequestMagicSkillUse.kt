package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.ai.NextAction
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.templates.skills.L2SkillType

class RequestMagicSkillUse : L2GameClientPacket() {
    private var _skillId: Int = 0
    protected var _ctrlPressed: Boolean = false
    protected var _shiftPressed: Boolean = false

    override fun readImpl() {
        _skillId = readD()
        _ctrlPressed = readD() != 0
        _shiftPressed = readC() != 0
    }

    override fun runImpl() {
        // Get the current player
        val player = client.activeChar ?: return

        // Get the L2Skill template corresponding to the skillID received from the client
        val skill = player.getSkill(_skillId)
        if (skill == null) {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        // If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
        if (skill.skillType === L2SkillType.RECALL && !Config.KARMA_PLAYER_CAN_TELEPORT && player.karma > 0) {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        // players mounted on pets cannot use any toggle skills
        if (skill.isToggle && player.isMounted) {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (player.isOutOfControl) {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        if (player.isAttackingNow)
            player.ai.setNextAction(NextAction(CtrlEvent.EVT_READY_TO_ACT, CtrlIntention.CAST, Runnable{
                player.useMagic(
                    skill,
                    _ctrlPressed,
                    _shiftPressed
                )
            }))
        else
            player.useMagic(skill, _ctrlPressed, _shiftPressed)
    }
}