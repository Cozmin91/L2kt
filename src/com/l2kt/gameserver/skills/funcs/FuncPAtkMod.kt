package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncPAtkMod : Func(Stats.POWER_ATTACK, 0x30, null, null) {

    override fun calc(env: Env) {
        env.mulValue(Formulas.STR_BONUS[env.character!!.str] * env.character!!.levelMod)
    }
}