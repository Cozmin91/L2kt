package com.l2kt.gameserver.handler

import com.l2kt.gameserver.handler.skillhandlers.*
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.*

object SkillHandler {
    private val _entries = HashMap<Int, ISkillHandler>()

    init {
        registerHandler(BalanceLife())
        registerHandler(Blow())
        registerHandler(Cancel())
        registerHandler(CombatPointHeal())
        registerHandler(Continuous())
        registerHandler(CpDamPercent())
        registerHandler(Craft())
        registerHandler(Disablers())
        registerHandler(DrainSoul())
        registerHandler(Dummy())
        registerHandler(Extractable())
        registerHandler(Fishing())
        registerHandler(FishingSkill())
        registerHandler(GetPlayer())
        registerHandler(GiveSp())
        registerHandler(Harvest())
        registerHandler(Heal())
        registerHandler(HealPercent())
        registerHandler(InstantJump())
        registerHandler(Manadam())
        registerHandler(ManaHeal())
        registerHandler(Mdam())
        registerHandler(Pdam())
        registerHandler(Resurrect())
        registerHandler(Sow())
        registerHandler(Spoil())
        registerHandler(StrSiegeAssault())
        registerHandler(SummonFriend())
        registerHandler(Sweep())
        registerHandler(TakeCastle())
        registerHandler(Unlock())
    }

    private fun registerHandler(handler: ISkillHandler) {
        for (t in handler.skillIds)
            _entries[t.ordinal] = handler
    }

    fun getHandler(skillType: L2SkillType): ISkillHandler? {
        return _entries[skillType.ordinal]
    }

    fun size(): Int {
        return _entries.size
    }
}