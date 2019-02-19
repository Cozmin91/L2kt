package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

class GMViewSkillInfo(private val _activeChar: Player) : L2GameServerPacket() {
    private val _skills = _activeChar.skills.values

    override fun writeImpl() {
        writeC(0x91)
        writeS(_activeChar.name)
        writeD(_skills.size)

        var isDisabled = false
        if (_activeChar.clan != null)
            isDisabled = _activeChar.clan!!.reputationScore < 0

        for (skill in _skills) {
            writeD(if (skill.isPassive) 1 else 0)
            writeD(skill.level)
            writeD(skill.id)
            writeC(if (isDisabled && skill.isClanSkill) 1 else 0)
        }
    }
}