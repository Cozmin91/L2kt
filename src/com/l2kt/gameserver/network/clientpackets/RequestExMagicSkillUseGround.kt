package com.l2kt.gameserver.network.clientpackets

import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.serverpackets.ValidateLocation

class RequestExMagicSkillUseGround : L2GameClientPacket() {
    private var _x: Int = 0
    private var _y: Int = 0
    private var _z: Int = 0

    private var _skillId: Int = 0

    private var _ctrlPressed: Boolean = false
    private var _shiftPressed: Boolean = false

    override fun readImpl() {
        _x = readD()
        _y = readD()
        _z = readD()

        _skillId = readD()

        _ctrlPressed = readD() != 0
        _shiftPressed = readC() != 0
    }

    override fun runImpl() {
        // Get the current player
        val player = client.activeChar ?: return

        // Get the L2Skill template corresponding to the skillID received from the client
        val skill = player.getSkill(_skillId) ?: return

        player.currentSkillWorldPosition = Location(_x, _y, _z)

        // normally magicskilluse packet turns char client side but for these skills, it doesn't (even with correct target)
        player.heading = MathUtil.calculateHeadingFrom(player.x, player.y, _x, _y)
        player.broadcastPacket(ValidateLocation(player))

        player.useMagic(skill, _ctrlPressed, _shiftPressed)
    }
}