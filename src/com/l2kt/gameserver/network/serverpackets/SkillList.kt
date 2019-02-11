package com.l2kt.gameserver.network.serverpackets

class SkillList : L2GameServerPacket() {
    private val _skills = mutableListOf<Skill>()

    internal class Skill(var id: Int, var level: Int, var passive: Boolean, var disabled: Boolean)

    fun addSkill(id: Int, level: Int, passive: Boolean, disabled: Boolean) {
        _skills.add(Skill(id, level, passive, disabled))
    }

    override fun writeImpl() {
        writeC(0x58)
        writeD(_skills.size)

        for (temp in _skills) {
            writeD(if (temp.passive) 1 else 0)
            writeD(temp.level)
            writeD(temp.id)
            writeC(if (temp.disabled) 1 else 0)
        }
    }
}