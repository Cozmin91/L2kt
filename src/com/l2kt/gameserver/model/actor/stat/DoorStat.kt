package com.l2kt.gameserver.model.actor.stat

import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Door

class DoorStat(activeChar: Door) : CreatureStat(activeChar) {
    var upgradeHpRatio: Int = 0

    override val maxHp: Int
        get() = super.maxHp * upgradeHpRatio

    init {

        upgradeHpRatio = 1
    }

    override val activeChar: Door? get() = super.activeChar as Door?

    override fun getMDef(target: Creature?, skill: L2Skill?): Int {
        var defense = activeChar!!.template.baseMDef

        when (SevenSigns.getSealOwner(SealType.STRIFE)) {
            SevenSigns.CabalType.DAWN -> defense *= 1.2

            SevenSigns.CabalType.DUSK -> defense *= 0.3
        }

        return defense.toInt()
    }

    override fun getPDef(target: Creature?): Int {
        var defense = activeChar!!.template.basePDef

        when (SevenSigns.getSealOwner(SealType.STRIFE)) {
            SevenSigns.CabalType.DAWN -> defense *= 1.2

            SevenSigns.CabalType.DUSK -> defense *= 0.3
        }

        return defense.toInt()
    }
}