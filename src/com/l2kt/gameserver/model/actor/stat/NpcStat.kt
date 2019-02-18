package com.l2kt.gameserver.model.actor.stat

import com.l2kt.gameserver.model.actor.Npc

class NpcStat(activeChar: Npc) : CreatureStat(activeChar) {

    override var level: Byte
        get() = activeChar!!.template.level
        set(value: Byte) {
            super.level = value
        }

    override val activeChar: Npc? get() = super.activeChar as Npc?
}